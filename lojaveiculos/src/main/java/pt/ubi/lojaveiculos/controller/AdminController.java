package pt.ubi.lojaveiculos.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
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
import java.util.Optional;
import java.util.UUID;

import java.util.List;
import java.util.ArrayList;


@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CarRepository carRepo;
    private final LogService    logService;
    private final Path          uploadDir;

    public AdminController(CarRepository carRepo,
                           LogService logService,
                           @Value("${car.upload-dir:uploads}") String dir)
            throws IOException {
        this.carRepo    = carRepo;
        this.logService = logService;
        this.uploadDir  = Paths.get(dir).toAbsolutePath();
        Files.createDirectories(uploadDir);
    }

    /* ---------- PAINEL PRINCIPAL ---------- */
    @GetMapping
    public String adminHome(HttpSession ses, Model m) {
        User u = (User) ses.getAttribute("user");
        if (u == null || !"admin".equals(u.getRole())) return "redirect:/login";

        List<Car> cars = carRepo.findAll();
        m.addAttribute("cars", cars);

        // Notificações de stock
        List<String> notificacoes = new ArrayList<>();
        for (Car car : cars) {
            if (car.isDeleted()) continue; // ignora soft-deletados
            if (car.getStock() == 0) {
                notificacoes.add("⚠ Stock esgotado: " + car.getBrand() + " " + car.getModel());
            } else if (car.getStock() < 10) {
                notificacoes.add("Aviso: stock baixo de " + car.getBrand() + " " + car.getModel() +
                        " (" + car.getStock() + " unidades)");
            }
        }
        m.addAttribute("notificacoes", notificacoes);

        return "admin";
    }

    /* ---------- CRIAR NOVO CARRO ---------- */
    @PostMapping(path = "/cars", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String addCar(HttpSession ses,
                         @RequestParam String brand,
                         @RequestParam("modelCar") String modelCar,
                         @RequestParam String category,
                         @RequestParam double price,
                         @RequestParam int stock,
                         @RequestParam("image") MultipartFile image) throws IOException {

        User admin = (User) ses.getAttribute("user");
        if (admin == null || !"admin".equals(admin.getRole())) return "redirect:/login";

        String savedName = saveImage(image);

        Car car = new Car();
        car.setBrand(brand);
        car.setModel(modelCar);
        car.setCategory(category);
        car.setPrice(price);
        car.setStock(stock);
        car.setImagePath(savedName);
        carRepo.save(car);

        logService.logCar(admin.getEmail(), "CRIOU", car, stock, String.valueOf(price));
        return "redirect:/admin";
    }

    /* ---------- SUBSTITUIR / ADICIONAR IMAGEM ---------- */
    @PostMapping(path = "/cars/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadImage(HttpSession ses,
                              @PathVariable Long id,
                              @RequestParam("image") MultipartFile image) throws IOException {

        User admin = (User) ses.getAttribute("user");
        if (admin == null || !"admin".equals(admin.getRole())) return "redirect:/login";
        if (image.isEmpty()) return "redirect:/admin";

        carRepo.findById(id).ifPresent(car -> {
            if (car.getImagePath() != null) {
                try { Files.deleteIfExists(uploadDir.resolve(car.getImagePath())); } catch (IOException ignored) { }
            }
            try {
                String newName = saveImage(image);
                car.setImagePath(newName);
                carRepo.save(car);
                logService.logCar(admin.getEmail(), "ATUALIZOU_IMAGEM", car, 0,
                        String.valueOf(car.getPrice()));
            } catch (IOException ignored) { }
        });
        return "redirect:/admin";
    }

    /* ---------- ATUALIZAR PREÇO / STOCK ---------- */
    @PostMapping("/cars/{id}/update")
    public String updateCar(HttpSession ses,
                            @PathVariable Long id,
                            @RequestParam double price,
                            @RequestParam int stock) {

        User admin = (User) ses.getAttribute("user");
        if (admin == null || !"admin".equals(admin.getRole())) return "redirect:/login";

        carRepo.findById(id).ifPresent(car -> {
            int    oldStock  = car.getStock();
            double oldPrice  = car.getPrice();

            car.setPrice(price);
            car.setStock(stock);
            carRepo.save(car);

            /* -------- LOG STOCK MOVIMENTO -------- */
            int diff = stock - oldStock;
            if (diff > 0)
                logService.logCar(admin.getEmail(), "INSERIU",  car, diff, String.valueOf(price));
            if (diff < 0)
                logService.logCar(admin.getEmail(), "RETIROU",  car, -diff, String.valueOf(price));

            /* -------- LOG ALTERAÇÃO DE PREÇO ------ */
            if (Double.compare(oldPrice, price) != 0) {
                String priceStr = oldPrice + " -> " + price;
                logService.logCar(admin.getEmail(), "ATUALIZOU", car, stock, priceStr);
            }
        });
        return "redirect:/admin";
    }

    /* ---------- ELIMINAR CARRO ---------- */
    @PostMapping("/cars/{id}/delete")
    public String deleteCar(HttpSession ses, @PathVariable Long id) {
        User admin = (User) ses.getAttribute("user");
        if (admin == null || !"admin".equals(admin.getRole())) return "redirect:/login";

        carRepo.findById(id).ifPresent(car -> {
            logService.logCar(admin.getEmail(), "ELIMINOU", car, car.getStock(),
                    String.valueOf(car.getPrice()));
            car.setDeleted(true);           // soft-delete
            carRepo.save(car);
        });
        return "redirect:/admin";
    }

    /* ---------- utilitário para gravar ficheiro ---------- */
    private String saveImage(MultipartFile img) throws IOException {
        if (img.isEmpty()) return null;
        String ext   = Optional.ofNullable(img.getOriginalFilename())
                .filter(n -> n.contains("."))
                .map(n -> n.substring(n.lastIndexOf('.')))
                .orElse("");
        String saved = UUID.randomUUID() + ext;
        Files.copy(img.getInputStream(),
                uploadDir.resolve(saved),
                StandardCopyOption.REPLACE_EXISTING);
        return saved;
    }
}
