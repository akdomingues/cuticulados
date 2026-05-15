-- converte os enums do postgresql (lowercase) para varchar em maiusculas
-- remove os valores padrão que dependem dos tipos enum antigos
alter table usuario
    alter column tipo drop default;
alter table cliente
    alter column tipo_cliente drop default;
alter table agendamento
    alter column status drop default;

-- converte as colunas de enum para varchar e aplica upper para ter compatibilidade
-- com os enums tipo de usuario, tipo de cliente e status do agendamento
alter table usuario
alter
column tipo type varchar(20) using upper(tipo::text); -- converte o valor da coluna para texto, aplica o uppercase e redefine o tipo para varchar

alter table cliente
alter
column tipo_cliente type varchar(20) using upper(tipo_cliente::text);

alter table agendamento
alter
column status type varchar(20) using upper(status::text);

-- remove os tipos enum do postgresql que não são mais necessários
drop type if exists tipo_usuario;
drop type if exists tipo_cliente_enum;
drop type if exists status_agendamento;