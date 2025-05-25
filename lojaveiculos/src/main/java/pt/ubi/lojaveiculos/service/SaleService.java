package pt.ubi.lojaveiculos.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ubi.lojaveiculos.model.*;
import pt.ubi.lojaveiculos.repository.*;

import java.time.LocalDate;
import java.util.List;

@Service
public class SaleService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final SaleRepository saleRepository;
    private final InvoiceRepository invoiceRepository;

    public SaleService(CarRepository carRepository,
                       UserRepository userRepository,
                       SaleRepository saleRepository,
                       InvoiceRepository invoiceRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.saleRepository = saleRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public Invoice purchaseCar(Long carId, Long userId, int quantity) throws Exception {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new Exception("Carro não encontrado"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("Usuário não encontrado"));

        if (quantity <= 0 || quantity > car.getStock()) {
            throw new Exception("Quantidade inválida ou acima do stock disponível.");
        }

        double total = car.getPrice() * quantity;
        car.setStock(car.getStock() - quantity);

        Invoice invoice = new Invoice();
        invoice.setDate(LocalDate.now());
        invoice.setTotal(total);
        invoice.setUser(user);

        Sale sale = new Sale();
        sale.setDate(LocalDate.now());
        sale.setQuantity(quantity);
        sale.setTotal(total);
        sale.setCar(car);
        sale.setUser(user);
        sale.setInvoice(invoice);

        invoice.setSales(List.of(sale));

        carRepository.save(car);
        invoiceRepository.save(invoice); // cascade salva Sale
        // saleRepository.save(sale); // opcional

        return invoice;
    }
}