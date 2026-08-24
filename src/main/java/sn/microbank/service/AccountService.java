package sn.microbank.service;

import sn.microbank.dao.AccountDAO;
import sn.microbank.dao.AgencyDAO;
import sn.microbank.dao.ClientDAO;
import sn.microbank.dao.GenericDAO;
import sn.microbank.dao.PagedResult;
import sn.microbank.model.*;
import sn.microbank.util.ValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service de gestion des comptes bancaires.
 * L'ouverture d'un compte (création + dépôt initial) est atomique.
 */
public class AccountService {

    private final AccountDAO accountDAO = new AccountDAO();
    private final ClientDAO clientDAO = new ClientDAO();
    private final AgencyDAO agencyDAO = new AgencyDAO();

    /**
     * Ouvre un compte pour un client, avec un dépôt initial facultatif.
     * La création du compte ET le dépôt initial se font dans UNE SEULE
     * transaction : en cas d'erreur rien n'est conservé.
     *
     * @return le compte créé
     */
    public Account ouvrirCompte(Long clientId, TypeCompte type, BigDecimal depotInitial,
                                Long agenceId, User agent) {
        if (clientId == null) {
            throw new ServiceException("Veuillez sélectionner un client.");
        }
        if (type == null) {
            throw new ServiceException("Veuillez choisir un type de compte (courant ou épargne).");
        }
        if (depotInitial != null && depotInitial.signum() < 0) {
            throw new ServiceException("Le dépôt initial ne peut pas être négatif.");
        }

        return GenericDAO.inTransaction(em -> {
            Client client = em.find(Client.class, clientId);
            if (client == null) {
                throw new ServiceException("Client introuvable.");
            }
            if (client.getStatut() != Statut.ACTIF) {
                throw new ServiceException("Impossible d'ouvrir un compte pour un client inactif.");
            }

            Agency agence = agenceId == null ? null : em.find(Agency.class, agenceId);

            Account compte = new Account();
            compte.setClient(client);
            compte.setType(type);
            compte.setAgency(agence);
            compte.setDateOuverture(LocalDate.now());
            compte.setStatut(CompteStatut.ACTIF);
            compte.setNumeroCompte(genererNumeroCompte(em));

            if (depotInitial == null || depotInitial.signum() == 0) {
                compte.setSolde(BigDecimal.ZERO);
                em.persist(compte);
                return compte;
            }

            // Dépôt initial enregistré comme une vraie opération.
            compte.setSolde(depotInitial);
            em.persist(compte);
            em.flush();

            Operation depot = new Operation();
            depot.setReference(genererReference(em));
            depot.setType(TypeOperation.DEPOT);
            depot.setMontant(depotInitial);
            depot.setDateOperation(java.time.LocalDateTime.now());
            depot.setDescription("Dépôt initial à l'ouverture du compte");
            depot.setCompte(compte);
            depot.setAgent(agent);
            em.persist(depot);

            return compte;
        });
    }

    /** Génère un numéro de compte unique de la forme MB100001. */
    private String genererNumeroCompte(jakarta.persistence.EntityManager em) {
        long total = em.createQuery("SELECT COUNT(a) FROM Account a", Long.class).getSingleResult();
        long maxId = em.createQuery("SELECT COALESCE(MAX(a.id), 0) FROM Account a", Long.class).getSingleResult();
        long base = Math.max(total, maxId);
        for (long i = 1; i <= 1000; i++) {
            String candidat = String.format("MB%06d", 100000 + base + i);
            Long n = em.createQuery("SELECT COUNT(a) FROM Account a WHERE a.numeroCompte = :numero", Long.class)
                    .setParameter("numero", candidat)
                    .getSingleResult();
            if (n == 0) {
                return candidat;
            }
        }
        throw new ServiceException("Impossible de générer un numéro de compte.");
    }

    /** Génère une référence d'opération unique de la forme OP-00001. */
    static String genererReference(jakarta.persistence.EntityManager em) {
        long maxId = em.createQuery("SELECT COALESCE(MAX(o.id), 0) FROM Operation o", Long.class).getSingleResult();
        return String.format("OP-%05d", maxId + 1);
    }

    /**
     * Valide le formulaire d'ouverture de compte.
     */
    public Map<String, String> validerOuverture(String clientId, String type, String depotInitial) {
        Map<String, String> erreurs = ValidationUtil.nouvellesErreurs();
        ValidationUtil.requis(erreurs, "clientId", clientId, "Veuillez sélectionner un client.");
        ValidationUtil.requis(erreurs, "type", type, "Veuillez choisir un type de compte.");
        if (depotInitial != null && !depotInitial.isBlank()) {
            try {
                BigDecimal montant = new BigDecimal(depotInitial.trim());
                if (montant.signum() < 0) {
                    erreurs.put("depotInitial", "Le dépôt initial ne peut pas être négatif.");
                }
            } catch (NumberFormatException e) {
                erreurs.put("depotInitial", "Montant invalide.");
            }
        }
        return erreurs;
    }

    public Account findById(Long id) {
        return accountDAO.findById(id);
    }

    public List<Account> findByClient(Long clientId) {
        return accountDAO.findByClient(clientId);
    }

    public PagedResult<Account> lister(String terme, TypeCompte type, CompteStatut statut,
                                       Long agenceId, Long clientId, int page, int size) {
        return accountDAO.search(terme, type, statut, agenceId, clientId, page, size);
    }

    /** Change le statut d'un compte (blocage / clôture / réactivation). */
    public Account changerStatut(Long id, CompteStatut nouveauStatut) {
        return GenericDAO.inTransaction(em -> {
            Account compte = em.find(Account.class, id);
            if (compte == null) {
                throw new ServiceException("Compte introuvable.");
            }
            if (nouveauStatut == CompteStatut.CLOTURE && compte.getSolde().signum() > 0) {
                throw new ServiceException(
                        "Impossible de clôturer : le solde doit être nul. Effectuez un retrait ou un virement d'abord.");
            }
            compte.setStatut(nouveauStatut);
            return compte;
        });
    }

    public List<Agency> toutesAgences() {
        return agencyDAO.findAllOrdered();
    }
}
