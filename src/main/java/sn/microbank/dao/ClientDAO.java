package sn.microbank.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import sn.microbank.model.Client;
import sn.microbank.model.Statut;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientDAO extends GenericDAO<Client> {

    public ClientDAO() {
        super(Client.class);
    }

    public PagedResult<Client> search(String terme, Statut statut, int page, int size) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        Map<String, Object> params = new HashMap<>();

        if (terme != null && !terme.isBlank()) {
            where.append(" AND (LOWER(c.nom) LIKE :motif OR LOWER(c.prenom) LIKE :motif "
                    + "OR c.telephone LIKE :motif OR LOWER(c.numeroPiece) LIKE :motif)");
            params.put("motif", "%" + terme.toLowerCase().trim() + "%");
        }
        if (statut != null) {
            where.append(" AND c.statut = :statut");
            params.put("statut", statut);
        }

        String jpqlSelect = "SELECT c FROM Client c" + where + " ORDER BY c.id DESC";
        String jpqlCount = "SELECT COUNT(c) FROM Client c" + where;

        return inRead(em -> {
            TypedQuery<Client> select = em.createQuery(jpqlSelect, Client.class);
            TypedQuery<Long> count = em.createQuery(jpqlCount, Long.class);
            params.forEach((nom, valeur) -> {
                select.setParameter(nom, valeur);
                count.setParameter(nom, valeur);
            });

            List<Client> items = pagine(select, page, size).getResultList();
            long total = count.getSingleResult();
            return new PagedResult<>(items, total, page, size);
        });
    }

    public boolean existsNumeroPiece(String numeroPiece, Long idExclu) {
        return inRead(em -> {
            Long n = em.createQuery(
                            "SELECT COUNT(c) FROM Client c WHERE LOWER(c.numeroPiece) = LOWER(:piece) "
                                    + "AND (:idExclu IS NULL OR c.id <> :idExclu)", Long.class)
                    .setParameter("piece", numeroPiece.trim())
                    .setParameter("idExclu", idExclu)
                    .getSingleResult();
            return n > 0;
        });
    }

    public long countByStatut(Statut statut) {
        return inRead(em -> em.createQuery(
                        "SELECT COUNT(c) FROM Client c WHERE c.statut = :statut", Long.class)
                .setParameter("statut", statut)
                .getSingleResult());
    }

    public long countCreatedSince(LocalDateTime date) {
        return inRead((EntityManager em) -> em.createQuery(
                        "SELECT COUNT(c) FROM Client c WHERE c.dateCreation >= :date", Long.class)
                .setParameter("date", date)
                .getSingleResult());
    }
}
