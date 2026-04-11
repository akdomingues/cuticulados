package org.cuticulados.pm.config;

import org.flywaydb.core.Flyway;

/**
 * Executa as migrações do banco via Flyway.
 *
 * Deve ser chamado antes de {@link JpaUtil#inicializar()} para garantir que o schema
 * esteja atualizado antes de o Hibernate tentar usá-lo.
 * Os parâmetros de conexão podem ser sobrescritos via system properties db.url, db.user e db.pass.
 */
public class FlywayConfig {

    /** URL de conexão JDBC com o banco de dados. */
    private static final String URL = System.getProperty("db.url","jdbc:postgresql://localhost:5433/cuticulados");

    /** Usuário do banco de dados. */
    private static final String USER = System.getProperty("db.user","postgres");

    /** Senha do banco de dados. */
    private static final String PASS = System.getProperty("db.pass","postgres");

    /** Construtor privado: classe utilitária, não deve ser instanciada. */
    private FlywayConfig() {}

    /**
     * Executa as migrações SQL pendentes em classpath:db/migration.
     * baselineOnMigrate evita falha caso o banco já tenha tabelas sem controle de versão.
     */
    public static void executarMigracoes() {
        Flyway flyway = Flyway.configure()
                .dataSource(URL, USER, PASS)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();

        var resultado = flyway.migrate();
        System.out.println("Flyway: " + resultado.migrationsExecuted + " migração(ões) executada(s).");
    }
}