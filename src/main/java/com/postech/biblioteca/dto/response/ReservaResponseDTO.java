package com.postech.biblioteca.dto.response;

import com.postech.biblioteca.enums.StatusReserva;

import java.time.Instant;

public record ReservaResponseDTO(
        Long id,
        LivroResumoDTO livro,
        UsuarioResumoDTO usuario,
        Instant dataReserva,
        StatusReserva status
) {
}
