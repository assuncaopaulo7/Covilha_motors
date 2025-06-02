package pt.ubi.lojaveiculos.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_logs")
public class UserLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private String name;
    private String email;
    private String action;      // LOGIN, LOGOUT, REGISTERED

    public UserLog() {}         // construtor sem args

    /* -------- getters & setters ---------- */
    public Long getId()                     { return id; }
    public void setId(Long id)              { this.id = id; }

    public LocalDateTime getTimestamp()     { return timestamp; }
    public void setTimestamp(LocalDateTime t) { this.timestamp = t; }

    public String getName()                 { return name; }
    public void setName(String name)        { this.name = name; }

    public String getEmail()                { return email; }
    public void setEmail(String email)      { this.email = email; }

    public String getAction()               { return action; }
    public void setAction(String action)    { this.action = action; }
}
