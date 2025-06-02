package pt.ubi.lojaveiculos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pt.ubi.lojaveiculos.model.Car;

import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {

    List<Car> findByCategory(String category);

    List<Car> findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(String brand, String model);

    List<Car> findByBrandContainingIgnoreCase(String brand);
    List<Car> findByModelContainingIgnoreCase(String model);
    List<Car> findByCategoryContainingIgnoreCase(String category);

    // métodos para faixa de preço (≤ e ≥)
    List<Car> findByPriceLessThanEqual(double price);
    List<Car> findByPriceGreaterThanEqual(double price);

    // métodos para faixa de stock
    List<Car> findByStockLessThanEqual(int stock);
    List<Car> findByStockGreaterThanEqual(int stock);


    @Query("SELECT c FROM Car c WHERE c.deleted = false")
    List<Car> findAllActive();
}