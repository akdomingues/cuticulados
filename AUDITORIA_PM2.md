# Relatório de Auditoria — NailGestor (Projeto Mensal II)

> **Auditor:** Claude Code (Engenheiro de Software Sênior)
> **Data:** 2026-04-29
> **Branch auditado:** `jp`
> **Último commit:** `9b14c76` — 2026-04-12 (Jão Pedro)

---

## Sumário Executivo

| # | Critério | Status |
|---|----------|--------|
| 1 | ORM, Persistência, Triggers, Functions, Views | ⚠️ ATENÇÃO |
| 2 | Migrations, Seeds, CI/CD | ⚠️ ATENÇÃO |
| 3 | Modelagem e Estruturação do Banco | ⚠️ ATENÇÃO |
| 4 | Orientação a Objetos | ❌ FALHOU |
| 5 | Atuação Ativa do Aluno | ⚠️ ATENÇÃO |
| 6 | Entrega no prazo | ✅ PASSOU |

---

## CRITÉRIO 1 — ORM, Persistência, Relacionamentos, Triggers, Functions e Views

### 1.1 Mapeamento JPA das Entidades

| Item | Status | Arquivo / Linha | Observação |
|------|--------|-----------------|------------|
| `@Entity` e `@Table` em todas as entidades | ✅ PASSOU | Todos os `.java` em `entity/` | Correto |
| `@Id` + `@GeneratedValue(IDENTITY)` | ✅ PASSOU | Todas as entidades | Correto |
| `@Column(nullable, length, precision, scale)` | ✅ PASSOU | Todas as entidades | Restrições mapeadas |
| `@Enumerated(EnumType.STRING)` | ✅ PASSOU | `Usuario.java:40`, `Agendamento.java:40`, `TransacaoFinanceira.java:26` | Consistente com V4 |
| `@Inheritance(strategy = InheritanceType.JOINED)` | ✅ PASSOU | `Usuario.java:21` | Estratégia correta |
| `Usuario` → `Cliente` e `Profissional` com JOINED | ✅ PASSOU | `Cliente.java:11`, `Profissional.java:11` | Hierarquia correta |
| `@OneToMany(mappedBy=...)` + `@ManyToOne` | ✅ PASSOU | `Cliente.java:27`, `Profissional.java:18`, `Agendamento.java:59` | Bidirecional sem tabelas extras |
| `@OneToOne(mappedBy=...)` | ✅ PASSOU | `VendaAvulsa.java:45`, `Agendamento.java:62` | Correto |
| `@PrePersist` para `createdAt`/`updatedAt` | ⚠️ ATENÇÃO | `AgendamentoServico.java` (sem `@PrePersist`) | **Campo `created_at` com `nullable=false` mas sem hook JPA → INSERT vai falhar com NOT NULL violation** |
| `@PreUpdate` em entidades com `updated_at` | ⚠️ ATENÇÃO | `Agendamento.java` (sem `@PreUpdate`) | `updated_at` só é atualizado pelo trigger SQL; do lado Java ficará desatualizado em cache de 1ª nível |

### 1.2 Organização dos Enums

| Item | Status | Arquivo / Linha | Observação |
|------|--------|-----------------|------------|
| `StatusAgendamento` definida em local único | ❌ FALHOU | `Agendamento.java:115–119` E `StatusAgendamento.java:1–7` | **Duplicação de `public enum` no mesmo pacote — erro de compilação latente** |
| `TipoTransacao` em arquivo próprio | ⚠️ ATENÇÃO | Definida dentro de `TipoUsuario.java` (sem `TipoTransacao.java`) | Funciona mas viola a convenção "um tipo público por arquivo" |
| `Usuario` declarada como classe abstrata | ⚠️ ATENÇÃO | `Usuario.java:22` | `Usuario` nunca deveria ser instanciada diretamente; a ausência de `abstract` permite `new Usuario()` sem hierarquia |

### 1.3 JPQL e Consultas

