package pt.ubi.lojaveiculos.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private double total;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sale> sales = new ArrayList<>();

    public Invoice() {}

    //‑‑ getters & setters (id, date, total, user, sales)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Sale> getSales() { return sales; }
    public void setSales(List<Sale> sales) { this.sales = sales; }
}