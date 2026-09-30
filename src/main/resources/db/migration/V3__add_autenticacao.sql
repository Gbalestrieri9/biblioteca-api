-- =============================================================================
-- V3: Autenticação — adiciona login (e-mail/senha) e papel de acesso aos
-- usuários.
--
-- Os usuários cadastrados pelo seed (V2) não tinham senha/papel — as colunas
-- são adicionadas como opcionais, um hash padrão é aplicado aos registros já
-- existentes e só então as restrições NOT NULL/CHECK são aplicadas, para não
-- quebrar dados já persistidos nem editar uma migration anterior (o Flyway
-- não permite alterar uma migration já aplicada).
--
-- Os hashes abaixo são bcrypt ($2b$, compatível com o BCryptPasswordEncoder
-- do Spring Security — só muda a letra de versão do prefixo, não o
-- algoritmo). São de senhas de demonstração; troque-as em qualquer ambiente
-- real.
-- =============================================================================

ALTER TABLE usuarios ADD COLUMN senha VARCHAR(255);
ALTER TABLE usuarios ADD COLUMN papel VARCHAR(20);

-- Hash bcrypt da senha "Mudar@123" — senha padrão e temporária para os
-- usuários que já existiam antes da autenticação (seed V2: Ana, Bruno,
-- Carla). Troque-a em qualquer ambiente real antes de usar essas contas
-- para login.
UPDATE usuarios
SET senha = '$2b$12$WNiNT.2VV//7GJEssS4OsupeDztn5OcQ4B1xbZ0Xeg22WdBgkDVlS',
    papel = 'USUARIO'
WHERE senha IS NULL;

ALTER TABLE usuarios ALTER COLUMN senha SET NOT NULL;
ALTER TABLE usuarios ALTER COLUMN papel SET NOT NULL;
ALTER TABLE usuarios ADD CONSTRAINT ck_usuarios_papel CHECK (papel IN ('BIBLIOTECARIO', 'USUARIO'));

-- Usuário BIBLIOTECARIO de demonstração — necessário para poder usar os
-- endpoints administrativos (cadastro/edição/exclusão de livros e usuários)
-- assim que a aplicação sobe. Hash bcrypt da senha "Admin@123".
INSERT INTO usuarios (nome, email, senha, papel, data_cadastro) VALUES
    ('Administrador (demonstração)', 'admin@biblioteca.com',
     '$2b$12$5nrKgidkn5ecQx5UdcGVJuhsP7KOKA4Z1TlGmNiO5rg/nAJig2U..', 'BIBLIOTECARIO', now());
