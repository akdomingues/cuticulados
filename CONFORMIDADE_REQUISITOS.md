# Documento de Conformidade de Requisitos — NailGestor
## Comparativo: Especificação de Requisitos × Estado Atual do Projeto

> **Elaborado por:** Claude Code (Engenheiro de Software Sênior)
> **Data:** 2026-04-29
> **Branch analisado:** `jp` · Último commit: `9b14c76` (2026-04-12)
> **Fonte primária:** `Documento especificação de requisitos.pdf` (rev. 03 — 30/03/2026)

---

## 1. Identificação do Projeto

| Campo | Especificado no Documento | Estado Atual |
|-------|--------------------------|--------------|
| **Nome** | NailGestor | ✅ Consistente (`pom.xml`: `cuticulados`) |
| **Descrição** | Sistema de gerenciamento de agendamentos, estoque e faturamento para profissionais de Nail Design | ✅ Implementado |
| **Linguagem** | Java 17 | ✅ `pom.xml:13–14`: `maven.compiler.source=17` |
| **Persistência** | JPA | ✅ Hibernate 6.4.4 + Jakarta Persistence 3.0 |
| **Banco de Dados** | PostgreSQL 15+ | ✅ Docker usa `postgres:17` (satisfaz 15+) |
| **Ambiente** | Local (máquina local) | ✅ Docker Compose para execução local |
| **IDE** | IntelliJ IDEA + DBeaver | ✅ Pasta `.idea/` presente |
| **Metodologia** | SCRUM + KANBAN (ClickUP) | ⚠️ Não verificável via código |
| **Ambiente (doc diz "Typescript")** | ⚠️ Provavelmente erro tipográfico no documento | — |

> ⚠️ O documento lista "**Typescript**" como ferramenta de ambiente — isso é inconsistente com um projeto Java SE CLI. Provavelmente erro de redação (talvez pretendiam escrever "Console" ou "Terminal").

---

## 2. Time e Papéis

| Papel | Pessoa | Commits Identificados no Git |
|-------|--------|------------------------------|
| Scrum Master | João Pedro Rospirsk | ✅ `bfc7e71`, `62e075f`, `587e795`, `6199236`, `dfdfb9e` |
| Product Owner | Christopher Adam | ✅ `db66049`, `5611729` |
| Tech Lead | Cauã Buch Domingues | ✅ `da27ea5`, `729eec8`, `3882107`, PRs #1, #7, #9 |
| Desenvolvedor | Leonardo Barth | ✅ `d93181f`, `4411052`, `fa4d68e` |
| Demandante | Vitória Jennifer Oliveira dos Santos | — (não desenvolvedora) |
| Mentor | Kauan Emanuel Vanceta Mendes | — (não desenvolvedora) |

---

## 3. Cronograma — Estado das Entregas

| # | Prazo | Descrição | Status | Observação |
|---|-------|-----------|--------|------------|
| 1ª | 30/03/2026 | Definição da demanda e documentação inicial | ✅ **ENTREGUE** | Rev. 03 do documento datada de 30/03/2026 |
| 2ª | 27/04/2026 | Modelagem de dados (DER Peter Chen) e protótipo lógico | ❌ **ATRASADA** | Prazo vencido (hoje é 29/04/2026). Nenhum diagrama encontrado no repositório |
| 3ª | 25/05/2026 | Desenvolvimento do Back-end (CRUDs e Regras) | 🔄 **EM ANDAMENTO** | Back-end parcialmente implementado; prazo: 26 dias |
| 4ª | 22/06/2026 | Aplicação final, vídeo e Resumo Expandido | ⏳ **PENDENTE** | Prazo: 54 dias |

> 🚨 A **2ª entrega está em atraso** — o DER de Peter Chen e o Modelo ER (MER) deveriam ter sido entregues em 27/04/2026 e não foram encontrados no repositório.

---

## 4. Requisitos Funcionais — Análise de Conformidade

### RF01 — Autenticar
> **Prioridade: Essencial**
> _"Login com níveis de Administrador e Atendente."_

