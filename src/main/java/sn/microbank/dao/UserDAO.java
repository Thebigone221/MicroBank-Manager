package sn.microbank.dao;

import sn.microbank.model.Statut;
import sn.microbank.model.User;

import java.util.Optional;

public class UserDAO extends GenericDAO<User> {

    public UserDAO() {
        super(User.class);
    }

    public Optional<User> findByLogin(String login) {
        return inRead(em -> em.createQuery(
                        "SELECT u FROM User u WHERE LOWER(u.login) = LOWER(:login)", User.class)
                .setParameter("login", login.trim())
                .getResultStream()
                .findFirst());
    }

    public boolean existsLogin(String login, Long idExclu) {
        return inRead(em -> {
            Long n = em.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE LOWER(u.login) = LOWER(:login) "
                                    + "AND (:idExclu IS NULL OR u.id <> :idExclu)", Long.class)
                    .setParameter("login", login.trim())
                    .setParameter("idExclu", idExclu)
                    .getSingleResult();
            return n > 0;
        });
    }

    public PagedResult<User> search(String terme, Statut statut, int page, int size) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        java.util.Map<String, Object> params = new java.util.HashMap<>();

        if (terme != null && !terme.isBlank()) {
            where.append(" AND (LOWER(u.nom) LIKE :motif OR LOWER(u.prenom) LIKE :motif OR LOWER(u.login) LIKE :motif)");
            params.put("motif", "%" + terme.toLowerCase().trim() + "%");
        }
        if (statut != null) {
            where.append(" AND u.statut = :statut");
            params.put("statut", statut);
        }

        String jpqlSelect = "SELECT u FROM User u" + where + " ORDER BY u.id DESC";
        String jpqlCount = "SELECT COUNT(u) FROM User u" + where;

        return inRead(em -> {
            var select = em.createQuery(jpqlSelect, User.class);
            var count = em.createQuery(jpqlCount, Long.class);
            params.forEach((nom, valeur) -> {
                select.setParameter(nom, valeur);
                count.setParameter(nom, valeur);
            });
            java.util.List<User> items = pagine(select, page, size).getResultList();
            long total = count.getSingleResult();
            return new PagedResult<>(items, total, page, size);
        });
    }
}
