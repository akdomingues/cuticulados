package org.cuticulados.pm.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {

    private static final String PERSISTENCE_UNIT = "cuticulados";
    private static EntityManagerFactory emf;

    private JpaUtil() {}

    public static void inicializar() {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
        }
    }

    public static EntityManager getEntityManager() {
        if (emf == null || !emf.isOpen()) {
            throw new IllegalStateException("JpaUtil não inicializado. Chame JpaUtil.inicializar() primeiro.");
        }
        return emf.createEntityManager();
    }

    public static void fechar() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}