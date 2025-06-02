package pt.ubi.lojaveiculos.config;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;
import pt.ubi.lojaveiculos.model.User;
import pt.ubi.lojaveiculos.service.LogService;

@Component
public class SessionLoggingListener implements HttpSessionListener {

    private final LogService logService;

    public SessionLoggingListener(LogService logService) {
        this.logService = logService;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        Object u = event.getSession().getAttribute("user");
        if (u instanceof User user) {
            logService.logUser(user, "LOGOUT");
        }
    }
}
