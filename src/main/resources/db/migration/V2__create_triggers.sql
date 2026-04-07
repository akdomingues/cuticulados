-- 1 trigger adiciona contador ao finalziar um atendimento
create or replace function fn_incrementa_atendimentos_mes()
returns trigger as $$
begin
    if new.status = 'concluido' and old.status is distinct from 'concluido' then
update cliente
set total_atendimentos_mes = total_atendimentos_mes + 1
where id = new.cliente_id;
end if;
return new;
end;
$$ language plpgsql;

create or replace trigger trg_incrementa_atendimentos_mes
after insert or update on agendamento
                              for each row execute function fn_incrementa_atendimentos_mes();

-- 2 trigger promove cliente novo para frequente
create or replace function fn_promove_cliente_frequente()
returns trigger as $$
begin
    if new.total_atendimentos_mes >= 3 then
        new.tipo_cliente := 'frequente';
end if;
return new;
end;
$$ language plpgsql;

create or replace trigger trg_promove_cliente_frequente
before update on cliente
                     for each row execute function fn_promove_cliente_frequente();

-- 3 trigger impede estoque negativo
create or replace function fn_impede_estoque_negativo()
returns trigger as $$
begin
    if new.quantidade_estoque < 0 then
        raise exception 'estoque insuficiente';
end if;
return new;
end;
$$ language plpgsql;

create or replace trigger trg_impede_estoque_negativo
before update on produto
                     for each row execute function fn_impede_estoque_negativo();

-- 4 trigger atualiza updated_at
create or replace function fn_set_updated_at()
returns trigger as $$
begin
    new.updated_at := now();
return new;
end;
$$ language plpgsql;

create or replace trigger trg_updated_at_agendamento
before update on agendamento
                     for each row execute function fn_set_updated_at();

create or replace trigger trg_updated_at_servico
before update on servico
                  for each row execute function fn_set_updated_at();

create or replace trigger trg_updated_at_produto
before update on produto
                  for each row execute function fn_set_updated_at();