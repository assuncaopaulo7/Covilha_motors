package pt.ubi.lojaveiculos.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidade Sale – a coluna car_id é agora opcional para que
 * as vendas sobrevivam mesmo depois do carro ser removido.
 */
@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private int quantity;
    private double total;

    /** relação MANY-TO-ONE opcional (nullable=true) */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = true)
    private Car car;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    /* ---------- getters & setters ---------- */
    public Long getId()              { return id; }
    public void setId(Long id)       { this.id = id; }

    public LocalDate getDate()       { return date; }
    public void setDate(LocalDate d) { this.date = d; }

    public int getQuantity()         { return quantity; }
    public void setQuantity(int q)   { this.quantity = q; }

    public double getTotal()         { return total; }
    public void setTotal(double t)   { this.total = t; }

    public Car getCar()              { return car; }
    public void setCar(Car car)      { this.car = car; }

    public User getUser()            { return user; }
    public void setUser(User user)   { this.user = user; }

    public Invoice getInvoice()          { return invoice; }
    public void setInvoice(Invoice inv)  { this.invoice = inv; }
}
