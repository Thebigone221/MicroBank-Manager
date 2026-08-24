package sn.microbank.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Opération bancaire (dépôt, retrait ou virement) effectuée sur un compte
 * par un agent de l'institution.
 */
@Entity
@Table(name = "operation",
       uniqueConstraints = @UniqueConstraint(name = "uk_operation_reference", columnNames = "reference"))
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TypeOperation type;

    /** Montant toujours positif ; le type détermine le sens du mouvement. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(name = "date_operation", nullable = false)
    private LocalDateTime dateOperation;

    @Column(length = 200)
    private String description;

    /** Compte sur lequel l'opération est enregistrée (* --- 1). */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "compte_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_operation_compte"))
    private Account compte;

    /** Compte destination dans le cas d'un virement entrant. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "compte_destination_id",
                foreignKey = @ForeignKey(name = "fk_operation_compte_dest"))
    private Account compteDestination;

    /** Agent ayant réalisé l'opération : User 1 --- * Operation. */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "agent_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_operation_agent"))
    private User agent;

    public Operation() {
    }

    // Getters / Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public TypeOperation getType() {
        return type;
    }

    public void setType(TypeOperation type) {
        this.type = type;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public LocalDateTime getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(LocalDateTime dateOperation) {
        this.dateOperation = dateOperation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Account getCompte() {
        return compte;
    }

    public void setCompte(Account compte) {
        this.compte = compte;
    }

    public Account getCompteDestination() {
        return compteDestination;
    }

    public void setCompteDestination(Account compteDestination) {
        this.compteDestination = compteDestination;
    }

    public User getAgent() {
        return agent;
    }

    public void setAgent(User agent) {
        this.agent = agent;
    }
}
