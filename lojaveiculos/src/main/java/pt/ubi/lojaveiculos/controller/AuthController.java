package pt.ubi.lojaveiculos.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.ubi.lojaveiculos.model.User;
import pt.ubi.lojaveiculos.repository.UserRepository;
import pt.ubi.lojaveiculos.service.LogService;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final LogService logService;

    public AuthController(UserRepository userRepository, LogService logService) {
        this.userRepository = userRepository;
        this.logService     = logService;
    }

    /* --------------------- LOGIN --------------------- */
    @GetMapping("/login")
    public String loginForm(HttpSession session) {
        if (session.getAttribute("user") != null) return "redirect:/cars";
        return "login";
    }

    @PostMapping("/login")
    public String performLogin(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        User user = userRepository.findByEmail(email);
        if (user == null || !user.getPassword().equals(password)) {
            model.addAttribute("error", "Credenciais inválidas.");
            return "login";
        }
        session.setAttribute("user", user);
        logService.logUser(user, "LOGIN");
        return "redirect:/cars";
    }

    /* --------------------- REGISTER (só clientes) ---- */
    @GetMapping("/register")
    public String registerForm() { return "register"; }

    @PostMapping("/register")
    public String performRegister(@RequestParam String nome,
                                  @RequestParam String email,
                                  @RequestParam String password,
                                  HttpSession session,
                                  Model model) {

        if (userRepository.findByEmail(email) != null) {
            model.addAttribute("error", "Email já cadastrado.");
            return "register";
        }
        User user = new User();
        user.setNome(nome);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("client");
        userRepository.save(user);

        logService.logUser(user, "REGISTERED");

        session.setAttribute("user", user);
        return "redirect:/cars";
    }

    /* --------------------- LOGOUT -------------------- */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        User u = (User) session.getAttribute("user");
        if (u != null) logService.logUser(u, "LOGOUT");
        session.invalidate();
        return "redirect:/cars";
    }
}
