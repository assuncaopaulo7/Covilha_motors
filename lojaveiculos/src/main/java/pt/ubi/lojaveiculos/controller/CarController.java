package pt.ubi.lojaveiculos.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.ubi.lojaveiculos.model.Car;
import pt.ubi.lojaveiculos.model.Invoice;
import pt.ubi.lojaveiculos.model.User;
import pt.ubi.lojaveiculos.repository.CarRepository;
import pt.ubi.lojaveiculos.service.SaleService;

import java.util.List;

@Controller
public class CarController {

    private final CarRepository carRepository;
    private final SaleService saleService;

    public CarController(CarRepository carRepository, SaleService saleService) {
        this.carRepository = carRepository;
        this.saleService = saleService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/cars";
    }

    @GetMapping("/cars")
    public String listCars(@RequestParam(required = false) String category,
                           @RequestParam(required = false) String search,
                           Model model,
                           HttpSession session) {
        List<Car> cars;
        if (category != null && !category.isEmpty()) {
            cars = carRepository.findByCategory(category);
            model.addAttribute("filter", "Categoria: " + category);
        } else if (search != null && !search.isEmpty()) {
            cars = carRepository.findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(search, search);
            model.addAttribute("filter", "Pesquisa: " + search);
        } else {
            cars = carRepository.findAll();
        }
        model.addAttribute("cars", cars);
        model.addAttribute("loggedUser", session.getAttribute("user"));
        return "index";
    }

    @GetMapping("/buy/{id}")
    public String showPurchasePage(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || "vendor".equals(user.getRole())) {
            return "redirect:/login";
        }
        Car car = carRepository.findById(id).orElse(null);
        if (car == null) {
            return "redirect:/cars";
        }
        model.addAttribute("car", car);
        return "purchase";
    }

    @PostMapping("/buy")
    public String performPurchase(@RequestParam Long carId,
                                  @RequestParam int quantity,
                                  HttpSession session,
                                  Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || "vendor".equals(user.getRole())) {
            return "redirect:/login";
        }
        try {
            Invoice invoice = saleService.purchaseCar(carId, user.getId(), quantity);
            model.addAttribute("invoice", invoice);
            return "invoice";
        } catch (Exception e) {
            Car car = carRepository.findById(carId).orElse(null);
            model.addAttribute("car", car);
            model.addAttribute("error", e.getMessage());
            return "purchase";
        }
    }
}