package com.postech.biblioteca.repository.projection;

/**
 * Projeção usada pelo relatório de livros mais emprestados. O Spring Data
 * preenche automaticamente cada método com o alias correspondente da query
 * JPQL (ver {@code EmprestimoRepository#buscarLivrosMaisEmprestados}),
 * evitando carregar a entidade {@code Livro} completa quando só os dados
 * agregados são necessários.
 */
public interface LivroMaisEmprestadoProjection {

    Long getLivroId();

    String getTitulo();

    String getAutor();

    String getIsbn();

    Long getTotalEmprestimos();
}
