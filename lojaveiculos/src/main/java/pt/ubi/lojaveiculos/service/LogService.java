package pt.ubi.lojaveiculos.service;

import org.springframework.stereotype.Service;
import pt.ubi.lojaveiculos.model.*;
import pt.ubi.lojaveiculos.repository.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogService {

    private final UserLogRepository userRepo;
    private final CarLogRepository  carRepo;

    public LogService(UserLogRepository u, CarLogRepository c) {
        this.userRepo = u;
        this.carRepo  = c;
    }

    /* -------------------- UTILIZADORES -------------------- */
    public void logUser(User user, String action) {
        UserLog ul = new UserLog();
        ul.setTimestamp(LocalDateTime.now());
        ul.setName(user.getNome());
        ul.setEmail(user.getEmail());
        ul.setAction(action);
        userRepo.save(ul);
    }

    /* -------------------- VEÍCULOS ------------------------ */
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

    /* -------------------- LEITURA ------------------------- */
    public List<UserLog> listUserLogs() { return userRepo.findAllByOrderByTimestampDesc(); }
    public List<CarLog>  listCarLogs()  { return carRepo.findAllByOrderByTimestampDesc(); }

    /* -------------------- LIMPAR -------------------------- */
    public void clearUserLogs() { userRepo.deleteAll(); }
    public void clearCarLogs()  { carRepo.deleteAll(); }
}