| Item | Status | Arquivo / Linha | Observação |
|------|--------|-----------------|------------|
| Consultas com `JOIN FETCH` | ✅ PASSOU | `AgendamentoRepository.java:33,46,72`, `ClienteRepository.java:55`, `VendaAvulsaRepository.java:43` | Correto |
| Consulta com `COUNT`, `SUM` (funções de agregação) | ✅ PASSOU | `AgendamentoRepository.java:90`, `TransacaoRepository.java:80` | Correto |
| Busca por período com `BETWEEN` | ✅ PASSOU | `AgendamentoRepository.java:73`, `TransacaoRepository.java:52` | Correto |
| `getSingleResult()` sem tratamento de `NoResultException` | ⚠️ ATENÇÃO | `AgendamentoRepository.java:36` | Usa `getSingleResult()` dentro de try/catch genérico — vai logar o erro mas retornar `Optional.empty()` silenciosamente quando o agendamento não existir; deve capturar `NoResultException` separadamente (como `ClienteRepository.java:45` faz corretamente) |

### 1.4 Triggers e Functions SQL

| Item | Status | Arquivo / Linha | Observação |
|------|--------|-----------------|------------|
| Mínimo de 3 triggers implementadas | ✅ PASSOU | `V2__create_triggers.sql` | **4 triggers** definidas |
| `trg_incrementa_atendimentos` — contagem de atendimentos ao concluir | ✅ PASSOU | `V2__create_triggers.sql:1–21` | Lógica correta |
| `trg_promove_cliente` — promoção para "frequente" | ✅ PASSOU | `V2__create_triggers.sql:24–42` | Lógica correta |
| `trg_estoque_negativo` — impede estoque negativo | ✅ PASSOU | `V2__create_triggers.sql:45–62` | Lógica correta (não um "choque de horário" mas pertinente) |
| `trg_updated_*` — atualização automática de `updated_at` | ✅ PASSOU | `V2__create_triggers.sql:65–91` | Aplicado em 3 tabelas |
| Trigger de impedir choque de horário no banco | ⚠️ ATENÇÃO | Ausente no SQL | A verificação de conflito existe **apenas via JPQL** em `AgendamentoRepository.java:88–103`; não há trigger SQL dedicada a isso |

### 1.5 Views

| Item | Status | Arquivo / Linha | Observação |
|------|--------|-----------------|------------|
| Pelo menos uma `VIEW` no banco de dados | ❌ FALHOU | Ausente em todos os scripts SQL | Nenhum `CREATE VIEW` encontrado em `V1` a `V5` |

---

## CRITÉRIO 2 — Versionamento e Integração do Banco (Migrations, Seeds, CI/CD)

| Item | Status | Arquivo / Linha | Observação |
|------|--------|-----------------|------------|
| Flyway configurado e funcional | ✅ PASSOU | `FlywayConfig.java:15–24`, `pom.xml:33–43` | Dependências `flyway-core` e `flyway-database-postgresql` 10.11.0 |
| Scripts na pasta `db/migration` | ✅ PASSOU | `src/main/resources/db/migration/` | 5 scripts: V1–V5 |
| Nomenclatura Flyway correta (`V#__descricao.sql`) | ✅ PASSOU | Todos os scripts | Padrão correto |
| Seed de dados (`V3__seed_data.sql`) | ✅ PASSOU | `V3__seed_data.sql` | Admin, 1 profissional, 3 clientes, 5 serviços, 7 produtos com `ON CONFLICT DO NOTHING` |
| Docker Compose com banco e migrações | ✅ PASSOU | `docker-compose.yml` | Serviços `db` e `flyway` com healthcheck |
| **MAPEAMENTO DE PORTA — BUG CRÍTICO** | ❌ FALHOU | `docker-compose.yml:10` vs `persistence.xml:26` | Docker expõe PostgreSQL na porta **5433** do host (`"5433:5432"`), mas a aplicação Java conecta em `localhost:**5432**`. A app **não consegue conectar ao banco Docker**. O mesmo erro está em `FlywayConfig.java:7` |
| CI/CD configurado | ❌ FALHOU | Ausente no repositório | Nenhum `.github/workflows/*.yml` ou pipeline equivalente encontrado |
| Consistência da versão do PostgreSQL | ⚠️ ATENÇÃO | `docker-compose.yml:3` | Usa `postgres:17` (imagem Docker); o enunciado menciona PostgreSQL 15 |
| `baselineOnMigrate(true)` no Flyway | ⚠️ ATENÇÃO | `FlywayConfig.java:19` | Útil em desenvolvimento, mas em produção pode mascarar schemas preexistentes incompatíveis |

