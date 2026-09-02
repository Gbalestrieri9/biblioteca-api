package com.postech.biblioteca.enums;

/**
 * Representa o estado de um {@link com.postech.biblioteca.entity.Emprestimo}.
 *
 * <p>O sistema mantém apenas dois estados persistidos (ATIVO e DEVOLVIDO); a
 * situação de atraso é calculada dinamicamente comparando a data prevista de
 * devolução com a data corrente, evitando a necessidade de um job agendado
 * apenas para transicionar status.</p>
 */
public enum StatusEmprestimo {

    /** Empréstimo em andamento, livro ainda não devolvido. */
    ATIVO,

    /** Empréstimo já finalizado, livro devolvido. */
    DEVOLVIDO
}
