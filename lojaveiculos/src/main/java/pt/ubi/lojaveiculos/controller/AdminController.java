package pt.ubi.lojaveiculos.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.ubi.lojaveiculos.model.Car;
import pt.ubi.lojaveiculos.model.User;
import pt.ubi.lojaveiculos.repository.CarRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CarRepository carRepository;

    public AdminController(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    // === ADMIN HOME =========================================================
    @GetMapping
    public String adminHome(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("cars", carRepository.findAll());
        return "admin";
    }

    // === ADD CAR ============================================================
    @PostMapping("/cars")
    public String addCar(HttpSession session,
                         @RequestParam String brand,
                         @RequestParam("modelCar") String modelCar,
                         @RequestParam String category,
                         @RequestParam double price,
                         @RequestParam int stock) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            return "redirect:/login";
        }
        Car car = new Car();
        car.setBrand(brand);
        car.setModel(modelCar);
        car.setCategory(category);
        car.setPrice(price);
        car.setStock(stock);
        carRepository.save(car);
        return "redirect:/admin";
    }

    // === DELETE CAR =========================================================
    @PostMapping("/cars/{id}/delete")
    public String deleteCar(HttpSession session, @PathVariable Long id) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            return "redirect:/login";
        }
        carRepository.deleteById(id);
        return "redirect:/admin";
    }

    // === UPDATE CAR =========================================================
    @PostMapping("/cars/{id}/update")
    public String updateCar(HttpSession session,
                            @PathVariable Long id,
                            @RequestParam double price,
                            @RequestParam int stock) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            return "redirect:/login";
        }
        carRepository.findById(id).ifPresent(car -> {
            car.setPrice(price);
            car.setStock(stock);
            carRepository.save(car);
        });
        return "redirect:/admin";
    }

    // === PLACEHOLDER FOR FUTURE STATISTICS ==================================
    @GetMapping("/stats")
    public String statistics(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            return "redirect:/login";
        }
        // TODO implement statistics page later
        return "stats-placeholder"; // simple template saying "Em desenvolvimento"
    }
}