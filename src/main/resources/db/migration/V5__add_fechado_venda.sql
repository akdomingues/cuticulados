-- Adiciona o campo de controle de fechamento diário nas vendas avulsas.
-- Quando um profissional finaliza o dia, todas as suas vendas daquele dia
-- são marcadas como fechadas (fechado = true), evitando recontagem.
alter table venda_avulsa
    add column if not exists fechado boolean not null default false;

-- Índice para acelerar a consulta de vendas abertas por profissional e data
create index if not exists idx_venda_fechado on venda_avulsa(fechado);