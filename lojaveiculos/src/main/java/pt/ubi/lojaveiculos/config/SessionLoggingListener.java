package pt.ubi.lojaveiculos.config;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;
import pt.ubi.lojaveiculos.model.User;
import pt.ubi.lojaveiculos.service.LogService;

/**
 * Listens for every HTTP-session that is destroyed.
 * That covers:
 *   • explicit session.invalidate()                     (user clicks Logout)
 *   • session timeout / expiration                      (user is idle)
 *   • application shutdown                              (dev stops Spring Boot)
 *
 * If the session still contains a User object, we record one—and only one—
 * LOGOUT action.
 */
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