| Item | Status | Evidência no Código |
|------|--------|---------------------|
| Login com usuário e senha | ✅ IMPLEMENTADO | `UsuarioService.autenticar()` — `UsuarioService.java:31–43` |
| Nível Administrador | ✅ IMPLEMENTADO | `TipoUsuario.ADMIN` → `menuAdmin()` em `Main.java:102` |
| Nível Atendente (→ Profissional) | ✅ IMPLEMENTADO | `TipoUsuario.PROFISSIONAL` → `menuProfissional()` em `Main.java:141` |
| Nível extra: Cliente | ✅ BÔNUS | `TipoUsuario.CLIENTE` → `menuCliente()` — vai além do especificado |
| Redirecionamento por perfil | ✅ IMPLEMENTADO | `switch(usuario.getTipo())` em `Main.java:84–88` |
| Persistência do usuário logado na sessão | ✅ IMPLEMENTADO | Usuário passado como parâmetro entre métodos (`Main.java:141,180`) |

**Veredicto: ✅ CONFORME** — Implementado além do mínimo especificado (3 níveis em vez de 2).

---

### RF02 — Manter Clientes
> **Prioridade: Essencial**
> _"Cadastro, consulta, edição e exclusão de clientes."_

| Operação | Status | Evidência no Código |
|----------|--------|---------------------|
| **C**adastrar cliente | ✅ IMPLEMENTADO | `ClienteService.cadastrarCliente()` — `ClienteService.java:16–34` |
| Validação de CPF único | ✅ IMPLEMENTADO | `clienteRepo.buscarPorCpf()` antes de salvar — `ClienteService.java:22–25` |
| **R**ead — buscar por ID | ✅ IMPLEMENTADO | `ClienteRepository.buscarPorId()` — `ClienteRepository.java:28` |
| **R**ead — buscar por CPF | ✅ IMPLEMENTADO | `ClienteRepository.buscarPorCpf()` — `ClienteRepository.java:38` |
| **R**ead — listar todos | ✅ IMPLEMENTADO | `ClienteRepository.listarTodos()` — `ClienteRepository.java:53` |
| **U**pdate — atualizar dados | ✅ IMPLEMENTADO | `ClienteService.atualizarCliente()` — `ClienteService.java:54–65` |
| **D**elete — remover cliente | ✅ IMPLEMENTADO | `ClienteService.removerCliente()` — `ClienteService.java:68–79` |
| Interface UI (CRUD na tela) | ✅ IMPLEMENTADO | `menuClientes()`, `cadastrarCliente()`, `atualizarCliente()`, `removerCliente()` — `Main.java:284–355` |

**Veredicto: ✅ CONFORME** — CRUD completo implementado com validações.

---

### RF03 — Gerenciar Agenda
> **Prioridade: Essencial**
> _"Marcar horários evitando sobreposição de atendimentos."_

| Item | Status | Evidência no Código |
|------|--------|---------------------|
| Criar agendamento | ✅ IMPLEMENTADO | `AgendamentoService.criarAgendamento()` — `AgendamentoService.java:22–46` |
| **Validação de choque de horário** | ✅ IMPLEMENTADO | `AgendamentoRepository.existeConflito()` — `AgendamentoRepository.java:88–103` (JPQL) + `trg_incrementa_atendimentos` (SQL) |
| Listar agendamentos | ✅ IMPLEMENTADO | `AgendamentoService.listarTodos()` + UI em `Main.java:574` |
| Filtrar por período | ✅ IMPLEMENTADO | `AgendamentoService.buscarPorPeriodo()` — `AgendamentoService.java:169` |
| Filtrar por status | ✅ IMPLEMENTADO | `AgendamentoService.buscarPorStatus()` — `AgendamentoService.java:178` |
| Concluir agendamento | ✅ IMPLEMENTADO | `AgendamentoService.concluirAgendamento()` — `AgendamentoService.java:124–146` |
| Cancelar agendamento | ✅ IMPLEMENTADO | `AgendamentoService.cancelarAgendamento()` — `AgendamentoService.java:149–167` |
| Remover agendamento | ✅ IMPLEMENTADO | `AgendamentoService.removerAgendamento()` — `AgendamentoService.java:79–96` |

**Veredicto: ✅ CONFORME**

---

### RF04 — Baixa de Estoque
> **Prioridade: Essencial**
> _"Subtração automática de insumos ao concluir um serviço."_

