package pt.ubi.lojaveiculos.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import pt.ubi.lojaveiculos.model.User;
import pt.ubi.lojaveiculos.repository.UserLogRepository;
import pt.ubi.lojaveiculos.repository.UserRepository;
import pt.ubi.lojaveiculos.service.LogService;

import java.util.List;


@Component
public class StartupSessionReconciler {

    private final UserLogRepository userLogRepo;
    private final UserRepository    userRepo;
    private final LogService        logService;

    public StartupSessionReconciler(UserLogRepository userLogRepo,
                                    UserRepository userRepo,
                                    LogService logService) {
        this.userLogRepo = userLogRepo;
        this.userRepo    = userRepo;
        this.logService  = logService;
    }

    @PostConstruct
    public void reconcileOpenLogins() {
        List<String> emails = userLogRepo.findEmailsWithOpenLogin();

        for (String email : emails) {
            User u = userRepo.findByEmail(email);
            if (u != null) {                 // conta ainda existe
                logService.logUser(u, "LOGOUT");
            }
        }
    }
}
