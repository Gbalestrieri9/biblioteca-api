package com.postech.biblioteca.repository.specification;

import com.postech.biblioteca.entity.Livro;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Predicados dinâmicos e compostos para a busca de livros, usados pelo
 * requisito "Consultar livros" (filtros por título, autor, ISBN e
 * disponibilidade). Cada método retorna {@code null} quando o filtro não foi
 * informado, para que o Spring Data ignore aquele predicado ao combinar as
 * specifications — assim a consulta SQL final contém apenas as cláusulas
 * WHERE realmente necessárias.
 */
public final class LivroSpecification {

    private LivroSpecification() {
    }

    public static Specification<Livro> comTitulo(String titulo) {
        if (!StringUtils.hasText(titulo)) {
            return null;
        }
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("titulo")), "%" + titulo.toLowerCase() + "%");
    }

    public static Specification<Livro> comAutor(String autor) {
        if (!StringUtils.hasText(autor)) {
            return null;
        }
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("autor")), "%" + autor.toLowerCase() + "%");
    }

    public static Specification<Livro> comIsbn(String isbn) {
        if (!StringUtils.hasText(isbn)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("isbn"), isbn);
    }

    public static Specification<Livro> disponivel(Boolean disponivel) {
        if (disponivel == null) {
            return null;
        }
        return disponivel
                ? (root, query, cb) -> cb.greaterThan(root.get("quantidadeDisponivel"), 0)
                : (root, query, cb) -> cb.equal(root.get("quantidadeDisponivel"), 0);
    }

    /**
     * Combina os filtros informados com AND, ignorando os que forem nulos.
     */
    public static Specification<Livro> comFiltros(String titulo, String autor, String isbn, Boolean disponivel) {
        Specification<Livro> spec = Specification.where(null);
        for (Specification<Livro> filtro : java.util.Arrays.asList(
                comTitulo(titulo), comAutor(autor), comIsbn(isbn), disponivel(disponivel))) {
            if (filtro != null) {
                spec = spec.and(filtro);
            }
        }
        return spec;
    }
}