| Item | Status | Evidência no Código |
|------|--------|---------------------|
| Baixa de estoque em **venda avulsa** | ✅ IMPLEMENTADO | `VendaAvulsaService.registrarVenda()` linhas 38–40: `produto.setQuantidadeEstoque(estoque - qtd)` |
| Trigger de proteção de estoque negativo | ✅ IMPLEMENTADO | `trg_estoque_negativo` em `V2__create_triggers.sql:45–62` |
| **Baixa automática ao CONCLUIR AGENDAMENTO** | ❌ NÃO IMPLEMENTADO | `AgendamentoService.concluirAgendamento()` (`AgendamentoService.java:124–146`) **não deduz estoque**. Existe a tabela `servico_produto` mapeando insumos por serviço, mas esta informação nunca é usada para baixar o estoque |
| Trigger SQL de baixa ao concluir serviço | ❌ NÃO IMPLEMENTADO | Nenhum trigger SQL para `UPDATE produto SET quantidade_estoque` disparado ao mudar status do agendamento para `concluido` |

**Veredicto: ⚠️ PARCIALMENTE CONFORME**

> 🚨 **Gap crítico:** O requisito diz "ao concluir um serviço". Quando `concluirAgendamento()` é chamado, o estoque dos produtos do `servico_produto` **não é decrementado**. A baixa de estoque só ocorre em vendas avulsas. O modelo de dados existe (`servico_produto` mapeia quais produtos cada serviço usa), mas a lógica de aplicação e a trigger SQL não foram implementadas para o fluxo de agendamento.

---

### RF05 — Alerta de Insumo
> **Prioridade: Importante**
> _"Bloquear agendamento se o material estiver insuficiente."_

| Item | Status | Evidência no Código |
|------|--------|---------------------|
| Alerta visual de estoque baixo | ✅ IMPLEMENTADO | `ProdutoService.verificarEstoqueBaixo()` + `relatorioService.gerarRelatorioEstoque()` com indicador `[BAIXO]` |
| Menu de alertas acessível | ✅ IMPLEMENTADO | `Main.java:495` — opção "Estoque baixo" no menu de produtos |
| **Bloqueio de agendamento quando insumo insuficiente** | ❌ NÃO IMPLEMENTADO | `AgendamentoService.criarAgendamento()` (`AgendamentoService.java:22–46`) **não verifica estoque** antes de confirmar |

**Veredicto: ⚠️ PARCIALMENTE CONFORME**

> 🚨 **Gap:** O alerta visual existe, mas o **bloqueio ativo** (pré-condição para criar um agendamento) não foi implementado. O sistema permite agendar mesmo sem insumos disponíveis.

---

### RF06 — Relatórios
> **Prioridade: Desejável**
> _"Exibir faturamento e ranking de serviços por período."_

| Item | Status | Evidência no Código |
|------|--------|---------------------|
| Relatório financeiro por período | ✅ IMPLEMENTADO | `RelatorioService.gerarRelatorioFinanceiro()` — `RelatorioService.java:80–105` |
| Relatório de agendamentos por período | ✅ IMPLEMENTADO | `RelatorioService.gerarRelatorioAgendamentos()` — `RelatorioService.java:42–56` |
| Relatório de estoque | ✅ IMPLEMENTADO | `RelatorioService.gerarRelatorioEstoque()` — `RelatorioService.java:111–122` |
| Saldo geral do caixa (entradas - saídas) | ✅ IMPLEMENTADO | `RelatorioService.imprimirSaldo()` — `RelatorioService.java:130–143` |
| Vendas do dia | ✅ IMPLEMENTADO | `VendaAvulsaService.relatorioVendasDoDia()` — `VendaAvulsaService.java:70–89` |
| **Ranking de serviços por período** | ❌ NÃO IMPLEMENTADO | Nenhuma query que agrupe e ordene serviços por frequência ou faturamento |

**Veredicto: ⚠️ PARCIALMENTE CONFORME** *(prioridade: Desejável — menor impacto)*

---

### Painel de Conformidade — Requisitos Funcionais

