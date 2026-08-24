package sn.microbank.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Agence de l'institution — Bonus 4 : les comptes sont rattachés aux agences.
 */
@Entity
@Table(name = "agency",
       uniqueConstraints = @UniqueConstraint(name = "uk_agency_code", columnNames = "code"))
public class Agency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false, length = 60)
    private String nom;

    @Column(length = 60)
    private String ville;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "agency")
    private List<Account> accounts = new ArrayList<>();

    public Agency() {
    }

    public Agency(String code, String nom, String ville) {
        this.code = code;
        this.nom = nom;
        this.ville = ville;
    }

    // Getters / Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public List<Account> getAccounts() {
        return accounts;
    }
}
