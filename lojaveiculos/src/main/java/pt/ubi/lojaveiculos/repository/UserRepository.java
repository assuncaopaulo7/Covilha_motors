package pt.ubi.lojaveiculos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ubi.lojaveiculos.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}