---

## CRITÉRIO 3 — Modelagem e Estruturação do Banco (Normalização, Integridade, Performance)

| Item | Status | Arquivo / Linha | Observação |
|------|--------|-----------------|------------|
| Tabelas em 3FN | ✅ PASSOU | `V1__create_tables.sql` | Sem grupos repetidos, dependências parciais ou transitivas |
| Chaves primárias em todas as tabelas | ✅ PASSOU | `V1__create_tables.sql` | `bigserial primary key` ou FK composta |
| Chaves estrangeiras com `REFERENCES` | ✅ PASSOU | `V1__create_tables.sql` | Todas as FK definidas |
| `ON DELETE CASCADE` nas tabelas filhas da herança | ✅ PASSOU | `V1__create_tables.sql:25,33` | `profissional` e `cliente` referenciam `usuario` com CASCADE |
| `CHECK` constraints de domínio | ✅ PASSOU | `V1__create_tables.sql:43,45,54` | `valor_base > 0`, `duracao_minutos > 0`, `quantidade_estoque >= 0` |
| `UNIQUE` constraints | ✅ PASSOU | `V1__create_tables.sql:10,11,13,32` | email, login, CPF, agendamento_id (transação), venda_avulsa_id (transação) |
| Índices de performance | ✅ PASSOU | `V1__create_tables.sql:131–139` + `V5__add_fechado_venda.sql:7` | 11 índices criados nas colunas mais consultadas |
| Tabela `profissional_servico` sem entidade JPA | ⚠️ ATENÇÃO | `V1__create_tables.sql:63–68` | A tabela existe no banco mas não tem `@Entity` em Java; os dados de `profissional_servico` inseridos no seed (V3) não são acessíveis via JPA |
| VIEW no banco | ❌ FALHOU | Ausente | (ver Critério 1.5) |
| Uso de `timestamptz` vs `LocalDateTime` no Java | ⚠️ ATENÇÃO | `V1__create_tables.sql` vs entidades Java | `timestamptz` (com fuso) é mapeado para `LocalDateTime` (sem fuso) pelo Hibernate; funciona, mas pode gerar inconsistências em ambientes com UTC diferente |

---

## CRITÉRIO 4 — Orientação a Objetos (Classes, Herança, Polimorfismo, Encapsulamento, Interfaces)

| Item | Status | Arquivo / Linha | Observação |
|------|--------|-----------------|------------|
| Herança com `extends` | ✅ PASSOU | `Cliente.java:13`, `Profissional.java:13` | `extends Usuario` correto |
| Encapsulamento (atributos `private` + getters/setters) | ✅ PASSOU | Todas as entidades e serviços | Correto |
| Separação de camadas (Repository / Service / UI) | ✅ PASSOU | Estrutura de pacotes | Repository usa `EntityManager/JPQL`; Service orquestra regras; UI usa `Scanner` |
| Uso de `Optional` para prevenir `NullPointerException` | ✅ PASSOU | Todos os repositórios | `Optional.ofNullable()`, `Optional.empty()`, `.isPresent()`, `.isEmpty()` |
| `try-catch` nas operações de banco | ✅ PASSOU | Todos os repositórios e serviços | Cobertura completa |
| `Scanner` centralizado na UI | ✅ PASSOU | `Main.java:31` | `private static final Scanner scanner` — único, sem vazamento para Service/Model |
| Proteção contra `InputMismatchException` | ✅ PASSOU | `Main.java` — uso de `scanner.nextLine()` + `Long.parseLong()` / `Double.parseDouble()` em try-catch | Entradas capturadas como String antes do parse, sem risco de `InputMismatchException` |
| **Polimorfismo por sobrescrita de método** | ❌ FALHOU | `AgendamentoService.java:112` | O desconto de fidelidade é calculado com `"frequente".equals(cliente.getTipoCliente())` — **simples comparação de String**, não polimorfismo real. Nenhum método é sobrescrito (`@Override`) nas subclasses `Cliente` ou `Profissional` |
| **Interfaces definidas no projeto** | ❌ FALHOU | Ausente em todo o projeto | Nenhuma `interface` foi criada. O critério menciona explicitamente "Interfaces" como elemento de avaliação |
| **Padrão Factory** para instanciação de usuários | ❌ FALHOU | `Main.java:319,389` | Objetos criados diretamente com `new Cliente()`, `new Profissional()` — nenhuma classe `Factory` existe |
| `Usuario` declarada como `abstract` | ❌ FALHOU | `Usuario.java:22` | Não é `abstract`; permite `new Usuario()` diretamente, violando o contrato de herança |
| `UsuarioService.buscarPorLogin()` retorna `null` | ⚠️ ATENÇÃO | `UsuarioService.java:47` | `return usuarioRepo.buscarPorLogin(login).orElse(null)` — retorna `null` em vez de `Optional<Usuario>`, quebrando o contrato de null-safety do próprio projeto |
| `equals()` e `hashCode()` implementados | ✅ PASSOU | Todas as entidades | Baseados no `id` |

