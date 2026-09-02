package com.postech.biblioteca.dto.response;

public record RelatorioLivroMaisEmprestadoDTO(
        Long livroId,
        String titulo,
        String autor,
        String isbn,
        long totalEmprestimos
) {
}
