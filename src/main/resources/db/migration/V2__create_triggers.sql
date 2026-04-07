-- incrementa o contador de atendimentos do cliente qnd o agendamento vai pra 'concluido'
create or replace function fn_incrementa_atendimentos()
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

create trigger trg_incrementa_atendimentos
    after update on agendamento
    for each row execute function fn_incrementa_atendimentos();


-- quando o cliente bate 3 atendimentos no mes, muda o tipo pra 'frequente'
create or replace function fn_promove_cliente()
returns trigger as $$
begin
    if new.total_atendimentos_mes >= 3 then
        new.tipo_cliente := 'frequente';
    end if;
    return new;
end;
$$ language plpgsql;

create trigger trg_promove_cliente
    before update on cliente
    for each row execute function fn_promove_cliente();


-- barrar update que tente deixar estoque negativo
create or replace function fn_estoque_negativo()
returns trigger as $$
begin
    if new.quantidade_estoque < 0 then
        raise exception 'estoque nao pode ficar negativo';
    end if;
    return new;
end;
$$ language plpgsql;

create trigger trg_estoque_negativo
    before update on produto
    for each row execute function fn_estoque_negativo();


-- atualiza o updated_at automaticamente
create or replace function fn_atualiza_timestamp()
returns trigger as $$
begin
    new.updated_at = now();
    return new;
end;
$$ language plpgsql;

create trigger trg_updated_agendamento
    before update on agendamento
    for each row execute function fn_atualiza_timestamp();

create trigger trg_updated_servico
    before update on servico
    for each row execute function fn_atualiza_timestamp();

create trigger trg_updated_produto
    before update on produto
    for each row execute function fn_atualiza_timestamp();
