CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO usuario (nome, sobrenome, email, senha, ativo, perfil, cep, rua, bairro, cidade, estado, numero)
VALUES
    ('Admin', 'Livraria', 'admin@livraria.com', crypt('Admin@123', gen_salt('bf', 10)), true, 'ADMIN',
     '01001000', 'Praça da Sé', 'Sé', 'São Paulo', 'SP', '1'),
    ('Cliente', 'Demonstração', 'cliente@livraria.com', crypt('Cliente@123', gen_salt('bf', 10)), true, 'CLIENTE',
     '01001000', 'Praça da Sé', 'Sé', 'São Paulo', 'SP', '100');
