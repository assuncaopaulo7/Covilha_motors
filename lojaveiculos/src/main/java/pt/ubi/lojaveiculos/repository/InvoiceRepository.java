package pt.ubi.lojaveiculos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ubi.lojaveiculos.model.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> { }