-- Migration corretiva: converte os enums do PostgreSQL (lowercase)
-- para varchar em maiúsculas, compatível com @Enumerated(EnumType.STRING) do JPA.

-- Passo 1: remove os valores padrão que dependem dos tipos enum antigos
alter table usuario    alter column tipo         drop default;
alter table cliente    alter column tipo_cliente  drop default;
alter table agendamento alter column status       drop default;

-- Passo 2: converte as colunas de enum para varchar e aplica upper()
-- para garantir compatibilidade com os ENUMs Java (ADMIN, PENDENTE, etc.)
alter table usuario
alter column tipo type varchar(20) using upper(tipo::text);

alter table cliente
alter column tipo_cliente type varchar(20) using upper(tipo_cliente::text);

alter table agendamento
alter column status type varchar(20) using upper(status::text);

-- Passo 3: remove os tipos enum do PostgreSQL que não são mais necessários
drop type if exists tipo_usuario;
drop type if exists tipo_cliente_enum;
drop type if exists status_agendamento;