```
RF01 — Autenticar            ████████████ 100%  ✅ CONFORME
RF02 — Manter Clientes       ████████████ 100%  ✅ CONFORME
RF03 — Gerenciar Agenda      ████████████ 100%  ✅ CONFORME
RF04 — Baixa de Estoque      ██████░░░░░░  50%  ⚠️ PARCIAL  ← ESSENCIAL
RF05 — Alerta de Insumo      ███████░░░░░  60%  ⚠️ PARCIAL  ← IMPORTANTE
RF06 — Relatórios            ████████░░░░  80%  ⚠️ PARCIAL  ← DESEJÁVEL
```

---

## 5. Requisitos Não Funcionais — Análise de Conformidade

### RNF01 — Persistência
> _"Os dados devem ser armazenados de forma permanente no PostgreSQL."_

| Item | Status | Evidência |
|------|--------|-----------|
| PostgreSQL como SGBD | ✅ CONFORME | `docker-compose.yml`, `persistence.xml:26`, `pom.xml:29` |
| Persistência permanente (não volátil) | ✅ CONFORME | Volume Docker `postgres_data:/var/lib/postgresql/data` |
| Migrations para controle de schema | ✅ BÔNUS | Flyway V1–V5 (não era obrigatório pelo documento) |

**Veredicto: ✅ CONFORME**

---

### RNF02 — Ambiente
> _"O sistema deve ser executado na máquina local."_

| Item | Status | Evidência |
|------|--------|-----------|
| Execução local | ✅ CONFORME | Java SE + Docker Compose — sem dependência de nuvem |
| Sem GUI (conforme escopo) | ✅ CONFORME | Aplicação 100% CLI via `Scanner` |
| Sem processamento de pagamento (conforme escopo) | ✅ CONFORME | Nenhum gateway de pagamento |
| Sem integração com nuvem (conforme escopo) | ✅ CONFORME | Sem AWS, sem APIs externas |
| **Bug de porta Docker** | ⚠️ ATENÇÃO | `docker-compose.yml` expõe porta `5433`, app conecta em `5432` — requer correção manual para rodar com Docker |

**Veredicto: ✅ CONFORME** *(com ressalva de bug de porta)*

---

### RNF03 — Interface CLI
> _"A interface CLI deve ser intuitiva, tratando erros de entrada de dados com try-catch."_

| Item | Status | Evidência |
|------|--------|-----------|
| Interface de terminal (CLI) | ✅ CONFORME | `Main.java` — menus estruturados com `Scanner` |
| `Scanner` único centralizado | ✅ CONFORME | `Main.java:31` — `private static final Scanner scanner` |
| Try-catch em leitura de entradas | ✅ CONFORME | Todos os métodos de entrada (`cadastrarCliente`, `criarAgendamento`, etc.) |
| Proteção contra entrada inválida | ✅ CONFORME | `scanner.nextLine()` + parse manual (sem risco de `InputMismatchException`) |
| `DateTimeParseException` tratada | ✅ CONFORME | `Main.java:616–617`, `Main.java:655–656` |
| Mensagens de feedback ao usuário | ✅ CONFORME | Mensagens descritivas em todos os fluxos |
| Menus por perfil (Admin/Prof/Cliente) | ✅ CONFORME | `menuAdmin()`, `menuProfissional()`, `menuCliente()` |

**Veredicto: ✅ CONFORME**

---

### RNF04 — Desempenho
> _"O tempo de resposta para operações de busca não deve exceder 2 segundos."_

| Item | Status | Evidência |
|------|--------|-----------|
| Índices nas colunas de busca frequente | ✅ IMPLEMENTADO | `V1__create_tables.sql:131–139`: 10 índices criados |
| `JOIN FETCH` para evitar N+1 queries | ✅ IMPLEMENTADO | `AgendamentoRepository.java:33,47`, `ClienteRepository.java:55` |
| Pool de conexões configurado | ✅ IMPLEMENTADO | `persistence.xml:37`: `hibernate.connection.pool_size=5` |
| Medição de tempo de resposta | ❌ NÃO IMPLEMENTADO | Sem logs de tempo, sem métricas de performance |
| Teste de carga/stress | ❌ NÃO VERIFICÁVEL | Sem testes automatizados no projeto |

**Veredicto: ⚠️ PROVAVELMENTE CONFORME** *(design favorece performance, mas não foi medido)*

---

