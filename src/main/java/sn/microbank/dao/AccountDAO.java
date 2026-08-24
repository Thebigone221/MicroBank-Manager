package sn.microbank.dao;

import sn.microbank.model.Account;
import sn.microbank.model.CompteStatut;
import sn.microbank.model.TypeCompte;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AccountDAO extends GenericDAO<Account> {

    public AccountDAO() {
        super(Account.class);
    }

    public PagedResult<Account> search(String terme, TypeCompte type, CompteStatut statut,
                                       Long agenceId, Long clientId, int page, int size) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        Map<String, Object> params = new HashMap<>();

        if (terme != null && !terme.isBlank()) {
            where.append(" AND (LOWER(a.numeroCompte) LIKE :motif "
                    + "OR LOWER(a.client.nom) LIKE :motif OR LOWER(a.client.prenom) LIKE :motif)");
            params.put("motif", "%" + terme.toLowerCase().trim() + "%");
        }
        if (type != null) {
            where.append(" AND a.type = :type");
            params.put("type", type);
        }
        if (statut != null) {
            where.append(" AND a.statut = :statut");
            params.put("statut", statut);
        }
        if (agenceId != null) {
            where.append(" AND a.agency.id = :agenceId");
            params.put("agenceId", agenceId);
        }
        if (clientId != null) {
            where.append(" AND a.client.id = :clientId");
            params.put("clientId", clientId);
        }

        String jpqlSelect = "SELECT a FROM Account a" + where + " ORDER BY a.id DESC";
        String jpqlCount = "SELECT COUNT(a) FROM Account a" + where;

        return inRead(em -> {
            var select = em.createQuery(jpqlSelect, Account.class);
            var count = em.createQuery(jpqlCount, Long.class);
            params.forEach((nom, valeur) -> {
                select.setParameter(nom, valeur);
                count.setParameter(nom, valeur);
            });
            List<Account> items = pagine(select, page, size).getResultList();
            long total = count.getSingleResult();
            return new PagedResult<>(items, total, page, size);
        });
    }

    public Optional<Account> findByNumero(String numeroCompte) {
        return inRead(em -> em.createQuery(
                        "SELECT a FROM Account a WHERE a.numeroCompte = :numero", Account.class)
                .setParameter("numero", numeroCompte)
                .getResultStream()
                .findFirst());
    }

    public List<Account> findByClient(Long clientId) {
        return inRead(em -> em.createQuery(
                        "SELECT a FROM Account a WHERE a.client.id = :clientId ORDER BY a.id DESC", Account.class)
                .setParameter("clientId", clientId)
                .getResultList());
    }

    public long countByStatut(CompteStatut statut) {
        return inRead(em -> em.createQuery(
                        "SELECT COUNT(a) FROM Account a WHERE a.statut = :statut", Long.class)
                .setParameter("statut", statut)
                .getSingleResult());
    }

    public BigDecimal sumSoldeActifs() {
        return inRead(em -> em.createQuery(
                        "SELECT COALESCE(SUM(a.solde), 0) FROM Account a WHERE a.statut = :statut",
                        BigDecimal.class)
                .setParameter("statut", CompteStatut.ACTIF)
                .getSingleResult());
    }

    public Map<TypeCompte, Long> countByType() {
        return inRead(em -> {
            Map<TypeCompte, Long> resultat = new HashMap<>();
            em.createQuery(
                            "SELECT a.type, COUNT(a) FROM Account a GROUP BY a.type", Object[].class)
                    .getResultList()
                    .forEach(ligne -> resultat.put((TypeCompte) ligne[0], (Long) ligne[1]));
            return resultat;
        });
    }
}