---

## CRITÉRIO 5 — Atuação Ativa do Aluno na Construção do Projeto

| Item | Status | Observação |
|------|--------|------------|
| Múltiplos contribuidores | ✅ PASSOU | 5 contribuidores identificados: Jão Pedro, Cauã Domingues, LeonardoBarth, Christopher, akdomingues |
| Pull Requests mergeados | ✅ PASSOU | PRs #1, #6, #7, #8, #9 visíveis no histórico |
| Branches de feature | ✅ PASSOU | `jp`, `domingues`, `agendamento`, `Alok` identificados no grafo |
| Commit de Jão Pedro com conteúdo técnico relevante | ✅ PASSOU | `62e075f` (Entidades JPA), `587e795` (fix de enum), `6199236` (JavaDoc), `dfdfb9e` (comentários) |
| Mensagens de commit de qualidade | ⚠️ ATENÇÃO | Commits `9eac5aa` ("a"), `7c51a91` ("."), `1d769c0` (".") são mensagens sem significado; comprometem a rastreabilidade |
| Evidência de code review nos PRs | ⚠️ ATENÇÃO | O git log local mostra merges de PRs, mas não há evidência de comentários/aprovações de revisão no histórico local — verificar no GitHub se os PRs têm reviews registrados |
| Distribuição de commits ao longo do tempo | ✅ PASSOU | Commits de 2026-04-07 a 2026-04-12 (6 dias de desenvolvimento) |

---

## CRITÉRIO 6 — Entrega no Prazo

| Item | Status | Observação |
|------|--------|------------|
| Último commit na branch `jp` | ✅ PASSOU | `9b14c76` em **2026-04-12** |
| Data de avaliação | — | 2026-04-29 |

---

## Plano de Ação — Correções Prioritárias

### PRIORIDADE ALTA (Bugs que impedem execução)

---

#### A1 — Corrigir mapeamento de porta no `persistence.xml` e `FlywayConfig.java`

**Problema:** Docker expõe PostgreSQL na porta `5433` do host, mas a aplicação conecta em `5432`.

**Correção em `src/main/resources/META-INF/persistence.xml` (linha 26):**
```xml
<!-- Trocar: -->
<property name="jakarta.persistence.jdbc.url" value="jdbc:postgresql://localhost:5432/cuticulados"/>
<!-- Por: -->
<property name="jakarta.persistence.jdbc.url" value="jdbc:postgresql://localhost:5433/cuticulados"/>
```

**Correção em `src/main/java/org/cuticulados/pm/config/FlywayConfig.java` (linha 7):**
```java
// Trocar:
private static final String URL = System.getProperty("db.url", "jdbc:postgresql://localhost:5432/cuticulados");
// Por:
private static final String URL = System.getProperty("db.url", "jdbc:postgresql://localhost:5433/cuticulados");
```

---

#### A2 — Adicionar `@PrePersist` em `AgendamentoServico.java`

**Problema:** `created_at` é `NOT NULL` no banco mas nunca é preenchido pelo Java.

**Correção em `AgendamentoServico.java` (adicionar antes dos getters):**
```java
import jakarta.persistence.PrePersist;

@PrePersist
protected void onCreate() {
    this.createdAt = java.time.LocalDateTime.now();
}
```

