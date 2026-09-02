package com.postech.biblioteca.dto.response;

import java.time.Instant;
import java.time.LocalDate;

public record RelatorioEmprestimoAtivoDTO(
        Long emprestimoId,
        LivroResumoDTO livro,
        UsuarioResumoDTO usuario,
        Instant dataEmprestimo,
        LocalDate dataPrevistaDevolucao,
        boolean atrasado,
        long diasAtraso
) {
}
