-- trigger 5: baixa automatica de estoque ao concluir agendamento
-- complementa a logica Java em AgendamentoService.concluirAgendamento()
-- quando um agendamento muda para status CONCLUIDO, deduz do estoque
-- a quantidade de cada produto usado nos servicos do agendamento

create or replace function fn_baixa_estoque_servico()
returns trigger as $$
begin
    if new.status = 'CONCLUIDO' and old.status is distinct from 'CONCLUIDO' then
        update produto p
        set quantidade_estoque = quantidade_estoque - ags.quantidade
        from agendamento_servico ags
        join servico_produto sp on sp.servico_id = ags.servico_id
        where ags.agendamento_id = new.id
          and sp.produto_id = p.id
          and p.quantidade_estoque >= ags.quantidade;
    end if;
    return new;
end;
$$ language plpgsql;

create trigger trg_baixa_estoque_servico
    after update on agendamento
    for each row execute function fn_baixa_estoque_servico();