---

#### A3 — Resolver duplicação do enum `StatusAgendamento`

**Problema:** `public enum StatusAgendamento` definida em `Agendamento.java` (linhas 115–119) E em `StatusAgendamento.java`.

**Correção:** Remover as linhas 114–119 de `Agendamento.java` (manter apenas `StatusAgendamento.java`).

---

### PRIORIDADE MÉDIA (Afetam nota em OO e Banco)

---

#### B1 — Criar pelo menos uma VIEW no banco de dados

**Criar `V6__create_views.sql` em `src/main/resources/db/migration/`:**
```sql
-- View resumo de agendamentos com dados de cliente e profissional
CREATE VIEW vw_resumo_agendamentos AS
SELECT
    a.id,
    a.data_hora_inicio,
    a.data_hora_fim,
    a.status,
    a.valor_final,
    u_cli.nome  AS nome_cliente,
    c.tipo_cliente,
    u_pro.nome  AS nome_profissional,
    p.especialidade
FROM agendamento a
JOIN cliente      c     ON c.id = a.cliente_id
JOIN usuario      u_cli ON u_cli.id = c.id
JOIN profissional p     ON p.id = a.profissional_id
JOIN usuario      u_pro ON u_pro.id = p.id;

-- View de estoque com alerta de nível baixo
CREATE VIEW vw_estoque_alertas AS
SELECT
    id,
    nome,
    quantidade_estoque,
    quantidade_minima,
    CASE WHEN quantidade_estoque <= quantidade_minima THEN 'BAIXO' ELSE 'OK' END AS situacao
FROM produto;
```

---

#### B2 — Adicionar polimorfismo real via método sobrescrito

**Problema:** Desconto calculado por comparação de String, não por polimorfismo.

**Criar interface em `src/main/java/org/cuticulados/pm/entity/Descontavel.java`:**
```java
package org.cuticulados.pm.entity;

public interface Descontavel {
    double calcularDesconto(double valorBruto);
}
```

**Modificar `Cliente.java`:**
```java
public class Cliente extends Usuario implements Descontavel {

    @Override
    public double calcularDesconto(double valorBruto) {
        if ("frequente".equalsIgnoreCase(this.tipoCliente)) {
            return valorBruto * 0.10; // 10% de desconto
        }
        return 0.0;
    }
}
```

**Modificar `AgendamentoService.calcularValorFinal()` (linha 111–114):**
```java
// Substituir a verificação de string por despacho polimórfico:
Cliente cliente = agendamento.getCliente();
if (cliente instanceof Descontavel descontavel) {
    total -= descontavel.calcularDesconto(total);
}
```

---

#### B3 — Criar `UsuarioFactory.java`

**Criar `src/main/java/org/cuticulados/pm/factory/UsuarioFactory.java`:**
```java
package org.cuticulados.pm.factory;

import org.cuticulados.pm.entity.Cliente;
import org.cuticulados.pm.entity.Profissional;
import org.cuticulados.pm.entity.TipoUsuario;
import org.cuticulados.pm.entity.Usuario;

public class UsuarioFactory {

    private UsuarioFactory() {}

    public static Usuario criar(TipoUsuario tipo) {
        return switch (tipo) {
            case CLIENTE      -> new Cliente();
            case PROFISSIONAL -> new Profissional();
            default -> throw new IllegalArgumentException("Tipo de usuário inválido: " + tipo);
        };
    }
}
```

**Atualizar `Main.java` para usar a factory (linhas 319 e 389):**
```java
// Trocar: Cliente c = new Cliente();
Cliente c = (Cliente) UsuarioFactory.criar(TipoUsuario.CLIENTE);

// Trocar: Profissional p = new Profissional();
Profissional p = (Profissional) UsuarioFactory.criar(TipoUsuario.PROFISSIONAL);
```

---

#### B4 — Declarar `Usuario` como `abstract`

**Em `Usuario.java` (linha 22):**
```java
// Trocar:
public class Usuario {
// Por:
public abstract class Usuario {
```

---

#### B5 — Corrigir `UsuarioService.buscarPorLogin()` para retornar `Optional`

