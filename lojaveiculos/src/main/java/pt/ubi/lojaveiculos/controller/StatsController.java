package pt.ubi.lojaveiculos.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pt.ubi.lojaveiculos.model.User;

/**
 * Simple placeholder controller that shows the “Estatísticas” page
 * (currently under construction) and protects it so only admins can see it.
 */
@Controller
@RequestMapping("/admin/stats")
public class StatsController {

    @GetMapping
    public String showStats(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            return "redirect:/login";
        }
        // templates/stats-placeholder.html
        return "stats-placeholder";
    }
}
