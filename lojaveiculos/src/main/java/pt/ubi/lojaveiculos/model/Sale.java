package pt.ubi.lojaveiculos.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private int quantity;
    private double total;

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    public Sale() {}

    //‑‑ getters & setters (id, date, quantity, total, car, user, invoice)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public Car getCar() { return car; }
    public void setCar(Car car) { this.car = car; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }
}