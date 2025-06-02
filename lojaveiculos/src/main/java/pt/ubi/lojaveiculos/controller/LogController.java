package pt.ubi.lojaveiculos.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import pt.ubi.lojaveiculos.model.User;
import pt.ubi.lojaveiculos.service.LogService;

import java.util.List;

@Controller
@RequestMapping("/admin/logs")
public class LogController {

    private final LogService logService;
    private static final int PAGE_SIZE = 10;   // 10 registos por página

    public LogController(LogService logService) {
        this.logService = logService;
    }

    /* ---------- menu geral ---------- */
    @GetMapping
    public String logsHome(HttpSession s) {
        return isAdmin(s) ? "logs_index" : "redirect:/login";
    }

    /* ---------- UTILIZADORES ------------------------ */
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

    /* ---------- VEÍCULOS (com filtro + paginação) --- */
    @GetMapping("/cars")
    public String carLogs(@RequestParam(required = false) String email,
                          @RequestParam(required = false) String action,
                          @RequestParam(defaultValue = "1") int page,
                          Model m,
                          HttpSession s) {

        if (!isAdmin(s)) return "redirect:/login";

        /* —— lista filtrada (ordem desc) —— */
        List<?> filtered = logService.listCarLogs(email, action);

        /* —— paginação básica —— */
        int totalPages = (int) Math.ceil(filtered.size() / (double) PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;
        if (page < 1)                page = 1;
        if (page > totalPages)       page = totalPages;

        int from = (page - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, filtered.size());

        m.addAttribute("logs",        filtered.subList(from, to));
        m.addAttribute("filterEmail", email  == null ? "" : email);
        m.addAttribute("filterAction",action == null ? "" : action);
        m.addAttribute("pageCurrent", page);
        m.addAttribute("pageTotal",   totalPages);

        return "logs_cars";
    }

    @PostMapping("/cars/clear")
    public String clearCarLogs(HttpSession s) {
        if (!isAdmin(s)) return "redirect:/login";
        logService.clearCarLogs();
        return "redirect:/admin/logs/cars";
    }

    /* ---------- helper ------------------------------ */
    private boolean isAdmin(HttpSession s) {
        User u = (User) s.getAttribute("user");
        return u != null && "admin".equals(u.getRole());
    }
}
