package pt.ubi.lojaveiculos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ubi.lojaveiculos.model.CarLog;

import java.util.List;

public interface CarLogRepository extends JpaRepository<CarLog, Long> {
    List<CarLog> findAllByOrderByTimestampDesc();
}
