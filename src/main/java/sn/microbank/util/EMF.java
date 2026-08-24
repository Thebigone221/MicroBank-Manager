package sn.microbank.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

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
