package com.postech.biblioteca.repository.specification;

import com.postech.biblioteca.entity.Emprestimo;
import com.postech.biblioteca.enums.StatusEmprestimo;
import org.springframework.data.jpa.domain.Specification;

public final class EmprestimoSpecification {

    private EmprestimoSpecification() {
    }

    public static Specification<Emprestimo> comFiltros(StatusEmprestimo status, Long usuarioId) {
        Specification<Emprestimo> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (usuarioId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("usuario").get("id"), usuarioId));
        }
        return spec;
    }
}
