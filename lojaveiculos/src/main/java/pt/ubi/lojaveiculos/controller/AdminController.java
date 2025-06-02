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
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CarRepository carRepo;
    private final LogService logService;
    private final Path uploadDir;

    public AdminController(CarRepository carRepo,
                           LogService logService,
                           @Value("${car.upload-dir:uploads}") String dir) throws IOException {
        this.carRepo = carRepo;
        this.logService = logService;
        this.uploadDir = Paths.get(dir).toAbsolutePath();
        Files.createDirectories(uploadDir);
    }

    /* ---------- PAINEL PRINCIPAL ---------- */
    @GetMapping
    public String adminHome(HttpSession ses,
                            @RequestParam(name = "filterField", required = false) String filterField,
                            @RequestParam(name = "search", required = false) String search,
                            Model model) {
        User u = (User) ses.getAttribute("user");
        if (u == null || !"admin".equals(u.getRole())) {
            return "redirect:/login";
        }

        List<Car> cars;

        if (search != null && !search.trim().isEmpty() && filterField != null) {
            switch (filterField) {
                case "marca":
                    cars = carRepo.findByBrandContainingIgnoreCase(search);
                    break;
                case "modelo":
                    cars = carRepo.findByModelContainingIgnoreCase(search);
                    break;
                case "categoria":
                    cars = carRepo.findByCategoryContainingIgnoreCase(search);
                    break;
                case "preco_abaixo":
                    try {
                        double preco = Double.parseDouble(search);
                        cars = carRepo.findByPriceLessThanEqual(preco);
                    } catch (NumberFormatException e) {
                        cars = new ArrayList<>();
                    }
                    break;
                case "preco_acima":
                    try {
                        double preco = Double.parseDouble(search);
                        cars = carRepo.findByPriceGreaterThanEqual(preco);
                    } catch (NumberFormatException e) {
                        cars = new ArrayList<>();
                    }
                    break;
                case "stock_abaixo":
                    try {
                        int stock = Integer.parseInt(search);
                        cars = carRepo.findByStockLessThanEqual(stock);
                    } catch (NumberFormatException e) {
                        cars = new ArrayList<>();
                    }
                    break;
                case "stock_acima":
                    try {
                        int stock = Integer.parseInt(search);
                        cars = carRepo.findByStockGreaterThanEqual(stock);
                    } catch (NumberFormatException e) {
                        cars = new ArrayList<>();
                    }
                    break;
                default:
                    cars = carRepo.findAllActive();
            }
            // apesar de @Where ignorar deleted=true, garantimos que não venham deletados
            cars.removeIf(Car::isDeleted);
        } else {
            cars = carRepo.findAllActive();
        }

        model.addAttribute("cars", cars);

        // Notificações de stock
        List<String> notificacoes = new ArrayList<>();
        for (Car car : cars) {
            if (car.getStock() == 0) {
                notificacoes.add("⚠ Stock esgotado: " + car.getBrand() + " " + car.getModel());
            } else if (car.getStock() < 10) {
                notificacoes.add(
                        "Aviso: stock baixo de " + car.getBrand() + " " + car.getModel() +
                                " (" + car.getStock() + " unidades)"
                );
            }
        }
        model.addAttribute("notificacoes", notificacoes);

        model.addAttribute("search", search);                   // mantém termo no input
        model.addAttribute("filterField", filterField);         // mantém dropdown selecionado

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
        if (admin == null || !"admin".equals(admin.getRole())) {
            return "redirect:/login";
        }

        String savedName = saveImage(image);

        Car car = new Car();
        car.setBrand(brand);
        car.setModel(modelCar);
        car.setCategory(category);
        car.setPrice(price);
        car.setStock(stock);
        car.setImagePath(savedName);
        car.setDeleted(false);
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
        if (admin == null || !"admin".equals(admin.getRole())) {
            return "redirect:/login";
        }
        if (image.isEmpty()) {
            return "redirect:/admin";
        }

        Optional<Car> carOpt = carRepo.findById(id);
        if (carOpt.isPresent()) {
            Car car = carOpt.get();
            if (car.getImagePath() != null) {
                try {
                    Files.deleteIfExists(uploadDir.resolve(car.getImagePath()));
                } catch (IOException ignored) { }
            }
            try {
                String newName = saveImage(image);
                car.setImagePath(newName);
                carRepo.save(car);
                logService.logCar(admin.getEmail(), "ATUALIZOU_IMAGEM", car, 0,
                        String.valueOf(car.getPrice()));
            } catch (IOException ignored) { }
        }
        return "redirect:/admin";
    }

    /* ---------- ATUALIZAR PREÇO / STOCK ---------- */
    @PostMapping("/cars/{id}/update")
    public String updateCar(HttpSession ses,
                            @PathVariable Long id,
                            @RequestParam double price,
                            @RequestParam int stock) {

        User admin = (User) ses.getAttribute("user");
        if (admin == null || !"admin".equals(admin.getRole())) {
            return "redirect:/login";
        }

        carRepo.findById(id).ifPresent(car -> {
            int oldStock = car.getStock();
            double oldPrice = car.getPrice();

            car.setPrice(price);
            car.setStock(stock);
            carRepo.save(car);

            int diff = stock - oldStock;
            if (diff > 0) {
                logService.logCar(admin.getEmail(), "INSERIU", car, diff, String.valueOf(price));
            }
            if (diff < 0) {
                logService.logCar(admin.getEmail(), "RETIROU", car, -diff, String.valueOf(price));
            }
            if (Double.compare(oldPrice, price) != 0) {
                String priceStr = oldPrice + " -> " + price;
                logService.logCar(admin.getEmail(), "ATUALIZOU", car, stock, priceStr);
            }
        });
        return "redirect:/admin";
    }

    /* ---------- ELIMINAR CARRO (SOFT DELETE) ---------- */
    @PostMapping("/cars/{id}/delete")
    public String deleteCar(HttpSession ses, @PathVariable Long id) {
        User admin = (User) ses.getAttribute("user");
        if (admin == null || !"admin".equals(admin.getRole())) {
            return "redirect:/login";
        }

        carRepo.findById(id).ifPresent(car -> {
            logService.logCar(admin.getEmail(), "ELIMINOU", car, car.getStock(),
                    String.valueOf(car.getPrice()));
            car.setDeleted(true);
            carRepo.save(car);
        });
        return "redirect:/admin";
    }

    /* ---------- utilitário para gravar ficheiro ---------- */
    private String saveImage(MultipartFile img) throws IOException {
        if (img == null || img.isEmpty()) {
            return null;
        }
        String originalFilename = Optional.ofNullable(img.getOriginalFilename()).orElse("");
        String ext = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            ext = originalFilename.substring(dotIndex);
        }
        String saved = UUID.randomUUID() + ext;
        Files.copy(img.getInputStream(), uploadDir.resolve(saved), StandardCopyOption.REPLACE_EXISTING);
        return saved;
    }
}