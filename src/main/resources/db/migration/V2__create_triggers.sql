-- trigger 1: incrementa o contador de atendimentos do cliente
-- quando um agendamento é marcado como concluido
create
or replace function fn_incrementa_atendimentos()
returns trigger as $$
begin
    if
new.status = 'concluido' and old.status is distinct from 'concluido' then
update cliente
set total_atendimentos_mes = total_atendimentos_mes + 1
where id = new.cliente_id;
end if;
return new;
end;
$$
language plpgsql;

create trigger trg_incrementa_atendimentos
    after update
    on agendamento
    for each row execute function fn_incrementa_atendimentos();


-- trigger 2: promove cliente para frequente
-- quando ele atinge 3 ou mais atendimentos no mês
create
or replace function fn_promove_cliente()
returns trigger as $$
begin
    if
new.total_atendimentos_mes >= 3 then
        new.tipo_cliente := 'frequente';
end if;
return new;
end;
$$
language plpgsql;

create trigger trg_promove_cliente
    before update
    on cliente
    for each row execute function fn_promove_cliente();


-- trigger 3: impede que o estoque de um produto fique negativo
create
or replace function fn_estoque_negativo()
returns trigger as $$
begin
    if
new.quantidade_estoque < 0 then
        raise exception 'estoque nao pode ficar negativo';
end if;
return new;
end;
$$
language plpgsql;

create trigger trg_estoque_negativo
    before update
    on produto
    for each row execute function fn_estoque_negativo();


-- trigger 4: atualiza o campo updated_at automaticamente
-- aplicado nas tabelas que fazem o uso
create
or replace function fn_atualiza_timestamp()
returns trigger as $$
begin
    new.updated_at
= now();
return new;
end;
$$
language plpgsql;

create trigger trg_updated_agendamento
    before update
    on agendamento
    for each row execute function fn_atualiza_timestamp();

create trigger trg_updated_servico
    before update
    on servico
    for each row execute function fn_atualiza_timestamp();

create trigger trg_updated_produto
    before update
    on produto
    for each row execute function fn_atualiza_timestamp();