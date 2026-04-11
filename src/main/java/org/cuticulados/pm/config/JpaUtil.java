package org.cuticulados.pm.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Gerencia o ciclo de vida do JPA com uma única EntityManagerFactory (Singleton).
 *
 * Cada operação deve obter um EntityManager via {@link #getEntityManager()} e fechá-lo
 * ao final (try-with-resources). Fluxo esperado: executarMigracoes → inicializar →
 * getEntityManager (por operação) → fechar (ao encerrar).
 */
public class JpaUtil {

    /** Nome da unidade de persistência declarada em {@code META-INF/persistence.xml}. */
    private static final String PERSISTENCE_UNIT = "cuticulados";

    /** Fábrica de EntityManagers, inicializada uma única vez. */
    private static EntityManagerFactory emf;

    /** Construtor privado: classe utilitária, não deve ser instanciada. */
    private JpaUtil() {}

    /**
     * Inicializa a EntityManagerFactory. Idempotente: se já estiver aberta, não faz nada.
     */
    public static void inicializar() {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
        }
    }

    /**
     * Retorna um novo EntityManager. O chamador é responsável por fechá-lo (try-with-resources).
     *
     * @return novo EntityManager
     * @throws IllegalStateException se inicializar() não foi chamado antes
     */
    public static EntityManager getEntityManager() {
        if (emf == null || !emf.isOpen()) {
            throw new IllegalStateException("JpaUtil não inicializado. Chame JpaUtil.inicializar() primeiro.");
        }
        return emf.createEntityManager();
    }

    /**
     * Fecha a EntityManagerFactory e libera os recursos.
     * Deve ser chamado no bloco finally do main ao encerrar a aplicação.
     */
    public static void fechar() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}