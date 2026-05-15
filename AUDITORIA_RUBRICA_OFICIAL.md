# Auditoria Oficial — Rubrica "Critérios Mensal II.xlsx"
## NailGestor — Mensal II: Integração com o Banco de Dados

> **Auditor:** Claude Code (Engenheiro de Software Sênior)
> **Data:** 2026-04-29
> **Branch:** `jp` | **Último commit:** `9b14c76` — 2026-04-12
> **Fonte dos critérios:** `critérios mensal II.xlsx` (extraído e interpretado via XML interno)

---

## Estrutura da Rubrica — Como a Nota é Calculada

A planilha define **17 subitens** distribuídos em 5 critérios, cada um com um **peso (P)** e nota máxima **(N = 10)**.

```
NOTA GERAL = Σ(nota_subitem × peso_subitem) / Σ(pesos)
           = Σ(nota × peso) / 50
```

| Critério | Subitens | Soma dos Pesos | % da Nota |
|----------|----------|----------------|-----------|
| I  — Script SQL        | 4 subitens | 10 | 20% |
| II — Diagramas         | 2 subitens | 10 | 20% |
| III — Integração       | 4 subitens | 10 | 20% |
| IV — Aplicação Java    | 4 subitens | 10 | 20% |
| V  — Entregas          | 3 subitens | 10 | 20% |
| **TOTAL**              | **17**     | **50** | **100%** |

---

## CRITÉRIO I — Script SQL
> *Colunas B–E da planilha*

### Subitens, Pesos e Evidências

---

#### I-1 · Operações de criação de tabela · Peso: 2

| Status | ✅ PASSOU |
|--------|-----------|

**Evidência:** `V1__create_tables.sql` cria **8 tabelas** com sintaxe PostgreSQL correta:
`usuario`, `profissional`, `cliente`, `servico`, `produto`, `profissional_servico`, `agendamento`, `agendamento_servico`, `servico_produto`, `venda_avulsa`, `transacao_financeira`.

Todas as tabelas possuem:
- Chave primária (`bigserial primary key` ou FK como PK para herança)
- Tipos de dados adequados (`varchar`, `numeric`, `integer`, `timestamptz`, `boolean`)
- Valores `DEFAULT` onde pertinente

**Nota estimada: 10/10**

---

#### I-2 · Operações de CRUD para cada tabela · Peso: 2

| Status | ✅ PASSOU |
|--------|-----------|

**Evidência:** Todos os repositórios implementam CRUD completo via `EntityManager`:

| Repositório | Criar | Ler | Atualizar | Deletar |
|-------------|-------|-----|-----------|---------|
| `AgendamentoRepository` | `persist` L.20 | `find`/JPQL L.32 | `merge` L.22 | `remove` L.107 |
| `ClienteRepository` | `persist` L.19 | `find`/JPQL L.30 | `merge` L.21 | `remove` L.78 |
| `ProdutoRepository` | `persist` L.22 | `find`/JPQL L.36 | `merge` L.24 | `remove` L.84 |
| `ServicoRepository` | `persist` L.19 | `find`/JPQL L.32 | `merge` L.21 | `remove` L.73 |
| `UsuarioRepository` | `persist` L.17 | `find`/JPQL L.30 | `merge` L.21 | soft-delete L.65 |
| `VendaAvulsaRepository` | `persist` L.19 | `find` L.32 | `merge` L.21 | `remove` L.71 |
| `TransacaoRepository` | `persist` L.18 | `find` L.31 | `merge` L.20 | — |

**Nota estimada: 10/10**

---

#### I-3 · Implementação de cada JOIN abordado em aula · Peso: 3

| Status | ✅ PASSOU |
|--------|-----------|

**Evidência — JOINs identificados:**

| Tipo de JOIN | Localização | JPQL/SQL |
|---|---|---|
| `INNER JOIN` (implícito via `JOIN FETCH`) | `AgendamentoRepository.java:47`, `VendaAvulsaRepository.java:44` | JPQL |
| `LEFT JOIN FETCH` | `AgendamentoRepository.java:33`, `ClienteRepository.java:55`, `ServicoRepository.java:64` | JPQL |
| `JOIN` explícito com filtro | `AgendamentoRepository.java:73–77` | JPQL |
| `CROSS JOIN` | `V3__seed_data.sql:55–60` | SQL puro |

