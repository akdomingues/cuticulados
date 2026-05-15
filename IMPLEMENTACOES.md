# Resumo de Implementações — NailGestor

> **Branch:** `jp` | **Data:** 2026-05-03

---

## RF04 — Baixa automática de estoque ao concluir agendamento

**Arquivo:** `src/main/resources/db/migration/V6__trigger_baixa_estoque.sql`

Criada a trigger `trg_baixa_estoque_servico` (função `fn_baixa_estoque_servico`). Dispara automaticamente após `UPDATE` na tabela `agendamento` sempre que o `status` muda para `CONCLUIDO`. Deduz do estoque de cada produto a quantidade proporcional aos serviços realizados no agendamento, consultando a tabela `servico_produto`. Inclui guarda `quantidade_estoque >= ags.quantidade` para não operar sobre estoque insuficiente.

---

## RF05 — Bloqueio de agendamento quando material está insuficiente

**Arquivo:** `src/main/java/org/cuticulados/pm/service/AgendamentoService.java`

Adicionada verificação em `criarAgendamento()`, após a checagem de conflito de horário. Para cada serviço do agendamento, o método consulta `ProdutoRepository.buscarServicoProdutos()` e compara `produto.quantidadeEstoque` com `agendamentoServico.quantidade`. Se qualquer produto estiver abaixo do necessário, o agendamento é bloqueado e uma mensagem informa o produto em falta e as quantidades disponível/necessária.

---

## RF06 — Ranking de serviços por período

**Arquivos:** `RelatorioService.java`, `Main.java`

Adicionado o método `gerarRankingServicos(LocalDate inicio, LocalDate fim)` em `RelatorioService`. Executa uma query JPQL que filtra `AgendamentoServico` por período e status `CONCLUIDO`, agrupa por descrição do serviço e ordena por frequência decrescente, exibindo posição, nome, contagem de realizações e faturamento total.

No `Main.menuRelatorios()`, adicionada a opção **"6. Ranking de serviços por período"** com o método `rankingServicos()` que coleta o período e delega ao serviço.

---

## OO — Interface `Descontavel`

**Arquivos:** `Descontavel.java` (novo), `Cliente.java`, `AgendamentoService.java`

Criada a interface `Descontavel` com o contrato:

```java
double calcularDesconto(double valorBruto);
```

`Cliente` implementa a interface: clientes do tipo `"frequente"` recebem 10% de desconto; demais pagam o valor integral.

`AgendamentoService.calcularValorFinal()` foi atualizado para usar polimorfismo:

```java
if (cliente instanceof Descontavel d) {
    total = d.calcularDesconto(total);
}
```

---

## OO — `Usuario` abstrata

**Arquivo:** `src/main/java/org/cuticulados/pm/entity/Usuario.java`

A classe `Usuario` foi declarada `abstract`, impedindo instanciação direta. Todas as criações de usuários passam pelas subclasses concretas `Cliente` e `Profissional`, conforme o modelo de herança `@Inheritance(JOINED)`.

---

## JPA — `@ManyToMany` entre `Profissional` e `Servico`

**Arquivos:** `Profissional.java`, `Servico.java`

Adicionado mapeamento JPA do relacionamento N:N já existente no banco via tabela `profissional_servico`:

- `Profissional`: lado proprietário com `@ManyToMany @JoinTable(name = "profissional_servico", ...)`
- `Servico`: lado inverso com `@ManyToMany(mappedBy = "servicos")`

Completa o requisito de "1 de cada relacionamento JPA" (One-to-One, One-to-Many, Many-to-One e **Many-to-Many**).

---

## JPA — Cascade e `@PrePersist` no `AgendamentoServico`

**Arquivos:** `Agendamento.java`, `AgendamentoServico.java`

- `Agendamento.servicos`: adicionado `cascade = CascadeType.ALL, orphanRemoval = true` para que os itens de serviço sejam persistidos junto com o agendamento.
- `AgendamentoServico`: adicionado `@PrePersist` que define `createdAt = LocalDateTime.now()`, preenchendo automaticamente o campo `NOT NULL` ao persistir.

---

## Repositório — `ProdutoRepository.buscarServicoProdutos()`

**Arquivo:** `src/main/java/org/cuticulados/pm/repository/ProdutoRepository.java`

Novo método que retorna todos os `ServicoProduto` de um serviço com `JOIN FETCH sp.produto`, evitando LazyInitializationException fora do contexto JPA. Usado tanto pela verificação de estoque (RF05) quanto como base para a baixa via trigger (RF04).

---

## UI — Seleção de serviços ao criar agendamento

**Arquivo:** `src/main/java/org/cuticulados/pm/ui/Main.java`

O fluxo de `criarAgendamento()` foi estendido com um loop de seleção de serviços após a escolha de horário. Para cada serviço selecionado, cria um `AgendamentoServico` com `precoAplicado = servico.valorBase` e `quantidade = 1`, associa ao agendamento e adiciona à coleção. O loop encerra com Enter vazio. Isso popula a lista de serviços necessária para as validações de RF05.

---

## Bug — Porta do Docker Compose

**Arquivo:** `docker-compose.yml`

Corrigido o mapeamento de porta de `"5433:5432"` para `"5432:5432"`, alinhando a porta exposta no host com a porta configurada em `persistence.xml` e `FlywayConfig.java`. A inconsistência anterior impedia a conexão da aplicação ao banco iniciado via Docker sem ajuste manual.
