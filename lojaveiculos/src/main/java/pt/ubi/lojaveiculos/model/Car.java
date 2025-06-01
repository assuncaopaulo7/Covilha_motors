package pt.ubi.lojaveiculos.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cars")
public class Car implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String model;
    private String category;
    private double price;
    private int stock;

    /* NOVO ─ caminho da imagem no disco */
    @Column(name = "image_path")
    private String imagePath;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sale> sales = new ArrayList<>();

    /* construtor vazio obrigatório */
    public Car() {}

    /* ---------- getters & setters ---------- */
    public Long getId()                   { return id; }
    public void setId(Long id)            { this.id = id; }

    public String getBrand()              { return brand; }
    public void setBrand(String brand)    { this.brand = brand; }

    public String getModel()              { return model; }
    public void setModel(String model)    { this.model = model; }

    public String getCategory()           { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice()              { return price; }
    public void setPrice(double price)    { this.price = price; }

    public int getStock()                 { return stock; }
    public void setStock(int stock)       { this.stock = stock; }

    public String getImagePath()          { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public List<Sale> getSales()          { return sales; }
    public void setSales(List<Sale> sales){ this.sales = sales; }
}
