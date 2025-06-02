package pt.ubi.lojaveiculos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pt.ubi.lojaveiculos.model.UserLog;

import java.util.List;

public interface UserLogRepository extends JpaRepository<UserLog, Long> {

    List<UserLog> findAllByOrderByTimestampDesc();

    /** último registo deste email (ou null) */
    UserLog findFirstByEmailOrderByTimestampDesc(String email);

    /**
     * E-mails cujo ÚLTIMO registo é LOGIN (logo, ainda sem LOGOUT).
     * Funciona em MySQL, H2, Postgres, etc.
     */
    @Query("""
          SELECT ul.email
          FROM   UserLog ul
          WHERE  ul.id IN (
                 SELECT MAX(u2.id) FROM UserLog u2 GROUP BY u2.email)
          AND    ul.action = 'LOGIN'
          """)
    List<String> findEmailsWithOpenLogin();
}
