package pt.ubi.lojaveiculos.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "car_logs")
public class CarLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private String email;
    private String action;      // COMPROU, INSERIU, RETIROU, ELIMINOU
    private String carBrand;
    private String carModel;
    private int    quantity;

    public CarLog() {}

    /* -------- getters & setters ---------- */
    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public LocalDateTime getTimestamp()        { return timestamp; }
    public void setTimestamp(LocalDateTime t)  { this.timestamp = t; }

    public String getEmail()                   { return email; }
    public void setEmail(String email)         { this.email = email; }

    public String getAction()                  { return action; }
    public void setAction(String action)       { this.action = action; }

    public String getCarBrand()                { return carBrand; }
    public void setCarBrand(String carBrand)   { this.carBrand = carBrand; }

    public String getCarModel()                { return carModel; }
    public void setCarModel(String carModel)   { this.carModel = carModel; }

    public int getQuantity()                   { return quantity; }
    public void setQuantity(int quantity)      { this.quantity = quantity; }
}
