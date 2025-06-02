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

    public LogService(UserLogRepository userRepo,
                      CarLogRepository carRepo) {
        this.userRepo = userRepo;
        this.carRepo  = carRepo;
    }

    /* =================== UTILIZADORES =================== */

    /** Regista uma ação de login/logout/register */
    public void logUser(User user, String action) {
        UserLog ul = new UserLog();
        ul.setTimestamp(LocalDateTime.now());
        ul.setName(user.getNome());
        ul.setEmail(user.getEmail());
        ul.setAction(action);
        userRepo.save(ul);
    }

    /**
     * Se o último registo deste utilizador foi LOGIN e ainda
     * não existe um LOGOUT correspondente (por falha de rede,
     * encerramento forçado, etc.), grava agora um LOGOUT.
     */
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

    /**
     * Regista uma ação de veículo (CRIAR, INSERIR, ELIMINAR, etc.)
     * @param email     email do utilizador (normalmente admin)
     * @param action    tipo de ação ("INSERIU", "RETIROU", etc.)
     * @param car       veículo afetado
     * @param quantity  quantidade afetada
     * @param priceInfo string de preço (ex: "1500" ou "1800 -> 1700")
     */
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

    /** Logs de utilizador – ordem descendente por tempo */
    public List<UserLog> listUserLogs() {
        return userRepo.findAllByOrderByTimestampDesc();
    }

    /** Logs de veículos – ordem descendente por ID (garante ordem real de inserção) */
    public List<CarLog> listCarLogs() {
        return carRepo.findAllByOrderByIdDesc();
    }

    /* =================== LIMPEZA =================== */

    public void clearUserLogs() {
        userRepo.deleteAll();
    }

    public void clearCarLogs() {
        carRepo.deleteAll();
    }
}
