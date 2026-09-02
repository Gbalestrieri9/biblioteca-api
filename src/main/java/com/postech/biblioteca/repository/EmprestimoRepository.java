package com.postech.biblioteca.repository;

import com.postech.biblioteca.entity.Emprestimo;
import com.postech.biblioteca.enums.StatusEmprestimo;
import com.postech.biblioteca.repository.projection.LivroMaisEmprestadoProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositório de {@link Emprestimo}.
 *
 * <p>As consultas usam {@link Pageable} para paginação (evitando carregar a
 * base inteira em memória) e agregações no próprio banco (GROUP BY / COUNT)
 * para o relatório de livros mais emprestados, em vez de trazer todos os
 * registros para agregar em Java. {@link JpaSpecificationExecutor} permite
 * combinar os filtros opcionais de status e usuário em uma única consulta
 * (ver {@code EmprestimoSpecification}).</p>
 */
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long>, JpaSpecificationExecutor<Emprestimo> {

    Page<Emprestimo> findByStatus(StatusEmprestimo status, Pageable pageable);

    Page<Emprestimo> findByUsuarioId(Long usuarioId, Pageable pageable);

    boolean existsByLivroIdAndUsuarioIdAndStatus(Long livroId, Long usuarioId, StatusEmprestimo status);

    long countByLivroIdAndStatus(Long livroId, StatusEmprestimo status);

    List<Emprestimo> findByStatusAndDataPrevistaDevolucaoBefore(StatusEmprestimo status, LocalDate data);

    /**
     * Relatório "livros mais emprestados": agrupa empréstimos por livro e
     * ordena pela contagem decrescente. Combinado com um {@link Pageable} de
     * tamanho 20 (ver {@code AppConstants#TOP_LIVROS_MAIS_EMPRESTADOS}),
     * implementa o requisito "Lista dos 20 livros mais emprestados" fazendo
     * o LIMIT/OFFSET no próprio banco.
     */
    @Query("""
            SELECT e.livro.id AS livroId,
                   e.livro.titulo AS titulo,
                   e.livro.autor AS autor,
                   e.livro.isbn AS isbn,
                   COUNT(e) AS totalEmprestimos
            FROM Emprestimo e
            GROUP BY e.livro.id, e.livro.titulo, e.livro.autor, e.livro.isbn
            ORDER BY COUNT(e) DESC
            """)
    List<LivroMaisEmprestadoProjection> buscarLivrosMaisEmprestados(Pageable pageable);

    @Query("""
            SELECT e FROM Emprestimo e
            WHERE e.usuario.id = :usuarioId AND e.status = :status
            """)
    List<Emprestimo> buscarPorUsuarioEStatus(@Param("usuarioId") Long usuarioId,
                                              @Param("status") StatusEmprestimo status);
}
