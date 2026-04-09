-- tipos de usuario, status do agendamento e tipo do cliente
-- criados como enums do postgresql (convertidos para varchar na V4)
create type status_agendamento as enum ('pendente', 'concluido', 'cancelado');
create type tipo_usuario as enum ('admin', 'cliente', 'profissional');
create type tipo_cliente_enum as enum ('novo', 'frequente');

-- Tabela principal de usuarios (usada por herança JPA JOINED)
create table usuario (
                         id         bigserial    primary key,
                         nome       varchar(100) not null,
                         email      varchar(100) unique not null,
                         login      varchar(50)  unique not null,
                         senha      varchar(255) not null,
                         tipo       tipo_usuario not null,
                         created_at timestamptz  not null default now(),
                         updated_at timestamptz  not null default now(),
                         deleted_at timestamptz
);

-- Tabela de profissionais (herança de usuario)
create table profissional (
                              id            bigint primary key references usuario(id) on delete cascade,
                              especialidade varchar(100) not null
);

-- Tabela de clientes (herança de usuario)
create table cliente (
                         id                     bigint primary key references usuario(id) on delete cascade,
                         cpf                    varchar(14) unique not null,
                         telefone               varchar(20) not null,
                         tipo_cliente           tipo_cliente_enum not null default 'novo',
                         total_atendimentos_mes integer not null default 0
);

-- Tabela de serviços oferecidos pelo salão
create table servico (
                         id               bigserial   primary key,
                         descricao        varchar(200) not null,
                         valor_base       numeric(10,2) not null check (valor_base > 0),
                         duracao_minutos  integer not null check (duracao_minutos > 0),
                         created_at       timestamptz not null default now(),
                         updated_at       timestamptz not null default now()
);

-- Tabela de produtos do estoque
create table produto (
                         id                 bigserial    primary key,
                         nome               varchar(100) not null,
                         quantidade_estoque integer not null default 0 check (quantidade_estoque >= 0),
                         quantidade_minima  integer not null default 0,
                         preco_custo        numeric(10,2) not null,
                         preco_venda        numeric(10,2) not null,
                         created_at         timestamptz not null default now(),
                         updated_at         timestamptz not null default now()
);

-- Tabela intermediária: quais serviços cada profissional realiza (ManyToMany)
create table profissional_servico (
                                      profissional_id bigint not null references profissional(id) on delete cascade,
                                      servico_id      bigint not null references servico(id)      on delete cascade,
                                      primary key (profissional_id, servico_id)
);

-- Tabela de agendamentos
create table agendamento (
                             id               bigserial    primary key,
                             data_hora_inicio timestamptz  not null,
                             data_hora_fim    timestamptz  not null,
                             status           status_agendamento not null default 'pendente',
                             valor_final      numeric(10,2) not null default 0,
                             cliente_id       bigint not null references cliente(id),
                             profissional_id  bigint not null references profissional(id),
                             created_at       timestamptz not null default now(),
                             updated_at       timestamptz not null default now()
);

-- Tabela de serviços realizados dentro de cada agendamento
create table agendamento_servico (
                                     id               bigserial    primary key,
                                     agendamento_id   bigint not null references agendamento(id) on delete cascade,
                                     servico_id       bigint not null references servico(id),
                                     quantidade       integer not null default 1,
                                     preco_aplicado   numeric(10,2) not null,
                                     desconto_aplicado numeric(10,2) not null default 0,
                                     tempo_real       integer,
                                     created_at       timestamptz not null default now()
);

-- Tabela intermediária: quais produtos são usados em cada serviço
create table servico_produto (
                                 id         bigserial primary key,
                                 servico_id bigint not null references servico(id) on delete cascade,
                                 produto_id bigint not null references produto(id),
                                 created_at timestamptz not null default now()
);

-- Tabela de vendas avulsas de produtos
create table venda_avulsa (
                              id              bigserial    primary key,
                              produto_id      bigint not null references produto(id),
                              quantidade      integer not null,
                              preco_unitario  numeric(10,2) not null,
                              total           numeric(10,2) not null,
                              data_venda      timestamptz  not null default now(),
                              profissional_id bigint not null references profissional(id)
);

-- Tabela de movimentações financeiras do caixa
create table transacao_financeira (
                                      id              bigserial    primary key,
                                      tipo            varchar(10)  not null,
                                      descricao       varchar(255) not null,
                                      valor           numeric(10,2) not null,
                                      data_transacao  timestamptz  not null default now(),
                                      agendamento_id  bigint unique references agendamento(id),
                                      venda_avulsa_id bigint unique references venda_avulsa(id)
);

-- Índices para melhorar performance das consultas mais frequentes
create index idx_usuario_deleted_at      on usuario(deleted_at);
create index idx_agendamento_cliente     on agendamento(cliente_id);
create index idx_agendamento_profissional on agendamento(profissional_id);
create index idx_agendamento_data        on agendamento(data_hora_inicio);
create index idx_agend_serv_agend        on agendamento_servico(agendamento_id);
create index idx_agend_serv_serv         on agendamento_servico(servico_id);
create index idx_serv_prod_serv          on servico_produto(servico_id);
create index idx_serv_prod_prod          on servico_produto(produto_id);
create index idx_venda_data              on venda_avulsa(data_venda);
create index idx_transacao_data          on transacao_financeira(data_transacao);