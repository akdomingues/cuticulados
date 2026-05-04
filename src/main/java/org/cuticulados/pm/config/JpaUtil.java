package org.cuticulados.pm.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {

    private static final String PERSISTENCE_UNIT = "cuticulados";
    private static EntityManagerFactory emf;

    private JpaUtil() {
    }

    // inicializa o emf caso ainda não tenha sido criado ou caso tenha sido fechado antes
    // deve ser chamado no começo da aplicação
    public static void inicializar() {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
        }
    }

    //IllegalStateException caso o emf não tenha sido inicializado
    public static EntityManager getEntityManager() {
        if (emf == null || !emf.isOpen()) {
            throw new IllegalStateException("JpaUtil não inicializado. Chame JpaUtil.inicializar() primeiro.");
        }
        return emf.createEntityManager();
    }

    // fecha o emf
    // deve ser chamado no final da aplicação
    public static void fechar() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}