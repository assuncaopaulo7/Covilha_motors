package pt.ubi.lojaveiculos.model;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade Car – usa “soft-delete”.
 *  • @SQLDelete  -> UPDATE cars SET deleted = true WHERE id = ?
 *  • @Where      -> todas as leituras ignoram carros marcados como deleted=true
 */
@Entity
@Table(name = "cars")
@SQLDelete(sql = "UPDATE cars SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String model;
    private String category;
    private double price;
    private int stock;
    private String imagePath;

    /** Flag de remoção lógica: carros “apagados” deixam de aparecer na loja. */
    private boolean deleted = false;

    /* relação inversa – SEM cascata nem orphanRemoval */
    @OneToMany(mappedBy = "car", fetch = FetchType.LAZY)
    private List<Sale> sales = new ArrayList<>();

    /* ---------- getters & setters ---------- */
    public Long   getId()                     { return id; }
    public void   setId(Long id)              { this.id = id; }

    public String getBrand()                  { return brand; }
    public void   setBrand(String brand)      { this.brand = brand; }

    public String getModel()                  { return model; }
    public void   setModel(String model)      { this.model = model; }

    public String getCategory()               { return category; }
    public void   setCategory(String category){ this.category = category; }

    public double getPrice()                  { return price; }
    public void   setPrice(double price)      { this.price = price; }

    public int    getStock()                  { return stock; }
    public void   setStock(int stock)         { this.stock = stock; }

    public String getImagePath()              { return imagePath; }
    public void   setImagePath(String imagePath){ this.imagePath = imagePath; }

    public boolean isDeleted()                { return deleted; }
    public void   setDeleted(boolean deleted) { this.deleted = deleted; }

    public List<Sale> getSales()              { return sales; }
    public void   setSales(List<Sale> sales)  { this.sales = sales; }
}
