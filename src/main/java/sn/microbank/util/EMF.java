package sn.microbank.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Point d'accès unique à l'EntityManagerFactory JPA.
 * <p>
 * L'EntityManagerFactory est créé une seule fois (singleton) au chargement
 * de la classe, à partir de META-INF/persistence.xml.
 * Chaque couche (DAO ou Service) crée ses EntityManager à la demande :
 * ils sont légers et non thread-safe, contrairement à la factory.
 */
public final class EMF {

    private static final EntityManagerFactory FACTORY =
            Persistence.createEntityManagerFactory("microbankPU");

    private EMF() {
    }

    public static EntityManager createEntityManager() {
        return FACTORY.createEntityManager();
    }

    public static void close() {
        FACTORY.close();
    }
}
