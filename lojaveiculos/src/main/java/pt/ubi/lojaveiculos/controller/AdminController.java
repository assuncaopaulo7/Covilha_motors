package pt.ubi.lojaveiculos.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import pt.ubi.lojaveiculos.model.Car;
import pt.ubi.lojaveiculos.model.User;
import pt.ubi.lojaveiculos.repository.CarRepository;
import pt.ubi.lojaveiculos.service.LogService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CarRepository carRepository;
    private final LogService    logService;

    public AdminController(CarRepository carRepository, LogService logService) {
        this.carRepository = carRepository;
        this.logService    = logService;
    }

    /* --------------------- HOME ---------------------- */
    @GetMapping
    public String adminHome(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole()))
            return "redirect:/login";

        model.addAttribute("cars", carRepository.findAll());
        return "admin";
    }

    /* --------------------- ADD (CRIAR) --------------- */
    @PostMapping("/cars")
    public String addCar(HttpSession session,
                         @RequestParam String brand,
                         @RequestParam("modelCar") String modelCar,
                         @RequestParam String category,
                         @RequestParam double price,
                         @RequestParam int stock) {

        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole()))
            return "redirect:/login";

        Car car = new Car();
        car.setBrand(brand);
        car.setModel(modelCar);
        car.setCategory(category);
        car.setPrice(price);
        car.setStock(stock);
        carRepository.save(car);

        /* ---- LOG como CRIOU ------------------------- */
        logService.logCar(user.getEmail(), "CRIOU", car, stock);

        return "redirect:/admin";
    }

    /* --------------------- DELETE -------------------- */
    @PostMapping("/cars/{id}/delete")
    public String deleteCar(HttpSession session, @PathVariable Long id) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole()))
            return "redirect:/login";

        carRepository.findById(id).ifPresent(car ->
                logService.logCar(user.getEmail(), "ELIMINOU", car, car.getStock()));

        carRepository.deleteById(id);
        return "redirect:/admin";
    }

    /* --------------------- UPDATE -------------------- */
    @PostMapping("/cars/{id}/update")
    public String updateCar(HttpSession session,
                            @PathVariable Long id,
                            @RequestParam double price,
                            @RequestParam int stock) {

        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole()))
            return "redirect:/login";

        carRepository.findById(id).ifPresent(car -> {
            int oldStock = car.getStock();
            car.setPrice(price);
            car.setStock(stock);
            carRepository.save(car);

            int diff = stock - oldStock;
            if (diff > 0)  logService.logCar(user.getEmail(), "INSERIU",  car, diff);
            if (diff < 0)  logService.logCar(user.getEmail(), "RETIROU", car, -diff);
        });
        return "redirect:/admin";
    }

    /* --------------------- PLACEHOLDER STATS --------- */
    @GetMapping("/stats")
    public String stats(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole()))
            return "redirect:/login";
        return "stats-placeholder";
    }
}
