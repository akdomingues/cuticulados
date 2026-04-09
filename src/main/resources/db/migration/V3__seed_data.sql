-- Inserção do usuário administrador (Vitória)
insert into usuario (nome, email, login, senha, tipo) values
    ('vitória (admin)', 'vitoria@cuticulados.com', 'admin', 'admin123', 'admin')
    on conflict (login) do nothing;

-- Inserção do profissional (Alok)
insert into usuario (nome, email, login, senha, tipo) values
    ('alok', 'alok@cuticulados.com', 'alok', 'alok123', 'profissional')
    on conflict (login) do nothing;

insert into profissional (id, especialidade)
select id, 'nail designer' from usuario where login = 'alok'
    on conflict do nothing;

-- Inserção de clientes de exemplo
insert into usuario (nome, email, login, senha, tipo) values
                                                          ('ana silva',     'ana@cuticulados.com',     'cli_12345678900', 'cliente123', 'cliente'),
                                                          ('maria souza',   'maria@cuticulados.com',   'cli_98765432100', 'cliente123', 'cliente'),
                                                          ('juliana lima',  'juliana@cuticulados.com', 'cli_11122233344', 'cliente123', 'cliente')
    on conflict (login) do nothing;

insert into cliente (id, cpf, telefone, tipo_cliente)
select id, '123.456.789-00', '(45) 99999-1111', 'frequente'
from usuario where login = 'cli_12345678900' on conflict do nothing;

insert into cliente (id, cpf, telefone, tipo_cliente)
select id, '987.654.321-00', '(45) 99999-2222', 'novo'
from usuario where login = 'cli_98765432100' on conflict do nothing;

insert into cliente (id, cpf, telefone, tipo_cliente)
select id, '111.222.333-44', '(45) 99999-3333', 'novo'
from usuario where login = 'cli_11122233344' on conflict do nothing;

-- Inserção dos serviços do salão
insert into servico (descricao, valor_base, duracao_minutos) values
                                                                 ('manicure',              35.00, 45),
                                                                 ('pedicure',              40.00, 60),
                                                                 ('alongamento de unhas', 120.00, 90),
                                                                 ('esmaltação em gel',     60.00, 60),
                                                                 ('nail art (por unha)',    8.00, 30)
    on conflict do nothing;

-- Inserção dos produtos do estoque
insert into produto (nome, quantidade_estoque, quantidade_minima, preco_custo, preco_venda) values
                                                                                                ('esmalte vermelho',  10, 1,  5.00, 10.00),
                                                                                                ('esmalte rosa',       8, 1,  5.00, 10.00),
                                                                                                ('acetona',            5, 1,  3.50,  7.50),
                                                                                                ('lixa',              20, 1,  1.00,  2.50),
                                                                                                ('base',               4, 1,  8.00, 16.00),
                                                                                                ('gliter',             4, 1,  8.00, 16.00),
                                                                                                ('gel uv',             3, 1, 25.00, 50.00)
    on conflict do nothing;

-- Alok habilitado para todos os serviços (CROSS JOIN entre profissional e serviço)
insert into profissional_servico (profissional_id, servico_id)
select p.id, s.id
from profissional p
         join usuario u on u.id = p.id
         cross join servico s
where u.login = 'alok'
    on conflict do nothing;

-- Produtos associados aos serviços de manicure e pedicure
insert into servico_produto (servico_id, produto_id)
select s.id, p.id
from servico s
         cross join produto p
where s.descricao in ('manicure', 'pedicure')
    on conflict do nothing;