**Em `UsuarioService.java` (linhas 45–50):**
```java
// Trocar a assinatura e o corpo:
public Optional<Usuario> buscarPorLogin(String login) {
    try {
        return usuarioRepo.buscarPorLogin(login);
    } catch (Exception e) {
        System.out.println("Erro ao buscar usuario: " + e.getMessage());
        return Optional.empty();
    }
}
```
> Atualizar o código em `Main.java` que chama este método para trabalhar com `Optional`.

---

#### B6 — Corrigir `AgendamentoRepository.buscarPorId()` — capturar `NoResultException`

**Em `AgendamentoRepository.java` (linhas 30–42):**
```java
import jakarta.persistence.NoResultException;

public Optional<Agendamento> buscarPorId(Long id) {
    try (EntityManager em = JpaUtil.getEntityManager()) {
        Agendamento a = em.createQuery(
                "SELECT a FROM Agendamento a LEFT JOIN FETCH a.servicos LEFT JOIN FETCH a.cliente WHERE a.id = :id",
                Agendamento.class)
                .setParameter("id", id)
                .getSingleResult();
        return Optional.ofNullable(a);
    } catch (NoResultException e) {
        return Optional.empty();
    } catch (Exception e) {
        System.err.println("Erro ao buscar agendamento: " + e.getMessage());
        return Optional.empty();
    }
}
```

---

#### B7 — Adicionar `@PreUpdate` em `Agendamento.java`

**Em `Agendamento.java` (adicionar após o `@PrePersist`):**
```java
import jakarta.persistence.PreUpdate;

@PreUpdate
protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
}
```

---

### PRIORIDADE BAIXA (Melhorias de qualidade)

---

#### C1 — Criar pipeline de CI/CD

**Criar `.github/workflows/ci.yml`:**
```yaml
name: CI

on:
  push:
    branches: [ main, jp ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build with Maven
        run: ./mvnw package -DskipTests
```

---

#### C2 — Mover `TipoTransacao` para arquivo próprio

**Criar `src/main/java/org/cuticulados/pm/entity/TipoTransacao.java`:**
```java
package org.cuticulados.pm.entity;

public enum TipoTransacao {
    ENTRADA,
    SAIDA
}
```
Remover a definição de `TipoTransacao` de dentro de `TipoUsuario.java`.

---

#### C3 — Melhorar mensagens de commit (orientação para próximos commits)

Adotar o padrão Conventional Commits:
```
feat: adicionar view vw_resumo_agendamentos
fix: corrigir mapeamento de porta 5432 → 5433
refactor: extrair polimorfismo de desconto para interface Descontavel
```

---

## Resumo de Conformidade por Critério

```
Critério 1 (ORM/Banco):   ⚠️  APROVADO PARCIALMENTE
  ✅ Entidades JPA, herança JOINED, relacionamentos bidirecionais
  ✅ 4 triggers PL/pgSQL funcionais
  ❌ Sem nenhuma VIEW no banco
  ⚠️ Bug de @PrePersist em AgendamentoServico
  ⚠️ Enum duplicado (StatusAgendamento)

Critério 2 (Migrations):  ⚠️  APROVADO PARCIALMENTE
  ✅ 5 migrations V1–V5, seed completo, Docker Compose
  ❌ Bug de porta: app conecta em 5432, Docker expõe 5433
  ❌ Sem pipeline CI/CD

Critério 3 (Modelagem):   ⚠️  APROVADO PARCIALMENTE
  ✅ 3FN, FK, CHECK, UNIQUE, 11 índices
  ❌ Sem VIEW
  ⚠️ profissional_servico sem entidade JPA

Critério 4 (OO):          ❌  REPROVADO
  ✅ Herança, encapsulamento, camadas, Optional, try-catch
  ❌ Sem interfaces
  ❌ Sem Factory pattern
  ❌ Usuario não é abstract
  ❌ Polimorfismo via comparação de String (não sobrescrita)

Critério 5 (Atividade):   ⚠️  APROVADO PARCIALMENTE
  ✅ 5 contribuidores, múltiplos PRs, commits distribuídos
  ⚠️ Mensagens "a", "." sem significado técnico
  ⚠️ Verificar reviews nos PRs no GitHub

Critério 6 (Prazo):       ✅  APROVADO
  Último commit: 2026-04-12
```
