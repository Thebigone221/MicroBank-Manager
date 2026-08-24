package sn.microbank.dao;

import sn.microbank.model.Operation;
import sn.microbank.model.TypeOperation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO de l'entité Operation : historique filtré et paginé en JPA.
 */
public class OperationDAO extends GenericDAO<Operation> {

    public OperationDAO() {
        super(Operation.class);
    }

    /**
     * Recherche paginée de l'historique avec filtres combinables
     * (Bonus 2 : compte, client, type, période, bornes de montant).
     */
    public PagedResult<Operation> search(Long accountId, Long clientId, String numeroCompte,
                                         TypeOperation type, LocalDateTime du, LocalDateTime au,
                                         BigDecimal montantMin, BigDecimal montantMax,
                                         int page, int size) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        Map<String, Object> params = new HashMap<>();

        if (accountId != null) {
            where.append(" AND o.compte.id = :accountId");
            params.put("accountId", accountId);
        }
        if (clientId != null) {
            where.append(" AND (o.compte.client.id = :clientId OR o.compteDestination.client.id = :clientId)");
            params.put("clientId", clientId);
        }
        if (numeroCompte != null && !numeroCompte.isBlank()) {
            where.append(" AND LOWER(o.compte.numeroCompte) LIKE :motif");
            params.put("motif", "%" + numeroCompte.toLowerCase().trim() + "%");
        }
        if (type != null) {
            where.append(" AND o.type = :type");
            params.put("type", type);
        }
        if (du != null) {
            where.append(" AND o.dateOperation >= :du");
            params.put("du", du);
        }
        if (au != null) {
            where.append(" AND o.dateOperation <= :au");
            params.put("au", au);
        }
        if (montantMin != null) {
            where.append(" AND o.montant >= :montantMin");
            params.put("montantMin", montantMin);
        }
        if (montantMax != null) {
            where.append(" AND o.montant <= :montantMax");
            params.put("montantMax", montantMax);
        }

        String jpqlSelect = "SELECT o FROM Operation o" + where + " ORDER BY o.dateOperation DESC, o.id DESC";
        String jpqlCount = "SELECT COUNT(o) FROM Operation o" + where;

        return inRead(em -> {
            var select = em.createQuery(jpqlSelect, Operation.class);
            var count = em.createQuery(jpqlCount, Long.class);
            params.forEach((nom, valeur) -> {
                select.setParameter(nom, valeur);
                count.setParameter(nom, valeur);
            });
            List<Operation> items = pagine(select, page, size).getResultList();
            long total = count.getSingleResult();
            return new PagedResult<>(items, total, page, size);
        });
    }

    /** Totaux des dépôts / retraits d'un compte sur une période (pour le relevé PDF). */
    public record Totaux(BigDecimal depots, BigDecimal retraits) {
    }

    public Totaux totauxParType(Long accountId, LocalDateTime du, LocalDateTime au) {
        return inRead(em -> {
            StringBuilder where = new StringBuilder(" WHERE o.compte.id = :accountId AND o.type IN (:depot, :retrait)");
            Map<String, Object> params = new HashMap<>();
            params.put("accountId", accountId);
            params.put("depot", TypeOperation.DEPOT);
            params.put("retrait", TypeOperation.RETRAIT);
            if (du != null) {
                where.append(" AND o.dateOperation >= :du");
                params.put("du", du);
            }
            if (au != null) {
                where.append(" AND o.dateOperation <= :au");
                params.put("au", au);
            }

            var queryDepot = em.createQuery(
                    "SELECT COALESCE(SUM(o.montant), 0) FROM Operation o" + where + " AND o.type = :depot",
                    BigDecimal.class);
            var queryRetrait = em.createQuery(
                    "SELECT COALESCE(SUM(o.montant), 0) FROM Operation o" + where + " AND o.type = :retrait",
                    BigDecimal.class);
            params.forEach((nom, valeur) -> {
                queryDepot.setParameter(nom, valeur);
                queryRetrait.setParameter(nom, valeur);
            });

            return new Totaux(queryDepot.getSingleResult(), queryRetrait.getSingleResult());
        });
    }

    /** Nombre d'opérations réalisées aujourd'hui (tableau de bord). */
    public long countSince(LocalDateTime date) {
        return inRead(em -> em.createQuery(
                        "SELECT COUNT(o) FROM Operation o WHERE o.dateOperation >= :date", Long.class)
                .setParameter("date", date)
                .getSingleResult());
    }

    /** Dernières opérations (tableau de bord amélioré — Bonus 3). */
    public List<Operation> findLatest(int limite) {
        return inRead(em -> em.createQuery(
                        "SELECT o FROM Operation o ORDER BY o.dateOperation DESC, o.id DESC",
                        Operation.class)
                .setMaxResults(limite)
                .getResultList());
    }
}
