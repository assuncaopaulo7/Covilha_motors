package pt.ubi.lojaveiculos.service;

import org.springframework.stereotype.Service;
import pt.ubi.lojaveiculos.model.*;
import pt.ubi.lojaveiculos.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class LogService {

    private final UserLogRepository userRepo;
    private final CarLogRepository  carRepo;

    public LogService(UserLogRepository userRepo,
                      CarLogRepository carRepo) {
        this.userRepo = userRepo;
        this.carRepo  = carRepo;
    }

    /* =================== UTILIZADORES =================== */

    public void logUser(User user, String action) {
        UserLog ul = new UserLog();
        ul.setTimestamp(LocalDateTime.now());
        ul.setName(user.getNome());
        ul.setEmail(user.getEmail());
        ul.setAction(action);
        userRepo.save(ul);
    }

    public void closeOpenLoginIfAny(User user) {
        UserLog last = userRepo.findFirstByEmailOrderByTimestampDesc(user.getEmail());
        if (last != null && "LOGIN".equals(last.getAction())) {
            UserLog fakeLogout = new UserLog();
            fakeLogout.setTimestamp(LocalDateTime.now());
            fakeLogout.setName(user.getNome());
            fakeLogout.setEmail(user.getEmail());
            fakeLogout.setAction("LOGOUT");
            userRepo.save(fakeLogout);
        }
    }

    /* =================== VEÍCULOS =================== */

    public void logCar(String email,
                       String action,
                       Car car,
                       int quantity,
                       String priceInfo) {

        CarLog cl = new CarLog();
        cl.setTimestamp(LocalDateTime.now());
        cl.setEmail(email);
        cl.setAction(action);

        if (car != null) {
            cl.setCarBrand(car.getBrand());
            cl.setCarModel(car.getModel());
        }

        cl.setQuantity(quantity);
        cl.setPrice(priceInfo);
        carRepo.save(cl);
    }

    /* =================== LEITURA DE LOGS =================== */

    public List<UserLog> listUserLogs() {
        return userRepo.findAllByOrderByTimestampDesc();
    }

    /** versão sem filtros – usada por outras partes do código */
    public List<CarLog> listCarLogs() {
        return carRepo.findAllByOrderByIdDesc();
    }

    /** versão COM filtros (email prefixo & ação exacta) */
    public List<CarLog> listCarLogs(String emailPrefix, String action) {
        String email  = emailPrefix == null ? "" : emailPrefix.toLowerCase(Locale.ROOT);
        String act    = action == null ? "" : action.toUpperCase(Locale.ROOT);

        return carRepo.findAllByOrderByIdDesc()
                .stream()
                .filter(cl -> email.isEmpty()
                        || cl.getEmail().toLowerCase(Locale.ROOT).startsWith(email))
                .filter(cl -> act.isEmpty() || cl.getAction().equals(act))
                .collect(Collectors.toList());
    }

    /* =================== LIMPEZA =================== */

    public void clearUserLogs() { userRepo.deleteAll(); }

    public void clearCarLogs()  { carRepo.deleteAll(); }
}