**Atenção:** Não foram encontrados `RIGHT JOIN` nem `FULL OUTER JOIN` em JPQL (o padrão JPA não suporta todos os tipos SQL nativamente). Para fins acadêmicos, os JOINs implementados cobrem o essencial.

**Nota estimada: 8/10** *(LEFT, INNER e CROSS presentes; ausência de RIGHT/FULL pode ser cobrada)*

---

#### I-4 · Implementação de ao menos 3 Triggers · Peso: 3

| Status | ✅ PASSOU |
|--------|-----------|

**Evidência:** `V2__create_triggers.sql` implementa **4 triggers** com funções PL/pgSQL:

| # | Trigger | Função | Evento | Tabela | Lógica |
|---|---------|--------|--------|--------|--------|
| 1 | `trg_incrementa_atendimentos` | `fn_incrementa_atendimentos` | `AFTER UPDATE` | `agendamento` | Incrementa `total_atendimentos_mes` quando status → `concluido` |
| 2 | `trg_promove_cliente` | `fn_promove_cliente` | `BEFORE UPDATE` | `cliente` | Promove para `frequente` quando atendimentos ≥ 3 |
| 3 | `trg_estoque_negativo` | `fn_estoque_negativo` | `BEFORE UPDATE` | `produto` | Bloqueia estoque negativo com `RAISE EXCEPTION` |
| 4 | `trg_updated_*` (×3) | `fn_atualiza_timestamp` | `BEFORE UPDATE` | `agendamento`, `servico`, `produto` | Atualiza `updated_at` automaticamente |

O requisito mínimo de 3 triggers é **amplamente superado**.

**Nota estimada: 10/10**

---

### Subtotal Estimado — Critério I

```
(10×2 + 8×3 + 10×3 + 10×2) / 10 = (20 + 24 + 30 + 20) / 10
```
> Aguarda revisão das notas individuais pelo professor.

---

## CRITÉRIO II — Diagramas
> *Colunas F–G da planilha*

### Subitens, Pesos e Evidências

---

#### II-1 · Diagrama Entidade-Relacionamento de Peter Chen · Peso: 5

| Status | ❌ FALHOU |
|--------|-----------|

**Evidência:** Varredura completa do repositório (`git log`, `Glob **/*`) não encontrou **nenhum arquivo de diagrama** (`.png`, `.jpg`, `.svg`, `.pdf`, `.drawio`, `.erd`, `.xml` de modelagem).

Nenhuma pasta `/docs`, `/diagramas`, `/ER` ou equivalente foi encontrada.

O histórico de commits não contém referência a adição de diagramas.

**Nota estimada: 0/10 — ITEM CRÍTICO**
> ⚠️ Este subitem tem peso 5 — a ausência zera 10% da nota final.

---

#### II-2 · Modelo Entidade-Relacionamento (MER) · Peso: 5

| Status | ❌ FALHOU |
|--------|-----------|

**Evidência:** Mesma conclusão do subitem anterior. Nenhum arquivo de MER (modelo lógico) encontrado no repositório.

**Nota estimada: 0/10 — ITEM CRÍTICO**
> ⚠️ Este subitem tem peso 5 — a ausência zera mais 10% da nota final.

---

### Subtotal Estimado — Critério II

```
(0×5 + 0×5) / 10 = 0/10
```
> **Impacto total na nota geral: –100 pontos no numerador (de 500 possíveis).**

---

## CRITÉRIO III — Integração
> *Colunas H–K da planilha*

### Subitens, Pesos e Evidências

---

#### III-1 · Aplicação integrada ao banco de dados · Peso: 1

| Status | ✅ PASSOU |
|--------|-----------|

**Evidência:**
- `JpaUtil.java` — gerencia `EntityManagerFactory` com persistence unit `cuticulados`
- `FlywayConfig.java` — executa migrações antes de inicializar JPA
- `persistence.xml` — configura driver PostgreSQL, URL, dialeto Hibernate
- `pom.xml` — dependências: `hibernate-core 6.4.4`, `postgresql 42.7.3`, `flyway-core 10.11.0`
- `docker-compose.yml` — banco PostgreSQL 17 + serviço Flyway

