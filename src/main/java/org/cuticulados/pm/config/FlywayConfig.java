package org.cuticulados.pm.config;

import org.flywaydb.core.Flyway;

/**Deve ser chamado ANTES da inicialização do JPA*/
public class FlywayConfig {

    private static final String URL = System.getProperty("db.url","jdbc:postgresql://localhost:5432/cuticulados");
    private static final String USER = System.getProperty("db.user","postgres");
    private static final String PASS = System.getProperty("db.pass","postgres");

    private FlywayConfig() {}

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