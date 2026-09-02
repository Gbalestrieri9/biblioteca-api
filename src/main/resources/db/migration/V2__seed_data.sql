-- =============================================================================
-- V2: Dados de exemplo (seed) para facilitar testes manuais e avaliação.
-- Não é necessário para o funcionamento do sistema; pode ser removido em
-- produção retirando este arquivo antes do deploy.
-- =============================================================================

INSERT INTO livros (titulo, autor, isbn, quantidade_total, quantidade_disponivel, data_cadastro) VALUES
    ('Clean Code', 'Robert C. Martin', '9780132350884', 3, 3, now()),
    ('Effective Java', 'Joshua Bloch', '9780134685991', 2, 2, now()),
    ('Domain-Driven Design', 'Eric Evans', '9780321125217', 2, 2, now()),
    ('Clean Architecture', 'Robert C. Martin', '9780134494166', 2, 2, now()),
    ('Java Concurrency in Practice', 'Brian Goetz', '9780321349606', 1, 1, now()),
    ('Head First Design Patterns', 'Eric Freeman', '9781492078005', 3, 3, now()),
    ('Spring in Action', 'Craig Walls', '9781617294945', 2, 2, now()),
    ('Refactoring', 'Martin Fowler', '9780134757599', 2, 2, now()),
    ('Building Microservices', 'Sam Newman', '9781492034025', 2, 2, now()),
    ('O Programador Pragmático', 'Andrew Hunt', '9788575225790', 2, 2, now());

INSERT INTO usuarios (nome, email, data_cadastro) VALUES
    ('Ana Souza', 'ana.souza@example.com', now()),
    ('Bruno Lima', 'bruno.lima@example.com', now()),
    ('Carla Mendes', 'carla.mendes@example.com', now());
