package pt.ubi.lojaveiculos.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pt.ubi.lojaveiculos.model.User;
import pt.ubi.lojaveiculos.repository.UserRepository;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner loadAdmin(UserRepository userRepository) {
        return args -> {
            final String adminEmail = "admin@loja.pt";
            if (userRepository.findByEmail(adminEmail) == null) {
                User admin = new User();
                admin.setNome("Administrador");
                admin.setEmail(adminEmail);
                admin.setPassword("password");
                admin.setRole("admin");
                userRepository.save(admin);
                System.out.println("Admin default criado: " + adminEmail + "/password");
            }
        };
    }
}