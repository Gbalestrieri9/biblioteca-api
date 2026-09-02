package com.postech.biblioteca.repository;

import com.postech.biblioteca.entity.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Repositório de {@link Livro}.
 *
 * <p>Estende {@link JpaSpecificationExecutor} para permitir consultas
 * dinâmicas e paginadas por título, autor e ISBN (ver {@code
 * LivroSpecification}), evitando múltiplos métodos derivados combinatórios e
 * mantendo as consultas eficientes (apenas os predicados informados entram no
 * WHERE).</p>
 */
public interface LivroRepository extends JpaRepository<Livro, Long>, JpaSpecificationExecutor<Livro> {

    Optional<Livro> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);
}
