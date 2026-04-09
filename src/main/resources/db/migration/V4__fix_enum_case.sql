-- 1. Remove os valores padrão que estão segurando a dependência dos Enums antigos
ALTER TABLE usuario ALTER COLUMN tipo DROP DEFAULT;
ALTER TABLE cliente ALTER COLUMN tipo_cliente DROP DEFAULT;
ALTER TABLE agendamento ALTER COLUMN status DROP DEFAULT;

-- 2. Converte a coluna de enum customizado para varchar
-- e atualiza os valores para maiúsculo (compatível com o enum Java)
ALTER TABLE usuario
ALTER COLUMN tipo TYPE varchar(20) USING upper(tipo::text);

ALTER TABLE cliente
ALTER COLUMN tipo_cliente TYPE varchar(20) USING upper(tipo_cliente::text);

ALTER TABLE agendamento
ALTER COLUMN status TYPE varchar(20) USING upper(status::text);

-- 3. Remove os tipos enum do PostgreSQL que não são mais necessários
DROP TYPE IF EXISTS tipo_usuario;
DROP TYPE IF EXISTS tipo_cliente_enum;
DROP TYPE IF EXISTS status_agendamento;

-- (Opcional) Retorna um valor padrão como VARCHAR, se você precisar.
-- Exemplo: ALTER TABLE cliente ALTER COLUMN tipo_cliente SET DEFAULT 'COMUM';