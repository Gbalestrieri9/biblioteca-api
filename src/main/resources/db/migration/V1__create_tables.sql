-- =============================================================================
-- V1: Estrutura inicial do Sistema de Biblioteca Online
--
-- Convenção de data/hora: colunas TIMESTAMP WITH TIME ZONE armazenam o
-- instante em UTC (GMT+0); a coluna data_prevista_devolucao é DATE (sem
-- hora), calculada em UTC pela aplicação.
-- =============================================================================

CREATE TABLE livros (
    id                     BIGSERIAL PRIMARY KEY,
    titulo                 VARCHAR(255)              NOT NULL,
    autor                  VARCHAR(255)              NOT NULL,
    isbn                   VARCHAR(20)                NOT NULL,
    quantidade_total       INTEGER                    NOT NULL DEFAULT 1,
    quantidade_disponivel  INTEGER                    NOT NULL DEFAULT 1,
    data_cadastro          TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT now(),
    CONSTRAINT uk_livros_isbn UNIQUE (isbn),
    CONSTRAINT ck_livros_quantidade_disponivel CHECK (quantidade_disponivel >= 0),
    CONSTRAINT ck_livros_quantidade_total CHECK (quantidade_total >= 0),
    CONSTRAINT ck_livros_disponivel_menor_total CHECK (quantidade_disponivel <= quantidade_total)
);

-- Índices usados pelos filtros de busca (título, autor, ISBN) exigidos no
-- requisito "Consultar livros".
CREATE INDEX idx_livros_titulo ON livros (lower(titulo));
CREATE INDEX idx_livros_autor  ON livros (lower(autor));
CREATE INDEX idx_livros_isbn   ON livros (isbn);

CREATE TABLE usuarios (
    id             BIGSERIAL PRIMARY KEY,
    nome           VARCHAR(255)              NOT NULL,
    email          VARCHAR(255)              NOT NULL,
    data_cadastro  TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT now(),
    CONSTRAINT uk_usuarios_email UNIQUE (email)
);

CREATE INDEX idx_usuarios_email ON usuarios (lower(email));

CREATE TABLE emprestimos (
    id                        BIGSERIAL PRIMARY KEY,
    livro_id                  BIGINT                    NOT NULL REFERENCES livros(id),
    usuario_id                BIGINT                    NOT NULL REFERENCES usuarios(id),
    data_emprestimo           TIMESTAMP WITH TIME ZONE  NOT NULL,
    data_prevista_devolucao   DATE                       NOT NULL,
    data_devolucao            TIMESTAMP WITH TIME ZONE,
    status                    VARCHAR(20)                NOT NULL,
    CONSTRAINT ck_emprestimos_status CHECK (status IN ('ATIVO', 'DEVOLVIDO'))
);

-- Índices que suportam os relatórios (livros mais emprestados, empréstimos
-- ativos) e as consultas por usuário, evitando full scans.
CREATE INDEX idx_emprestimos_status         ON emprestimos (status);
CREATE INDEX idx_emprestimos_usuario        ON emprestimos (usuario_id);
CREATE INDEX idx_emprestimos_livro          ON emprestimos (livro_id);
CREATE INDEX idx_emprestimos_data_prevista  ON emprestimos (data_prevista_devolucao);
CREATE INDEX idx_emprestimos_livro_status   ON emprestimos (livro_id, status);

CREATE TABLE reservas (
    id             BIGSERIAL PRIMARY KEY,
    livro_id       BIGINT                    NOT NULL REFERENCES livros(id),
    usuario_id     BIGINT                    NOT NULL REFERENCES usuarios(id),
    data_reserva   TIMESTAMP WITH TIME ZONE  NOT NULL,
    status         VARCHAR(20)                NOT NULL,
    CONSTRAINT ck_reservas_status CHECK (status IN ('ATIVA', 'ATENDIDA', 'CANCELADA'))
);

CREATE INDEX idx_reservas_status       ON reservas (status);
CREATE INDEX idx_reservas_livro_status ON reservas (livro_id, status);
CREATE INDEX idx_reservas_usuario      ON reservas (usuario_id);
