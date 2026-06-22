# NailGestor

Sistema de gestão para salão de beleza/estética, desenvolvido em Java 17 com Swing e JPA/Hibernate + PostgreSQL.

## Pré-requisitos

- Java 17 ou superior (JDK)
- Maven 3.8+
- PostgreSQL 14+ rodando na porta **5432**
- Docker (opcional, para subir o banco via docker-compose)

## Configuração do Banco de Dados

### Com Docker
```bash
docker-compose up -d
```

### Sem Docker
Crie um banco PostgreSQL chamado `nailgestor` e um usuário `nailgestor` com senha `nailgestor`:
```sql
CREATE DATABASE nailgestor;
CREATE USER nailgestor WITH PASSWORD 'nailgestor';
GRANT ALL PRIVILEGES ON DATABASE nailgestor TO nailgestor;
```

O Flyway aplica as migrations automaticamente na primeira execução.

## Como Executar

### Windows (recomendado)
Dê duplo clique em `rodar.bat` ou execute no terminal:
```bat
rodar.bat
```

### Linha de comando (Maven)
```bash
mvn clean compile exec:java
```

## Usuários padrão (criados pelas migrations)

| Login | Senha | Perfil |
|-------|-------|--------|
| admin | admin | Administrador |
| prof1 | 1234 | Profissional |
| cliente1 | 1234 | Cliente |

## Funcionalidades

- **Admin**: gerenciar clientes, profissionais, serviços, produtos, agendamentos e vendas avulsas
- **Profissional**: visualizar agendamentos do dia e registrar vendas avulsas
- **Cliente**: visualizar e agendar serviços

## Arquitetura

O projeto segue uma separação em camadas: a interface (Swing) e o `Main` conversam com
os **controllers**, que recebem dados em **Requests (DTOs)**, convertem para entidades
através dos **mappers** e delegam as regras de negócio aos **services**, que por sua vez
persistem os dados via **repositories** (JPA/Hibernate). Cada domínio (agendamento,
cliente, produto, serviço, relatório, usuário e venda) tem seu próprio controller, request
e mapper.

## Estrutura do Projeto

```
src/main/java/org/cuticulados/pm/
├── config/          # JPA e Flyway
├── entity/          # Entidades JPA
├── repository/      # Acesso a dados (JPA)
├── service/         # Regras de negócio
├── controller/      # Controllers por domínio (agendamento, cliente, produto,
│                    #   relatorio, servico, usuario, venda) com Requests (DTOs)
│                    #   e mappers de conversão DTO <-> entidade
└── ui/
    ├── frames/      # Janelas principais (MainFrame, LoginFrame, ...)
    ├── panels/      # Painéis de cada seção
    └── theme/       # Cores, fontes e estilos (AppColors, AppFonts, AppTheme)
```