## 6. Objetivos Específicos do Projeto — Análise

| Objetivo | Status | Detalhe |
|----------|--------|---------|
| CRUD completo para clientes, serviços e produtos | ✅ IMPLEMENTADO | Clientes: completo. Serviços: completo. Produtos: completo. Profissionais: completo. |
| Agendamento inteligente com validação de choque | ✅ IMPLEMENTADO | `existeConflito()` em `AgendamentoRepository.java:88` |
| Baixa automática de estoque após conclusão de serviços | ❌ NÃO IMPLEMENTADO | `concluirAgendamento()` não deduz `produto.quantidade_estoque` via `servico_produto` |
| Relatórios de produtividade e faturamento mensal | ⚠️ PARCIAL | Faturamento por período ✅; relatório de estoque ✅; ranking de serviços ❌ |

---

## 7. Escopo — Verificação do Que Foi e Não Foi Feito

### ✅ O que DEVERIA ser feito (e foi):
| Item do Escopo | Status |
|----------------|--------|
| Gestão de vendas e agendamentos | ✅ Implementado |
| Controle de estoque com alertas de nível mínimo | ✅ Implementado (alerta) |
| Autenticação de usuários | ✅ Implementado |
| Persistência em banco de dados | ✅ Implementado |

### ❌ Restrições do escopo que foram respeitadas:
| Restrição | Status |
|-----------|--------|
| Sem interface gráfica (GUI) | ✅ Respeitada |
| Sem processamento de pagamento via cartão | ✅ Respeitada |
| Sem integração com nuvem (AWS) | ✅ Respeitada |

---

## 8. Itens Implementados Além do Especificado (Bônus)

O projeto entregou funcionalidades não previstas no documento de requisitos:

| Funcionalidade Extra | Localização | Valor Agregado |
|---------------------|-------------|----------------|
| 3º nível de usuário (CLIENTE) com menu próprio | `Main.java:180` | Clientes podem consultar serviços |
| Sistema de fidelidade (cliente "frequente" com 10% off) | `AgendamentoService.java:99–121` | Regra de negócio adicional |
| Venda avulsa de produtos | `VendaAvulsaService.java` | Permite vender produtos sem serviço |
| Fechamento de dia por profissional | `VendaAvulsaService.fecharDia()` | Controle de caixa diário |
| Soft delete (exclusão lógica) de usuários | `UsuarioRepository.deletarLogico()` | Evita perda de histórico |
| Flyway para versionamento de banco | `FlywayConfig.java` | Reprodutibilidade do ambiente |
| Docker Compose | `docker-compose.yml` | Ambiente padronizado |
| JavaDoc nos principais métodos | `Main.java`, `RelatorioService.java` | Documentação inline |

---

## 9. Sumário Geral de Conformidade

### Requisitos Funcionais

| ID | Requisito | Prioridade | Status | Conformidade |
|----|-----------|-----------|--------|--------------|
| RF01 | Autenticar | Essencial | ✅ Completo | 100% |
| RF02 | Manter Clientes | Essencial | ✅ Completo | 100% |
| RF03 | Gerenciar Agenda | Essencial | ✅ Completo | 100% |
| RF04 | Baixa de Estoque | **Essencial** | ❌ **Incompleto** | **50%** |
| RF05 | Alerta de Insumo | Importante | ⚠️ Parcial | 60% |
| RF06 | Relatórios | Desejável | ⚠️ Parcial | 80% |

### Requisitos Não Funcionais

| ID | Requisito | Status | Conformidade |
|----|-----------|--------|--------------|
| RNF01 | Persistência PostgreSQL | ✅ Completo | 100% |
| RNF02 | Ambiente local | ✅ Completo | 95% |
| RNF03 | Interface CLI com try-catch | ✅ Completo | 100% |
| RNF04 | Desempenho < 2s | ⚠️ Não medido | — |

### Cronograma

| Entrega | Prazo | Status |
|---------|-------|--------|
| 1ª — Documentação | 30/03/2026 | ✅ Entregue |
| 2ª — Diagramas (DER + MER) | 27/04/2026 | ❌ **Atrasada** |
| 3ª — Back-end | 25/05/2026 | 🔄 Em andamento |
| 4ª — Aplicação final | 22/06/2026 | ⏳ Pendente |

