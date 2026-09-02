package com.postech.biblioteca.enums;

/**
 * Representa o estado de uma {@link com.postech.biblioteca.entity.Reserva}.
 */
public enum StatusReserva {

    /** Reserva aguardando disponibilidade do livro. */
    ATIVA,

    /** Reserva convertida em empréstimo. */
    ATENDIDA,

    /** Reserva cancelada pelo usuário ou pelo bibliotecário. */
    CANCELADA
}
