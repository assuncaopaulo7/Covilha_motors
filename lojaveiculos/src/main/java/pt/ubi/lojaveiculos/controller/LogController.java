package pt.ubi.lojaveiculos.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import pt.ubi.lojaveiculos.model.User;
import pt.ubi.lojaveiculos.service.LogService;

@Controller
@RequestMapping("/admin/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    /* ---------- menu --------- */
    @GetMapping
    public String logsHome(HttpSession s) {
        return isAdmin(s) ? "logs_index" : "redirect:/login";
    }

    /* ---------- UTILIZADORES ---------- */
    @GetMapping("/users")
    public String userLogs(Model m, HttpSession s) {
        if (!isAdmin(s)) return "redirect:/login";
        m.addAttribute("logs", logService.listUserLogs());
        return "logs_users";
    }

    @PostMapping("/users/clear")
    public String clearUserLogs(HttpSession s) {
        if (!isAdmin(s)) return "redirect:/login";
        logService.clearUserLogs();
        return "redirect:/admin/logs/users";
    }

    /* ---------- VEÍCULOS -------------- */
    @GetMapping("/cars")
    public String carLogs(Model m, HttpSession s) {
        if (!isAdmin(s)) return "redirect:/login";
        m.addAttribute("logs", logService.listCarLogs());
        return "logs_cars";
    }

    @PostMapping("/cars/clear")
    public String clearCarLogs(HttpSession s) {
        if (!isAdmin(s)) return "redirect:/login";
        logService.clearCarLogs();
        return "redirect:/admin/logs/cars";
    }

    /* ---------- helper --------------- */
    private boolean isAdmin(HttpSession s) {
        User u = (User) s.getAttribute("user");
        return u != null && "admin".equals(u.getRole());
    }
}
