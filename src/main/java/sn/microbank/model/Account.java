package sn.microbank.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account",
       uniqueConstraints = @UniqueConstraint(name = "uk_account_numero", columnNames = "numero_compte"))
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_compte", nullable = false, length = 20)
    private String numeroCompte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TypeCompte type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal solde = BigDecimal.ZERO;

    @Column(name = "date_ouverture", nullable = false)
    private LocalDate dateOuverture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CompteStatut statut;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "client_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_account_client"))
    private Client client;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agency_id",
                foreignKey = @ForeignKey(name = "fk_account_agency"))
    private Agency agency;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "compte", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Operation> operations = new ArrayList<>();

    public Account() {
    }

    public boolean isActif() {
        return statut == CompteStatut.ACTIF;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }

    public TypeCompte getType() {
        return type;
    }

    public void setType(TypeCompte type) {
        this.type = type;
    }

    public BigDecimal getSolde() {
        return solde;
    }

    public void setSolde(BigDecimal solde) {
        this.solde = solde;
    }

    public LocalDate getDateOuverture() {
        return dateOuverture;
    }

    public void setDateOuverture(LocalDate dateOuverture) {
        this.dateOuverture = dateOuverture;
    }

    public CompteStatut getStatut() {
        return statut;
    }

    public void setStatut(CompteStatut statut) {
        this.statut = statut;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Agency getAgency() {
        return agency;
    }

    public void setAgency(Agency agency) {
        this.agency = agency;
    }

    public List<Operation> getOperations() {
        return operations;
    }
}
