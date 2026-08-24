package sn.microbank.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Utilisateur de l'application (agent ou administrateur).
 */
@Entity
@Table(name = "app_user",
       uniqueConstraints = @UniqueConstraint(name = "uk_user_login", columnNames = "login"))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nom;

    @Column(nullable = false, length = 60)
    private String prenom;

    @Column(nullable = false, length = 40)
    private String login;

    /** Mot de passe hashé en SHA-256 : jamais stocké ni affiché en clair. */
    @Column(name = "mot_de_passe", nullable = false, length = 64)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Statut statut;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    public User() {
    }

    public User(String nom, String prenom, String login, String motDePasseHash, Role role) {
        this.nom = nom;
        this.prenom = prenom;
        this.login = login;
        this.motDePasse = motDePasseHash;
        this.role = role;
        this.statut = Statut.ACTIF;
        this.dateCreation = LocalDateTime.now();
    }

    /** Nom complet affichable. */
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

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}
