package org.cuticulados.pm.config;

import org.flywaydb.core.Flyway;

public class FlywayConfig {
    // passa a url, usar e a senha do banco de dados
    private static final String URL = System.getProperty("db.url", "jdbc:postgresql://localhost:5433/cuticulados");
    private static final String USER = System.getProperty("db.user", "postgres");
    private static final String PASS = System.getProperty("db.pass", "postgres");
    // impede a instanciação da classe, deixa só no uso estático
    private FlywayConfig() {
    }

    // configura o flyway
    public static void executarMigracoes() {
        Flyway flyway = Flyway.configure()
                .dataSource(URL, USER, PASS) // conexão com o banco
                .locations("classpath:db/migration") // pasta dos scripts sql
                .baselineOnMigrate(true) // permite que o flyway altere coisas no banco
                .load();

        var resultado = flyway.migrate(); // executa pendencias
        System.out.println("Flyway: " + resultado.migrationsExecuted + " migrações executadas!"); // log das migrações que forame executadas acima
    }
}