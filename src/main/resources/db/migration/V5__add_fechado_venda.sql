-- quando um profissional finalizar o dia, todas as suas vendas daquele dia
-- são marcadas como fechadas (fechado = true)
alter table venda_avulsa
    add column if not exists fechado boolean not null default false;

-- indices para melhorar o desempenho das consultas igual na V1
create index if not exists idx_venda_fechado on venda_avulsa(fechado);