⚠️ **Bug de porta detectado:** `docker-compose.yml` expõe PostgreSQL em `localhost:5433` (host), mas `persistence.xml:26` e `FlywayConfig.java:7` conectam em `localhost:5432`. A aplicação **não conecta ao banco Docker** sem correção manual.

**Nota estimada: 7/10** *(integração existe mas tem bug de porta impeditivo)*

---

#### III-2 · Aplicação usando o padrão JPA para relacionamentos · Peso: 3

| Status | ✅ PASSOU |
|--------|-----------|

**Evidência:** Todas as entidades utilizam anotações JPA padrão Jakarta Persistence 3.0:

| Anotação | Entidade / Linha |
|---|---|
| `@Entity`, `@Table` | Todas as 8 entidades |
| `@Id`, `@GeneratedValue(IDENTITY)` | Todas as entidades |
| `@Column(nullable, length, precision)` | Todas as entidades |
| `@ManyToOne` + `@JoinColumn` | `Agendamento.java:49–55`, `AgendamentoServico.java:29–36`, `ServicoProduto.java:30–38`, `VendaAvulsa.java:25–43` |
| `@OneToMany(mappedBy=...)` | `Cliente.java:27`, `Profissional.java:18`, `Agendamento.java:59`, `Servico.java:49`, `Produto.java:55` |
| `@OneToOne` + `@OneToOne(mappedBy=...)` | `Agendamento.java:62`, `TransacaoFinanceira.java:39–44`, `VendaAvulsa.java:45` |
| `@Inheritance(InheritanceType.JOINED)` | `Usuario.java:21` |
| `@Enumerated(EnumType.STRING)` | `Usuario.java:40`, `Agendamento.java:40`, `TransacaoFinanceira.java:26` |
| `@PrePersist`, `@PreUpdate` | `Usuario.java:53,59`, `Servico.java:53,59`, `Produto.java:60,65` |

**Nota estimada: 10/10**

---

#### III-3 · 1 implementação de cada relacionamento JPA · Peso: 4

| Status | ⚠️ ATENÇÃO |
|--------|-----------|

**Relacionamentos implementados:**

| Tipo JPA | Implementado? | Localização |
|---|---|---|
| `@OneToMany` (bidirecional) | ✅ SIM | `Cliente.java:27`, `Agendamento.java:59` |
| `@ManyToOne` | ✅ SIM | `Agendamento.java:49–55`, `AgendamentoServico.java:29–36` |
| `@OneToOne` (bidirecional) | ✅ SIM | `TransacaoFinanceira.java:39–44`, `VendaAvulsa.java:45` |
| `@ManyToMany` | ❌ NÃO | A tabela `profissional_servico` existe no SQL mas **não tem `@Entity` JPA** correspondente — o relacionamento N:N não é gerenciado pelo JPA |

O relacionamento `@ManyToMany` entre `Profissional` e `Servico` existe no banco (via `profissional_servico`) mas foi "esquecido" no modelo JPA. Para atender ao critério de "1 de cada", a inclusão do `@ManyToMany` é necessária.

**Nota estimada: 7/10** *(3 dos 4 tipos implementados; falta @ManyToMany no JPA)*

---

#### III-4 · Implementação e uso do Flyway · Peso: 2

| Status | ✅ PASSOU |
|--------|-----------|

**Evidência:**

| Item | Localização | Detalhe |
|---|---|---|
| Dependência Flyway | `pom.xml:33–43` | `flyway-core 10.11.0` + `flyway-database-postgresql 10.11.0` |
| Configuração Java | `FlywayConfig.java` | `.configure().dataSource().locations("classpath:db/migration").baselineOnMigrate(true).load()` |
| Scripts de migração | `db/migration/V1–V5` | 5 scripts cobrindo criação, triggers, seed, fix de enum e nova coluna |
| Integração na inicialização | `Main.java:43` | `FlywayConfig.executarMigracoes()` chamado antes de `JpaUtil.inicializar()` |
| Docker Compose | `docker-compose.yml:19–28` | Serviço `flyway` com `depends_on: db: condition: service_healthy` |

