package pt.ubi.lojaveiculos.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.ubi.lojaveiculos.model.Car;
import pt.ubi.lojaveiculos.model.User;
import pt.ubi.lojaveiculos.repository.CarRepository;
import pt.ubi.lojaveiculos.service.LogService;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;


@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CarRepository carRepo;
    private final LogService    logService;
    private final Path          uploadDir;

    /* credenciais da BD (usadas por mysqldump/mysql) */
    private final String dbUser;
    private final String dbPass;
    private final String dbName;

    public AdminController(CarRepository carRepo,
                           LogService    logService,
                           @Value("${car.upload-dir:uploads}") String dir,
                           @Value("${spring.datasource.username}") String dbUser,
                           @Value("${spring.datasource.password}") String dbPass,
                           @Value("${spring.datasource.url}")      String dbUrl)
            throws IOException {

        this.carRepo    = carRepo;
        this.logService = logService;

        this.uploadDir  = Files.createDirectories(Paths.get(dir).toAbsolutePath());

        this.dbUser = dbUser;
        this.dbPass = dbPass;
        /* extrai DBNAME de jdbc:mysql://host:port/DBNAME?... */
        this.dbName = dbUrl.substring(dbUrl.lastIndexOf('/') + 1)
                .split("\\?")[0];
    }

    /* ------------------------------------------------------------------
       PÁGINA PRINCIPAL (GET /admin)
       ------------------------------------------------------------------ */
    @GetMapping
    public String adminHome(HttpSession ses, Model m) {
        if (!isAdmin(ses)) return "redirect:/login";

        List<Car> cars = carRepo.findAll();
        m.addAttribute("cars", cars);

        /* ===== Gera lista de notificações de stock ===== */
        List<String> notifs = new ArrayList<>();
        for (Car c : cars) {
            if (c.isDeleted()) continue;               // ignora soft-deletes
            if (c.getStock() == 0) {
                notifs.add("⚠ Stock esgotado: " + c.getBrand() + " " + c.getModel());
            } else if (c.getStock() < 10) {
                notifs.add("Aviso: stock baixo de " + c.getBrand() + " " + c.getModel()
                        + " (" + c.getStock() + " unidades)");
            }
        }
        m.addAttribute("notificacoes", notifs);

        return "admin";
    }

    /* ------------------------------------------------------------------
       EXPORTAR BD  (GET /admin/db/export)
       ------------------------------------------------------------------ */
    @GetMapping("/db/export")
    public ResponseEntity<FileSystemResource> exportDb(HttpSession ses)
            throws IOException, InterruptedException {

        if (!isAdmin(ses)) return ResponseEntity.status(403).build();

        Path dump = Files.createTempFile("loja_veiculos-", ".sql");

        ProcessBuilder pb = new ProcessBuilder(
                "mysqldump",
                "-u" + dbUser,
                "-p" + dbPass,
                dbName);
        pb.redirectOutput(dump.toFile());

        if (pb.start().waitFor() != 0) {
            Files.deleteIfExists(dump);
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"loja_veiculos.sql\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(new FileSystemResource(dump));
    }

    /* ------------------------------------------------------------------
       IMPORTAR BD  (POST /admin/db/import)
       ------------------------------------------------------------------ */
    @PostMapping(path="/db/import", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public String importDb(HttpSession ses,
                           @RequestParam("file") MultipartFile file)
            throws IOException, InterruptedException {

        if (!isAdmin(ses) || file.isEmpty()) return "redirect:/admin";

        Path tmpSql = Files.createTempFile("upload-sql-", ".sql");
        Files.copy(file.getInputStream(), tmpSql, StandardCopyOption.REPLACE_EXISTING);

        ProcessBuilder pb = new ProcessBuilder(
                "mysql",
                "-u" + dbUser,
                "-p" + dbPass,
                dbName);
        pb.redirectInput(tmpSql.toFile());

        int exit = pb.start().waitFor();
        Files.deleteIfExists(tmpSql);

        return (exit == 0) ? "redirect:/admin" : "redirect:/admin?err=import";
    }

    /* ------------------------------------------------------------------
       CRIAR NOVO CARRO  (POST /admin/cars)
       ------------------------------------------------------------------ */
    @PostMapping(path="/cars", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public String addCar(HttpSession ses,
                         @RequestParam String brand,
                         @RequestParam("modelCar") String modelCar,
                         @RequestParam String category,
                         @RequestParam double price,
                         @RequestParam int stock,
                         @RequestParam("image") MultipartFile image) throws IOException {

        if (!isAdmin(ses)) return "redirect:/login";

        Car car = new Car();
        car.setBrand(brand);
        car.setModel(modelCar);
        car.setCategory(category);
        car.setPrice(price);
        car.setStock(stock);
        car.setImagePath(saveImage(image));
        carRepo.save(car);

        logService.logCar(getAdminEmail(ses), "CRIOU", car, stock, String.valueOf(price));
        return "redirect:/admin";
    }

    /* ------------------------------------------------------------------
       UPLOAD / TROCAR IMAGEM  (POST /admin/cars/{id}/image)
       ------------------------------------------------------------------ */
    @PostMapping(path="/cars/{id}/image", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadImage(HttpSession ses,
                              @PathVariable Long id,
                              @RequestParam("image") MultipartFile image) throws IOException {

        if (!isAdmin(ses) || image.isEmpty()) return "redirect:/admin";

        carRepo.findById(id).ifPresent(car -> {
            if (car.getImagePath() != null) {
                try { Files.deleteIfExists(uploadDir.resolve(car.getImagePath())); }
                catch (IOException ignored) {}
            }
            try {
                car.setImagePath(saveImage(image));
                carRepo.save(car);

                logService.logCar(getAdminEmail(ses), "ATUALIZOU_IMAGEM",
                        car, car.getStock(), String.valueOf(car.getPrice()));
            } catch (IOException ignored) {}
        });
        return "redirect:/admin";
    }

    /* ------------------------------------------------------------------
       ATUALIZAR PREÇO / STOCK  (POST /admin/cars/{id}/update)
       ------------------------------------------------------------------ */
    @PostMapping("/cars/{id}/update")
    public String updateCar(HttpSession ses,
                            @PathVariable Long id,
                            @RequestParam double price,
                            @RequestParam int stock) {

        if (!isAdmin(ses)) return "redirect:/login";

        carRepo.findById(id).ifPresent(car -> {

            int    oldStock = car.getStock();
            double oldPrice = car.getPrice();

            car.setPrice(price);
            car.setStock(stock);
            carRepo.save(car);

            /* 1) regista alteração de preço (se existir) */
            if (Double.compare(oldPrice, price) != 0) {
                logService.logCar(getAdminEmail(ses), "ATUALIZOU",
                        car, stock, oldPrice + " -> " + price);
            }

            /* 2) regista variação de stock */
            int diff = stock - oldStock;
            if (diff > 0)
                logService.logCar(getAdminEmail(ses), "INSERIU", car, diff, String.valueOf(price));
            if (diff < 0)
                logService.logCar(getAdminEmail(ses), "RETIROU", car, -diff, String.valueOf(price));
        });
        return "redirect:/admin";
    }

    /* ------------------------------------------------------------------
       ELIMINAR CARRO (soft-delete)  (POST /admin/cars/{id}/delete)
       ------------------------------------------------------------------ */
    @PostMapping("/cars/{id}/delete")
    public String deleteCar(HttpSession ses, @PathVariable Long id) {
        if (!isAdmin(ses)) return "redirect:/login";

        carRepo.findById(id).ifPresent(car -> {
            logService.logCar(getAdminEmail(ses), "ELIMINOU",
                    car, car.getStock(), String.valueOf(car.getPrice()));
            car.setDeleted(true);
            carRepo.save(car);
        });
        return "redirect:/admin";
    }

    /* ------------------------------------------------------------------
       MÉTODOS AUXILIARES
       ------------------------------------------------------------------ */
    private boolean isAdmin(HttpSession ses) {
        User u = (User) ses.getAttribute("user");
        return u != null && "admin".equals(u.getRole());
    }

    private String getAdminEmail(HttpSession ses) {
        User u = (User) ses.getAttribute("user");
        return u == null ? "admin@loja" : u.getEmail();
    }

    private String saveImage(MultipartFile img) throws IOException {
        if (img == null || img.isEmpty()) return null;

        String ext = Optional.ofNullable(img.getOriginalFilename())
                .filter(n -> n.contains("."))
                .map(n -> n.substring(n.lastIndexOf('.')))
                .orElse("");
        String name = UUID.randomUUID() + ext;
        Files.copy(img.getInputStream(),
                uploadDir.resolve(name),
                StandardCopyOption.REPLACE_EXISTING);
        return name;
    }
}
