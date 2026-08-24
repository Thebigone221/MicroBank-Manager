package sn.microbank.service;

import sn.microbank.dao.AccountDAO;
import sn.microbank.dao.GenericDAO;
import sn.microbank.dao.OperationDAO;
import sn.microbank.dao.PagedResult;
import sn.microbank.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OperationService {

    private final AccountDAO accountDAO = new AccountDAO();
    private final OperationDAO operationDAO = new OperationDAO();

    public void effectuerDepot(Long accountId, BigDecimal montant, User agent, String description) {
        GenericDAO.inTransaction(em -> {
            Account compte = em.find(Account.class, accountId);
            verifierCompte(compte, "destination du dépôt");
            verifierMontant(montant);

            compte.setSolde(compte.getSolde().add(montant));

            Operation operation = new Operation();
            operation.setReference(genererReference(em));
            operation.setType(TypeOperation.DEPOT);
            operation.setMontant(montant);
            operation.setDateOperation(LocalDateTime.now());
            operation.setDescription(description == null || description.isBlank()
                    ? "Dépôt en espèces" : description.trim());
            operation.setCompte(compte);
            operation.setAgent(agent);
            em.persist(operation);
            return null;
        });
    }

    public void effectuerRetrait(Long accountId, BigDecimal montant, User agent, String description) {
        GenericDAO.inTransaction(em -> {
            Account compte = em.find(Account.class, accountId);
            verifierCompte(compte, "du retrait");
            verifierMontant(montant);

            if (compte.getSolde().compareTo(montant) < 0) {
                throw new ServiceException("Solde insuffisant : solde actuel = "
                        + compte.getSolde().toPlainString() + " FCFA.");
            }

            compte.setSolde(compte.getSolde().subtract(montant));

            Operation operation = new Operation();
            operation.setReference(genererReference(em));
            operation.setType(TypeOperation.RETRAIT);
            operation.setMontant(montant);
            operation.setDateOperation(LocalDateTime.now());
            operation.setDescription(description == null || description.isBlank()
                    ? "Retrait en espèces" : description.trim());
            operation.setCompte(compte);
            operation.setAgent(agent);
            em.persist(operation);
            return null;
        });
    }

    public void effectuerVirement(Long sourceId, Long destinationId, BigDecimal montant,
                                  User agent, String description) {
        if (sourceId != null && sourceId.equals(destinationId)) {
            throw new ServiceException("Les comptes source et destination doivent être différents.");
        }
        verifierMontant(montant);

        GenericDAO.inTransaction(em -> {
            Account source = em.find(Account.class, sourceId);
            Account destination = em.find(Account.class, destinationId);
            verifierCompte(source, "source du virement");
            verifierCompte(destination, "destination du virement");

            if (comptesIdentiques(source, destination)) {
                throw new ServiceException("Les comptes source et destination doivent être différents.");
            }
            if (source.getSolde().compareTo(montant) < 0) {
                throw new ServiceException("Solde insuffisant sur le compte source : "
                        + source.getSolde().toPlainString() + " FCFA.");
            }

            LocalDateTime maintenant = LocalDateTime.now();

            source.setSolde(source.getSolde().subtract(montant));
            Operation sortie = new Operation();
            sortie.setReference(genererReference(em));
            sortie.setType(TypeOperation.VIREMENT);
            sortie.setMontant(montant);
            sortie.setDateOperation(maintenant);
            sortie.setDescription((description == null || description.isBlank()
                    ? "Virement" : description.trim()) + " → " + destination.getNumeroCompte());
            sortie.setCompte(source);
            sortie.setAgent(agent);
            em.persist(sortie);

            destination.setSolde(destination.getSolde().add(montant));
            Operation entree = new Operation();
            entree.setReference(genererReference(em));
            entree.setType(TypeOperation.VIREMENT);
            entree.setMontant(montant);
            entree.setDateOperation(maintenant);
            entree.setDescription((description == null || description.isBlank()
                    ? "Virement" : description.trim()) + " ← " + source.getNumeroCompte());
            entree.setCompte(destination);
            entree.setCompteDestination(source);
            entree.setAgent(agent);
            em.persist(entree);

            return null;
        });
    }

    private boolean comptesIdentiques(Account a, Account b) {
        return a.getId() != null && a.getId().equals(b.getId());
    }

    private static String genererReference(jakarta.persistence.EntityManager em) {
        long maxId = em.createQuery("SELECT COALESCE(MAX(o.id), 0) FROM Operation o", Long.class)
                .getSingleResult();
        return String.format("OP-%05d", maxId + 1);
    }

    private void verifierMontant(BigDecimal montant) {
        if (montant == null) {
            throw new ServiceException("Le montant est obligatoire.");
        }
        if (montant.signum() <= 0) {
            throw new ServiceException("Le montant doit être strictement positif.");
        }
    }

    private void verifierCompte(Account compte, String role) {
        if (compte == null) {
            throw new ServiceException("Compte " + role + " introuvable.");
        }
        if (!compte.isActif()) {
            throw new ServiceException(
                    "Le compte " + role + " (" + compte.getNumeroCompte() + ") n'est pas actif.");
        }
    }

    public PagedResult<Operation> historique(Long accountId, Long clientId, String numeroCompte,
                                             TypeOperation type, LocalDateTime du, LocalDateTime au,
                                             BigDecimal montantMin, BigDecimal montantMax,
                                             int page, int size) {
        return operationDAO.search(accountId, clientId, numeroCompte, type, du, au,
                montantMin, montantMax, page, size);
    }

    public OperationDAO.Totaux totaux(Long accountId, LocalDateTime du, LocalDateTime au) {
        return operationDAO.totauxParType(accountId, du, au);
    }

    public List<Operation> dernieres(int limite) {
        return operationDAO.findLatest(limite);
    }

    public long countDepuis(LocalDateTime date) {
        return operationDAO.countSince(date);
    }
}
