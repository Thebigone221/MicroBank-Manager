package sn.microbank.dao;

import sn.microbank.model.Agency;

import java.util.List;
import java.util.Optional;

/**
 * DAO de l'entité Agency (Bonus 4).
 */
public class AgencyDAO extends GenericDAO<Agency> {

    public AgencyDAO() {
        super(Agency.class);
    }

    @Override
    public List<Agency> findPage(int page, int size) {
        // Les agences sont peu nombreuses : tri alphabétique pour la liste.
        return inRead(em -> em.createQuery("SELECT a FROM Agency a ORDER BY a.nom", Agency.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList());
    }

    public List<Agency> findAllOrdered() {
        return inRead(em -> em.createQuery("SELECT a FROM Agency a ORDER BY a.nom", Agency.class).getResultList());
    }

    public boolean existsCode(String code, Long idExclu) {
        return inRead(em -> {
            Long n = em.createQuery(
                            "SELECT COUNT(a) FROM Agency a WHERE LOWER(a.code) = LOWER(:code) "
                                    + "AND (:idExclu IS NULL OR a.id <> :idExclu)", Long.class)
                    .setParameter("code", code.trim())
                    .setParameter("idExclu", idExclu)
                    .getSingleResult();
            return n > 0;
        });
    }

    public Optional<Agency> findByCode(String code) {
        return inRead(em -> em.createQuery(
                        "SELECT a FROM Agency a WHERE LOWER(a.code) = LOWER(:code)", Agency.class)
                .setParameter("code", code.trim())
                .getResultStream()
                .findFirst());
    }
}
