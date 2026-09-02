package com.postech.biblioteca.dto.response;

import com.postech.biblioteca.enums.StatusEmprestimo;

import java.time.Instant;
import java.time.LocalDate;

public record EmprestimoResponseDTO(
        Long id,
        LivroResumoDTO livro,
        UsuarioResumoDTO usuario,
        Instant dataEmprestimo,
        LocalDate dataPrevistaDevolucao,
        Instant dataDevolucao,
        StatusEmprestimo status,
        boolean atrasado,
        long diasAtraso
) {
}
