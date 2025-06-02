package pt.ubi.lojaveiculos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import pt.ubi.lojaveiculos.dto.*;
import pt.ubi.lojaveiculos.model.Sale;

import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query("SELECT COALESCE(c.brand, 'Removido') AS brand, COUNT(s) AS count, SUM(s.total) AS total " +
            "FROM Sale s LEFT JOIN s.car c " +
            "GROUP BY c.brand " +
            "ORDER BY SUM(s.total) DESC")
    List<BrandSalesDTO> findSalesByBrand();

    @Query("SELECT FUNCTION('MONTH', s.date) AS month, FUNCTION('YEAR', s.date) AS year, " +
            "COUNT(s) AS count, SUM(s.quantity) AS quantity, SUM(s.total) AS total " +
            "FROM Sale s " +
            "GROUP BY FUNCTION('YEAR', s.date), FUNCTION('MONTH', s.date) " +
            "ORDER BY FUNCTION('YEAR', s.date), FUNCTION('MONTH', s.date)")
    List<MonthlySalesDTO> findMonthlySales();

    @Query("SELECT COALESCE(c.category, 'Sem Categoria') AS category, COUNT(s) AS count, SUM(s.total) AS total " +
            "FROM Sale s LEFT JOIN s.car c " +
            "GROUP BY c.category " +
            "ORDER BY SUM(s.total) DESC")
    List<CategorySalesDTO> findSalesByCategory();

    @Query("""
    SELECT COUNT(s.id) as totalVendas,
           COALESCE(SUM(s.total), 0) as totalArrecadado,
           (SELECT COUNT(c.id) FROM Car c) as carrosEmStock
    FROM Sale s
    """)
    SummaryDTO getResumoGeral();

    @Query("SELECT u.nome AS nome, COUNT(s) AS count, SUM(s.total) AS total " +
            "FROM Sale s JOIN s.user u " +
            "GROUP BY u.nome " +
            "ORDER BY SUM(s.total) DESC")
    List<TopClientDTO> findTopClients(Pageable pageable);

    List<Sale> findByDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT COUNT(s), COALESCE(SUM(s.total), 0) FROM Sale s")
    Object[] getTotalSalesAndRevenue();

    @Query("SELECT COUNT(DISTINCT s.user) FROM Sale s")
    Long countDistinctClients();

    @Query("SELECT COALESCE(AVG(s.total), 0) FROM Sale s")
    Double averageSaleValue();
}