---

## 10. Itens Faltantes — Plano de Ação por Prioridade

### 🔴 CRÍTICO — Essencial para o sistema funcionar conforme o contrato

---

#### FALTA-1 · RF04 — Baixa automática de estoque ao concluir agendamento

**O que falta:** Quando um agendamento é concluído, o sistema deve deduzir do estoque a quantidade de cada produto (`servico_produto`) utilizado nos serviços do agendamento.

**Onde implementar:** `AgendamentoService.concluirAgendamento()` — após `a.setStatus(CONCLUIDO)`:

```java
// Adicionar em AgendamentoService.java, dentro de concluirAgendamento()
// após: a.setStatus(StatusAgendamento.CONCLUIDO);

for (AgendamentoServico as : a.getServicos()) {
    Servico servico = as.getServico();
    for (ServicoProduto sp : servico.getProdutosUtilizados()) {
        Produto produto = sp.getProduto();
        int novoEstoque = produto.getQuantidadeEstoque() - as.getQuantidade();
        if (novoEstoque < 0) novoEstoque = 0; // trigger SQL tb protege
        produto.setQuantidadeEstoque(novoEstoque);
        produtoRepo.salvar(produto);
    }
}
```

**Adicionar repositório de produto em `AgendamentoService`:**
```java
private final ProdutoRepository produtoRepo = new ProdutoRepository();
```

**Alternativa via Trigger SQL** (mais robusta — adicionar em `V6__create_triggers.sql`):
```sql
CREATE OR REPLACE FUNCTION fn_baixa_estoque_servico()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'CONCLUIDO' AND OLD.status IS DISTINCT FROM 'CONCLUIDO' THEN
        UPDATE produto p
        SET quantidade_estoque = quantidade_estoque - ags.quantidade
        FROM agendamento_servico ags
        JOIN servico_produto sp ON sp.servico_id = ags.servico_id
        WHERE ags.agendamento_id = NEW.id
          AND sp.produto_id = p.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_baixa_estoque_servico
    AFTER UPDATE ON agendamento
    FOR EACH ROW EXECUTE FUNCTION fn_baixa_estoque_servico();
```

---

#### FALTA-2 · RF05 — Bloqueio de agendamento quando material está insuficiente

**O que falta:** Antes de confirmar um agendamento, verificar se os produtos necessários para os serviços têm estoque suficiente.

**Onde implementar:** `AgendamentoService.criarAgendamento()` — após a verificação de conflito:

```java
// Adicionar em AgendamentoService.java, dentro de criarAgendamento()
// após a verificação de conflito existente (linha ~37)

for (AgendamentoServico as : agendamento.getServicos()) {
    for (ServicoProduto sp : as.getServico().getProdutosUtilizados()) {
        Produto produto = sp.getProduto();
        if (produto.getQuantidadeEstoque() < as.getQuantidade()) {
            System.out.println("Material insuficiente: " + produto.getNome()
                + " (disponível: " + produto.getQuantidadeEstoque() + ")");
            return;
        }
    }
}
```

---

### 🟡 IMPORTANTE — 2ª Entrega vencida (diagramas)

---

#### FALTA-3 · DER de Peter Chen (2ª entrega — prazo 27/04/2026 — VENCIDO)

**O que falta:** Diagrama com notação de Peter Chen contendo:
- Entidades (retângulos): `USUARIO`, `CLIENTE`, `PROFISSIONAL`, `SERVICO`, `PRODUTO`, `AGENDAMENTO`, `VENDA_AVULSA`, `TRANSACAO_FINANCEIRA`
- Relacionamentos (losangos): REALIZA, EXECUTA, CONTÉM, UTILIZA, GERA
- Atributos (elipses): campos de cada entidade
- Cardinalidades: 1:N, N:M

**Ferramentas sugeridas:** draw.io, brModelo 3.0, Lucidchart

**Onde salvar:**
```
docs/der_peter_chen.png
```

---

#### FALTA-4 · MER — Modelo Entidade-Relacionamento lógico (2ª entrega — vencido)

**O que falta:** Modelo lógico com tabelas, colunas, tipos, PKs, FKs e cardinalidades.