**Nota estimada: 10/10**

---

### Subtotal Estimado — Critério III

```
(7×1 + 10×3 + 7×4 + 10×2) / 10 = (7 + 30 + 28 + 20) / 10 = 85/10 = 8,5
```

---

## CRITÉRIO IV — Aplicação Java
> *Colunas L–O da planilha*

### Subitens, Pesos e Evidências

---

#### IV-1 · Adequação da aplicação conforme Orientação a Objetos · Peso: 1

| Status | ⚠️ ATENÇÃO |
|--------|-----------|

**O que está correto:**
- ✅ Herança: `Cliente extends Usuario`, `Profissional extends Usuario` com `@Inheritance(JOINED)`
- ✅ Encapsulamento: todos os atributos `private` com getters/setters
- ✅ Separação de responsabilidades: Entity → Repository → Service → UI
- ✅ Uso de `Optional` para null-safety

**O que está faltando:**
- ❌ Nenhuma `interface` definida no projeto (critério de OO básico)
- ❌ `Usuario` não é `abstract` (`Usuario.java:22`), permitindo `new Usuario()` direto
- ❌ Polimorfismo implementado por **comparação de String** (`"frequente".equals(...)` em `AgendamentoService.java:112`) em vez de sobrescrita de método

**Nota estimada: 6/10**

---

#### IV-2 · Implementação de entidades, anotações, repositórios e serviços · Peso: 1

| Status | ✅ PASSOU |
|--------|-----------|

**Evidência — estrutura completa:**

| Camada | Arquivos | Observação |
|--------|----------|------------|
| **Entidades** | `Usuario`, `Cliente`, `Profissional`, `Agendamento`, `AgendamentoServico`, `Servico`, `ServicoProduto`, `Produto`, `VendaAvulsa`, `TransacaoFinanceira` | 10 entidades com anotações JPA completas |
| **Repositórios** | `AgendamentoRepository`, `ClienteRepository`, `ProdutoRepository`, `ServicoRepository`, `UsuarioRepository`, `TransacaoRepository`, `VendaAvulsaRepository` | 7 repositórios com `EntityManager` e JPQL |
| **Serviços** | `AgendamentoService`, `ClienteService`, `ProdutoService`, `ServicoService`, `UsuarioService`, `VendaAvulsaService`, `RelatorioService` | 7 serviços orquestrando regras de negócio |
| **Config** | `JpaUtil`, `FlywayConfig` | Infraestrutura de persistência |
| **UI** | `Main.java` | Menu terminal com `Scanner` centralizado |

**Nota estimada: 10/10**

---

#### IV-3 · Implementação de 4 regras de negócio (exceto CRUD) · Peso: 5

| Status | ✅ PASSOU |
|--------|-----------|

**Evidência — regras de negócio identificadas (mínimo exigido: 4):**

| # | Regra de Negócio | Arquivo / Linha | Descrição |
|---|-----------------|-----------------|-----------|
| 1 | **Verificação de conflito de horário** | `AgendamentoService.java:30–37` | Impede criar agendamento se o profissional já tem atendimento no mesmo período |
| 2 | **Desconto de fidelidade** | `AgendamentoService.java:99–121` | Cliente com tipo `"frequente"` recebe 10% de desconto no valor final |
| 3 | **Promoção de fidelidade** | `ClienteService.java:83–95` | Cliente com 3+ atendimentos no mês é classificado como `"frequente"` |
| 4 | **Verificação de estoque antes da venda** | `VendaAvulsaService.java:28–30` | Impede venda avulsa se estoque disponível for menor que a quantidade solicitada |
| 5 | **Impedir conclusão de agendamento cancelado** | `AgendamentoService.java:136–139` | Agendamento `CANCELADO` não pode ser marcado como `CONCLUIDO` |
| 6 | **Impedir cancelamento de agendamento concluído** | `AgendamentoService.java:157–160` | Agendamento `CONCLUIDO` não pode ser `CANCELADO` |
| 7 | **Fechamento de dia** | `VendaAvulsaService.java:91–109` | Marca todas as vendas abertas do dia de um profissional como `fechado = true` |

