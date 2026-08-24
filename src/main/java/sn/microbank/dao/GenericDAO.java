package sn.microbank.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import sn.microbank.util.EMF;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * DAO générique fournissant le CRUD et une pagination réalisée en JPA
 * (setFirstResult / setMaxResults).
 *
 * @param <T> entité gérée
 */
public abstract class GenericDAO<T> {

    private final Class<T> entityClass;

    protected GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Exécute une opération métier dans une transaction :
     * l'EntityManager est créé puis fermé automatiquement.
     */
    public static <R> R inTransaction(Function<EntityManager, R> travail) {
        try (EntityManager em = EMF.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                R resultat = travail.apply(em);
                tx.commit();
                return resultat;
            } catch (RuntimeException e) {
                // Toute erreur provoque un ROLLBACK complet
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }

    /** Variante sans valeur de retour pour les traitements transactionnels. */
    public static void inTransactionVoid(Consumer<EntityManager> travail) {
        inTransaction(em -> {
            travail.accept(em);
            return null;
        });
    }

    /** Exécution en lecture seule (pas de transaction). */
    public static <R> R inRead(Function<EntityManager, R> lecture) {
        try (EntityManager em = EMF.createEntityManager()) {
            return lecture.apply(em);
        }
    }

    public T findById(Long id) {
        return inRead(em -> em.find(entityClass, id));
    }

    /** Liste paginée de toutes les entités — pagination JPA obligatoire. */
    public List<T> findPage(int page, int size) {
        return inRead(em -> em.createQuery(
                        "SELECT e FROM " + entityClass.getSimpleName() + " e ORDER BY e.id DESC",
                        entityClass)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList());
    }

    public long count() {
        return inRead(em -> em.createQuery(
                "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class).getSingleResult());
    }

    public T save(T entite) {
        return inTransaction(em -> {
            em.persist(entite);
            return entite;
        });
    }

    public T update(T entite) {
        return inTransaction(em -> em.merge(entite));
    }

    public void delete(T entite) {
        inTransactionVoid(em -> {
            T geree = em.contains(entite) ? entite : em.merge(entite);
            em.remove(geree);
        });
    }

    public void deleteById(Long id) {
        inTransactionVoid(em -> {
            T entite = em.find(entityClass, id);
            if (entite != null) {
                em.remove(entite);
            }
        });
    }

    /** Construit une requête paginée (utilitaire partagé par les DAO fils). */
    protected static <R> TypedQuery<R> pagine(TypedQuery<R> query, int page, int size) {
        return query.setFirstResult(page * size).setMaxResults(size);
    }
}
