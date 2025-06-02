package pt.ubi.lojaveiculos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ubi.lojaveiculos.model.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> { }