**Como gerar rapidamente:** Usar o DBeaver com o banco rodando → Menu "Database" → "New ER Diagram" → exportar como PNG.

**Onde salvar:**
```
docs/mer_logico.png
```

---

### 🟢 DESEJÁVEL — Melhora a conformidade com RF06

---

#### FALTA-5 · RF06 — Ranking de serviços por período

**O que falta:** Relatório mostrando os serviços mais realizados (frequência) e maior faturamento no período.

**Adicionar em `RelatorioService.java`:**
```java
public void gerarRankingServicos(LocalDate inicio, LocalDate fim) {
    try (EntityManager em = JpaUtil.getEntityManager()) {
        List<Object[]> resultado = em.createQuery(
            "SELECT as.servico.descricao, COUNT(as.id), SUM(as.precoAplicado * as.quantidade) " +
            "FROM AgendamentoServico as " +
            "JOIN as.agendamento a " +
            "WHERE a.dataHoraInicio BETWEEN :inicio AND :fim " +
            "AND a.status = 'CONCLUIDO' " +
            "GROUP BY as.servico.descricao " +
            "ORDER BY COUNT(as.id) DESC", Object[].class)
            .setParameter("inicio", inicio.atStartOfDay())
            .setParameter("fim", fim.atTime(23, 59, 59))
            .getResultList();

        System.out.println("=== Ranking de Serviços ===");
        int pos = 1;
        for (Object[] row : resultado) {
            System.out.printf(" %dº %s | %dx realizados | R$ %.2f%n",
                pos++, row[0], row[1], row[2]);
        }
    } catch (Exception e) {
        System.out.println("Erro ao gerar ranking: " + e.getMessage());
    }
}
```

---

## 11. Dashboard de Status Final

```
╔══════════════════════════════════════════════════════════════════╗
║           CONFORMIDADE GERAL — NailGestor vs. Requisitos         ║
╠══════════════════════════════════════════════╦═══════════════════╣
║ REQUISITOS FUNCIONAIS                        ║                   ║
║  RF01 Autenticar               [ESSENCIAL]   ║ ✅ 100% CONFORME  ║
║  RF02 Manter Clientes          [ESSENCIAL]   ║ ✅ 100% CONFORME  ║
║  RF03 Gerenciar Agenda         [ESSENCIAL]   ║ ✅ 100% CONFORME  ║
║  RF04 Baixa de Estoque         [ESSENCIAL]   ║ ❌  50% INCOMPLETO ║
║  RF05 Alerta de Insumo         [IMPORTANTE]  ║ ⚠️  60% PARCIAL   ║
║  RF06 Relatórios               [DESEJÁVEL]   ║ ⚠️  80% PARCIAL   ║
╠══════════════════════════════════════════════╬═══════════════════╣
║ REQUISITOS NÃO FUNCIONAIS                    ║                   ║
║  RNF01 Persistência PostgreSQL               ║ ✅ 100% CONFORME  ║
║  RNF02 Ambiente local                        ║ ✅  95% CONFORME  ║
║  RNF03 Interface CLI + try-catch             ║ ✅ 100% CONFORME  ║
║  RNF04 Desempenho < 2s                       ║ ⚠️  N/A (não med.) ║
╠══════════════════════════════════════════════╬═══════════════════╣
║ CRONOGRAMA                                   ║                   ║
║  1ª Entrega (30/03) — Documentação           ║ ✅ ENTREGUE       ║
║  2ª Entrega (27/04) — Diagramas DER+MER      ║ ❌ ATRASADA       ║
║  3ª Entrega (25/05) — Back-end               ║ 🔄 EM ANDAMENTO   ║
║  4ª Entrega (22/06) — App final              ║ ⏳ PENDENTE       ║
╠══════════════════════════════════════════════╬═══════════════════╣
║ CONFORMIDADE GERAL ESTIMADA                  ║     ~78%          ║
╚══════════════════════════════════════════════╩═══════════════════╝
```

---

*Documento gerado automaticamente por análise estática do código-fonte e cruzamento com a especificação de requisitos.*
*Para dúvidas, consultar os arquivos `AUDITORIA_PM2.md` e `AUDITORIA_RUBRICA_OFICIAL.md` na raiz do projeto.*
