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

/**
 * Painel Admin:
 * • Pesquisa avançada - marca/modelo/categoria/preço/stock
 * • CRUD de veículos (imagem & soft-delete)
 * • Exportar + Importar base de dados
 * • Notificações de baixo stock
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CarRepository carRepo;
    private final LogService    logService;
    private final Path          uploadDir;

    /* credenciais BD para mysqldump/mysql */
    private final String dbUser, dbPass, dbName;

    public AdminController(CarRepository carRepo,
                           LogService    logService,
                           @Value("${car.upload-dir:uploads}") String dir,
                           @Value("${spring.datasource.username}") String dbUser,
                           @Value("${spring.datasource.password}") String dbPass,
                           @Value("${spring.datasource.url}")      String dbUrl) throws IOException {

        this.carRepo    = carRepo;
        this.logService = logService;
        this.uploadDir  = Files.createDirectories(Paths.get(dir).toAbsolutePath());

        this.dbUser = dbUser;
        this.dbPass = dbPass;
        this.dbName = dbUrl.substring(dbUrl.lastIndexOf('/') + 1).split("\\?")[0]; // extrai nome da BD
    }

    /* ===============================================================
       PÁGINA PRINCIPAL  (/admin)
       =============================================================== */
    @GetMapping
    public String adminHome(HttpSession ses,
                            @RequestParam(name="filterField", required=false) String filterField,
                            @RequestParam(name="search",      required=false) String search,
                            Model m) {

        User u = (User) ses.getAttribute("user");
        if (u == null || !"admin".equals(u.getRole())) return "redirect:/login";

        List<Car> cars;
        if (search != null && !search.trim().isEmpty() && filterField != null) {
            switch (filterField) {
                case "marca"        -> cars = carRepo.findByBrandContainingIgnoreCase(search);
                case "modelo"       -> cars = carRepo.findByModelContainingIgnoreCase(search);
                case "categoria"    -> cars = carRepo.findByCategoryContainingIgnoreCase(search);
                case "preco_abaixo" -> cars = tryDouble(search)
                        .map(carRepo::findByPriceLessThanEqual)
                        .orElse(List.of());
                case "preco_acima"  -> cars = tryDouble(search)
                        .map(carRepo::findByPriceGreaterThanEqual)
                        .orElse(List.of());
                case "stock_abaixo" -> cars = tryInt(search)
                        .map(carRepo::findByStockLessThanEqual)
                        .orElse(List.of());
                case "stock_acima"  -> cars = tryInt(search)
                        .map(carRepo::findByStockGreaterThanEqual)
                        .orElse(List.of());
                default             -> cars = carRepo.findAllActive();
            }
            cars.removeIf(Car::isDeleted);   // salvaguarda extra
        } else {
            cars = carRepo.findAllActive();
        }

        /* notificações de stock */
        List<String> notifs = new ArrayList<>();
        for (Car c : cars) {
            if (c.getStock() == 0)
                notifs.add("⚠ Stock esgotado: " + c.getBrand() + " " + c.getModel());
            else if (c.getStock() < 10)
                notifs.add("Aviso: stock baixo de " + c.getBrand() + " "
                        + c.getModel() + " (" + c.getStock() + ")");
        }

        m.addAttribute("cars", cars);
        m.addAttribute("search", search == null ? "" : search);
        m.addAttribute("filterField", filterField == null ? "" : filterField);
        m.addAttribute("notificacoes", notifs);
        return "admin";
    }

    /* ===============================================================
       EXPORTAR BASE DE DADOS  (/admin/db/export)
       =============================================================== */
    @GetMapping("/db/export")
    public ResponseEntity<FileSystemResource> exportDb(HttpSession s)
            throws IOException, InterruptedException {

        if (!isAdmin(s)) return ResponseEntity.status(403).build();

        Path dump = Files.createTempFile("loja_veiculos-", ".sql");
        int code = new ProcessBuilder("mysqldump",
                "-u"+dbUser,"-p"+dbPass, dbName)
                .redirectOutput(dump.toFile())
                .start().waitFor();
        if (code != 0) { Files.deleteIfExists(dump);
            return ResponseEntity.internalServerError().build(); }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"loja_veiculos.sql\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(new FileSystemResource(dump));
    }

    /* ===============================================================
       IMPORTAR BASE DE DADOS  (/admin/db/import)
       =============================================================== */
    @PostMapping(path="/db/import", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public String importDb(HttpSession s,
                           @RequestParam("file") MultipartFile file)
            throws IOException, InterruptedException {

        if (!isAdmin(s) || file.isEmpty()) return "redirect:/admin";

        Path tmp = Files.createTempFile("upload-", ".sql");
        Files.copy(file.getInputStream(), tmp, StandardCopyOption.REPLACE_EXISTING);

        int code = new ProcessBuilder("mysql",
                "-u"+dbUser,"-p"+dbPass, dbName)
                .redirectInput(tmp.toFile())
                .start().waitFor();
        Files.deleteIfExists(tmp);
        return (code == 0) ? "redirect:/admin" : "redirect:/admin?err=import";
    }

    /* ===============================================================
       CRUD DE VEÍCULOS  (mesma lógica de antes)
       =============================================================== */
    @PostMapping(path="/cars", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public String addCar(HttpSession s,
                         @RequestParam String brand,
                         @RequestParam("modelCar") String model,
                         @RequestParam String category,
                         @RequestParam double price,
                         @RequestParam int stock,
                         @RequestParam("image") MultipartFile img) throws IOException {

        if (!isAdmin(s)) return "redirect:/login";

        Car car = new Car();
        car.setBrand(brand); car.setModel(model); car.setCategory(category);
        car.setPrice(price); car.setStock(stock); car.setImagePath(saveImage(img));
        carRepo.save(car);
        logService.logCar(getEmail(s),"CRIOU",car,stock,String.valueOf(price));
        return "redirect:/admin";
    }

    @PostMapping(path="/cars/{id}/image", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadImage(HttpSession s,@PathVariable Long id,
                              @RequestParam("image") MultipartFile img) throws IOException {

        if (!isAdmin(s) || img.isEmpty()) return "redirect:/admin";
        carRepo.findById(id).ifPresent(c -> {
            try { if (c.getImagePath()!=null)
                Files.deleteIfExists(uploadDir.resolve(c.getImagePath())); }
            catch(IOException ignored){}
            try {
                c.setImagePath(saveImage(img)); carRepo.save(c);
                logService.logCar(getEmail(s),"ATUALIZOU_IMAGEM",
                        c,0,String.valueOf(c.getPrice()));
            } catch(IOException ignored){}
        });
        return "redirect:/admin";
    }

    @PostMapping("/cars/{id}/update")
    public String updateCar(HttpSession s,
                            @PathVariable Long id,
                            @RequestParam double price,
                            @RequestParam int stock) {

        if (!isAdmin(s)) return "redirect:/login";
        carRepo.findById(id).ifPresent(c -> {
            int diff = stock - c.getStock();
            double old = c.getPrice();
            c.setPrice(price); c.setStock(stock); carRepo.save(c);

            if (Double.compare(old,price)!=0)
                logService.logCar(getEmail(s),"ATUALIZOU",c,stock,old+" -> "+price);
            if (diff>0) logService.logCar(getEmail(s),"INSERIU", c,diff,String.valueOf(price));
            if (diff<0) logService.logCar(getEmail(s),"RETIROU", c,-diff,String.valueOf(price));
        });
        return "redirect:/admin";
    }

    @PostMapping("/cars/{id}/delete")
    public String deleteCar(HttpSession s,@PathVariable Long id){
        if (!isAdmin(s)) return "redirect:/login";
        carRepo.findById(id).ifPresent(c->{
            logService.logCar(getEmail(s),"ELIMINOU",c,c.getStock(),String.valueOf(c.getPrice()));
            c.setDeleted(true); carRepo.save(c);
        });
        return "redirect:/admin";
    }

    /* =============== helpers =============== */
    private boolean isAdmin(HttpSession s){
        User u=(User)s.getAttribute("user");
        return u!=null && "admin".equals(u.getRole());
    }
    private String getEmail(HttpSession s){
        User u=(User)s.getAttribute("user");
        return u==null? "admin@loja" : u.getEmail();
    }
    private String saveImage(MultipartFile img)throws IOException{
        if (img==null||img.isEmpty()) return null;
        String ext=Optional.ofNullable(img.getOriginalFilename())
                .filter(n->n.contains("."))
                .map(n->n.substring(n.lastIndexOf('.')))
                .orElse("");
        String name=UUID.randomUUID()+ext;
        Files.copy(img.getInputStream(),
                uploadDir.resolve(name),StandardCopyOption.REPLACE_EXISTING);
        return name;
    }
    private static Optional<Double> tryDouble(String s){
        try{ return Optional.of(Double.parseDouble(s)); }catch(NumberFormatException e){return Optional.empty();}
    }
    private static Optional<Integer> tryInt(String s){
        try{ return Optional.of(Integer.parseInt(s)); }catch(NumberFormatException e){return Optional.empty();}
    }
}