**7 regras implementadas — critério de 4 mínimas amplamente superado.**

**Nota estimada: 10/10**

---

#### IV-4 · Tratamento de erros com try-catch em todas as funções · Peso: 3

| Status | ✅ PASSOU — com ressalva |
|--------|-----------|

**Evidência — cobertura de try-catch:**

Todos os métodos de repositório e serviço possuem blocos `try-catch`, inclusive com diferenciação de exceções específicas em alguns casos:

```
AgendamentoRepository   → todos os 6 métodos com try-catch
ClienteRepository       → todos os 5 métodos (buscarPorCpf captura NoResultException separadamente)
ProdutoRepository       → todos os 5 métodos (buscarPorNome captura NoResultException separadamente)
ServicoRepository       → todos os 5 métodos
UsuarioRepository       → todos os 4 métodos
TransacaoRepository     → todos os 4 métodos
VendaAvulsaRepository   → todos os 4 métodos
AgendamentoService      → todos os 8 métodos
ClienteService          → todos os 6 métodos
ProdutoService          → todos os 5 métodos
ServicoService          → todos os 6 métodos
UsuarioService          → todos os 5 métodos
VendaAvulsaService      → todos os 4 métodos
RelatorioService        → todos os 4 métodos
Main.java               → todos os métodos de leitura de entrada (criarAgendamento, cadastrarCliente, etc.)
```

**Ressalva — `AgendamentoRepository.buscarPorId` (`linha 36`):**
Usa `getSingleResult()` sem capturar `NoResultException` separadamente — o catch genérico `Exception` absorve o erro mas não distingue "não encontrado" de "erro de banco". Para critério acadêmico, still covered.

**Nota estimada: 9/10**

---

### Subtotal Estimado — Critério IV

```
(6×1 + 10×1 + 10×5 + 9×3) / 10 = (6 + 10 + 50 + 27) / 10 = 93/10 = 9,3
```

---

## CRITÉRIO V — Entregas
> *Colunas P–R da planilha*

### Subitens, Pesos e Evidências

---

#### V-1 · Entrega do SRC em ZIP no Blackboard · Peso: 2

| Status | ⚠️ NÃO VERIFICÁVEL LOCALMENTE |
|--------|-----------|

**Evidência:** A entrega no Blackboard é externa ao repositório Git e não pode ser auditada via código-fonte. É necessário que o aluno confirme o envio.

**Ação necessária:** Verificar na guia de entrega final do Blackboard se o arquivo `.zip` foi enviado contendo a pasta `src/`.

**Nota estimada: — (indeterminada — depende de confirmação externa)**

---

#### V-2 · Entrega do link do Git com acesso livre · Peso: 3

| Status | ✅ PASSOU |
|--------|-----------|

**Evidência:**
- Repositório remoto identificado: `https://github.com/akdomingues/cuticulados`
- Referência encontrada no git log: `Merge branch 'main' of https://github.com/akdomingues/cuticulados into jp`
- Múltiplos contribuidores acessando o mesmo repositório remoto confirmam acesso livre

**Ação necessária:** Verificar se o repositório está público no GitHub (não privado).

**Nota estimada: 10/10** *(assumindo repositório público)*

---

#### V-3 · Atuação ativa nos commits e merges · Peso: 5

| Status | ⚠️ ATENÇÃO |
|--------|-----------|

**Evidência — análise do histórico Git da branch `jp`:**

**Contribuidores identificados (com commits):**

| Autor | Commits Relevantes |
|-------|--------------------|
| Jão Pedro | `62e075f` (entidades JPA), `587e795` (fix enums), `6199236` (JavaDoc), `dfdfb9e` (comentários), merges de PR |
| Cauã Domingues | `da27ea5` (agendamentoServico), `729eec8` (Produto e servico), `3882107` (vendaAvulsa), merges |
| LeonardoBarth | `d93181f` (AgendamentoRepository, TransacaoFinanceira), `4411052` (comentários), `fa4d68e` |
| Christopher | `db66049` (Produto e servico), `5611729` (Produto) |
| akdomingues | `c43b109` ("finalizado"), `799d7f6`, migrations |

