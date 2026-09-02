package com.postech.biblioteca.repository.specification;

import com.postech.biblioteca.entity.Usuario;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class UsuarioSpecification {

    private UsuarioSpecification() {
    }

    public static Specification<Usuario> comNome(String nome) {
        if (!StringUtils.hasText(nome)) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
    }
}
