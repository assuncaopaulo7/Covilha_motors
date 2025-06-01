package pt.ubi.lojaveiculos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ubi.lojaveiculos.model.UserLog;

import java.util.List;

public interface UserLogRepository extends JpaRepository<UserLog, Long> {
    List<UserLog> findAllByOrderByTimestampDesc();
}