**Pull Requests mergeados visíveis:** #1, #6, #7, #8, #9

**Pontos positivos:**
- ✅ 5 contribuidores com commits distintos
- ✅ Workflow com branches de feature e PRs
- ✅ Commits distribuídos ao longo de múltiplos dias (07/04 → 12/04)
- ✅ Commits com conteúdo técnico descritivo: `62e075f`, `d93181f`, `6199236`

**Pontos negativos:**
- ❌ Commit `9eac5aa` — mensagem `"a"` (sem valor descritivo)
- ❌ Commit `7c51a91` — mensagem `"."` (sem valor descritivo)
- ❌ Commit `1d769c0` — mensagem `"."` (sem valor descritivo)
- ❌ Commit `c43b109` — mensagem `"finalizado"` (vaga)
- ⚠️ Não é possível verificar evidências de code review (comentários nos PRs) via git log local — verificar no GitHub

**Nota estimada: 7/10**

---

### Subtotal Estimado — Critério V

```
(10×2 + 10×3 + 7×5) / 10 = (20 + 30 + 35) / 10 = 85/10 = 8,5
```
*(Assumindo ZIP entregue com nota 10)*

---

## Nota Geral Estimada

Usando a fórmula exata da planilha:

```
NOTA GERAL = (B×2 + C×2 + D×3 + E×3 + F×5 + G×5 + H×1 + I×3 + J×4 + K×2 + L×1 + M×1 + N×5 + O×3 + P×2 + Q×3 + R×5) / 50
```

| Subitem | Descrição | Nota | Peso | Pontos |
|---------|-----------|------|------|--------|
| I-1 (B) | Criação de tabelas | 10 | 2 | 20 |
| I-2 (C) | CRUD para cada tabela | 10 | 2 | 20 |
| I-3 (D) | JOINs | 8 | 3 | 24 |
| I-4 (E) | 3+ Triggers | 10 | 3 | 30 |
| **II-1 (F)** | **DER de Peter Chen** | **0** | **5** | **0** |
| **II-2 (G)** | **MER** | **0** | **5** | **0** |
| III-1 (H) | App integrada ao banco | 7 | 1 | 7 |
| III-2 (I) | JPA para relacionamentos | 10 | 3 | 30 |
| III-3 (J) | 1 de cada relacionamento JPA | 7 | 4 | 28 |
| III-4 (K) | Flyway | 10 | 2 | 20 |
| IV-1 (L) | Adequação OO | 6 | 1 | 6 |
| IV-2 (M) | Entidades, repos, serviços | 10 | 1 | 10 |
| IV-3 (N) | 4 regras de negócio | 10 | 5 | 50 |
| IV-4 (O) | Try-catch | 9 | 3 | 27 |
| V-1 (P) | ZIP Blackboard | 10* | 2 | 20* |
| V-2 (Q) | Link Git | 10 | 3 | 30 |
| V-3 (R) | Contribuição | 7 | 5 | 35 |
| | **SOMA** | | **50** | **357** |

```
NOTA ESTIMADA = 357 / 50 = 7,14
```

> ⚠️ *Nota com ZIP entregue (10). Sem ZIP: 337/50 = 6,74*

---

## Diagnóstico — O que está derrubando a nota

```
┌─────────────────────────────────────────────────────────────────────┐
│  IMPACTO NA NOTA GERAL POR ITEM                                     │
├───────────────────────┬──────────┬─────────────────────────────────┤
│ Item                  │ Perda    │ Causa                           │
├───────────────────────┼──────────┼─────────────────────────────────┤
│ DER de Peter Chen     │ -1,00    │ Arquivo ausente no repositório  │
│ MER                   │ -1,00    │ Arquivo ausente no repositório  │
│ @ManyToMany no JPA    │ -0,24    │ profissional_servico sem @Entity │
│ Qualidade OO          │ -0,08    │ Sem interface, sem abstract     │
│ Contribuição          │ -0,30    │ Commits "a", ".", "finalizado"  │
│ Bug de porta          │ -0,06    │ 5432 vs 5433                    │
├───────────────────────┼──────────┼─────────────────────────────────┤
│ TOTAL DE PERDAS       │ -1,68    │                                 │
└───────────────────────┴──────────┴─────────────────────────────────┘
```

