package sn.microbank.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Client de l'institution de microfinance.
 */
@Entity
@Table(name = "client",
       uniqueConstraints = @UniqueConstraint(name = "uk_client_numero_piece", columnNames = "numero_piece"))
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nom;

    @Column(nullable = false, length = 60)
    private String prenom;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(nullable = false, length = 20)
    private String telephone;

    @Column(length = 80)
    private String email;

    @Column(length = 150)
    private String adresse;

    @Column(name = "numero_piece", nullable = false, length = 40)
    private String numeroPiece;

    /** Chemin du fichier (copie de la pièce d'identité) uploadé — Bonus 1. */
    @Column(name = "piece_identite", length = 255)
    private String pieceIdentite;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Statut statut;

    /** Relation JPA : un client possède plusieurs comptes (1 --- *). */
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();

    public Client() {
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    // Getters / Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getNumeroPiece() {
        return numeroPiece;
    }

    public void setNumeroPiece(String numeroPiece) {
        this.numeroPiece = numeroPiece;
    }

    public String getPieceIdentite() {
        return pieceIdentite;
    }

    public void setPieceIdentite(String pieceIdentite) {
        this.pieceIdentite = pieceIdentite;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public List<Account> getAccounts() {
        return accounts;
    }
}