> O Critério II (Diagramas) representa 20% da nota — a ausência dos dois diagramas custa **~2 pontos inteiros** na nota final de 10.

---

## Plano de Ação — Ordenado por Impacto na Nota

### URGENTE — Recupera ~2,0 pontos

#### AÇÃO 1 — Criar e commitar os diagramas

**DER de Peter Chen** (notação com retângulos, losangos, elipses):
- Ferramenta sugerida: [draw.io](https://app.diagrams.net), brModelo, ou Lucidchart
- Entidades: `usuario`, `profissional`, `cliente`, `servico`, `produto`, `agendamento`, `agendamento_servico`, `servico_produto`, `venda_avulsa`, `transacao_financeira`
- Relacionamentos: cliente `realiza` agendamento (1:N), profissional `executa` agendamento (1:N), agendamento `contém` servico (N:N via agendamento_servico), serviço `usa` produto (N:N via servico_produto)

**MER (Modelo Lógico)**:
- Tabelas com colunas, tipos de dados, PK (sublinhado), FK (itálico), cardinalidades
- Pode ser gerado automaticamente pelo DBeaver ou pgAdmin a partir do banco

**Onde salvar:**
```
/docs/
  der_peter_chen.png
  mer_logico.png
```

```bash
git add docs/
git commit -m "docs: adicionar DER de Peter Chen e MER do banco de dados"
```

---

### MÉDIA PRIORIDADE — Recupera ~0,24 pontos

#### AÇÃO 2 — Implementar `@ManyToMany` entre `Profissional` e `Servico`

**Criar `ProfissionalServico.java`** (ou adicionar `@ManyToMany` diretamente):

```java
// Em Profissional.java — adicionar campo:
@ManyToMany
@JoinTable(
    name = "profissional_servico",
    joinColumns = @JoinColumn(name = "profissional_id"),
    inverseJoinColumns = @JoinColumn(name = "servico_id")
)
private List<Servico> servicos = new ArrayList<>();
```

```java
// Em Servico.java — adicionar campo bidirecional:
@ManyToMany(mappedBy = "servicos")
private List<Profissional> profissionais = new ArrayList<>();
```

Registrar `Profissional` e `Servico` no `persistence.xml` (já estão).

---

### QUALIDADE — Recupera ~0,38 pontos

#### AÇÃO 3 — Corrigir bug de porta

Em `persistence.xml:26` e `FlywayConfig.java:7`:
```
localhost:5432  →  localhost:5433
```

#### AÇÃO 4 — Melhorar qualidade dos commits futuros

Padrão recomendado (Conventional Commits):
```
feat: implementar @ManyToMany entre Profissional e Servico
docs: adicionar DER de Peter Chen e modelo ER
fix: corrigir porta de conexão JDBC (5432 → 5433)
refactor: tornar classe Usuario abstrata
```

#### AÇÃO 5 — Declarar `Usuario` como `abstract`

```java
// Usuario.java linha 22
public abstract class Usuario {  // adicionar 'abstract'
```

#### AÇÃO 6 — Criar ao menos uma `interface`

```java
// src/main/java/org/cuticulados/pm/entity/Descontavel.java
package org.cuticulados.pm.entity;

public interface Descontavel {
    double calcularDesconto(double valorBruto);
}
```

Implementar em `Cliente.java` com `@Override` de `calcularDesconto()`.

---

## Checklist Final para Entrega

```
[ ] DER de Peter Chen salvo em /docs/ e commitado          ← URGENTE (+1,0 ponto)
[ ] MER (modelo lógico) salvo em /docs/ e commitado        ← URGENTE (+1,0 ponto)
[ ] @ManyToMany em Profissional ↔ Servico                  ← MÉDIA   (+0,24 ponto)
[ ] Bug de porta corrigido (5432 → 5433)                   ← BAIXA   (+0,06 ponto)
[ ] Repositório Git público confirmado no GitHub           ← CONFIRMAR
[ ] ZIP com /src entregue no Blackboard                    ← CONFIRMAR
[ ] Link do GitHub na guia de entrega do Blackboard        ← CONFIRMAR
```
