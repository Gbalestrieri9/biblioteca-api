package com.postech.biblioteca.mapper;

import com.postech.biblioteca.dto.request.LivroRequestDTO;
import com.postech.biblioteca.dto.response.LivroResponseDTO;
import com.postech.biblioteca.dto.response.LivroResumoDTO;
import com.postech.biblioteca.entity.Livro;

public final class LivroMapper {

    private LivroMapper() {
    }

    public static Livro toEntity(LivroRequestDTO dto) {
        return Livro.builder()
                .titulo(dto.titulo())
                .autor(dto.autor())
                .isbn(dto.isbn())
                .quantidadeTotal(dto.quantidadeTotal())
                .quantidadeDisponivel(dto.quantidadeTotal())
                .build();
    }

    /** Aplica os dados de {@code dto} sobre uma entidade já existente, preservando o histórico de empréstimos ativos. */
    public static void atualizarEntity(Livro livro, LivroRequestDTO dto) {
        int emprestados = livro.getQuantidadeTotal() - livro.getQuantidadeDisponivel();
        livro.setTitulo(dto.titulo());
        livro.setAutor(dto.autor());
        livro.setIsbn(dto.isbn());
        livro.setQuantidadeTotal(dto.quantidadeTotal());
        livro.setQuantidadeDisponivel(Math.max(0, dto.quantidadeTotal() - emprestados));
    }

    public static LivroResponseDTO toResponseDTO(Livro livro) {
        return new LivroResponseDTO(
                livro.getId(),
                livro.getTitulo(),
                livro.getAutor(),
                livro.getIsbn(),
                livro.getQuantidadeTotal(),
                livro.getQuantidadeDisponivel(),
                livro.isDisponivel(),
                livro.getDataCadastro()
        );
    }

    public static LivroResumoDTO toResumoDTO(Livro livro) {
        return new LivroResumoDTO(livro.getId(), livro.getTitulo(), livro.getAutor(), livro.getIsbn());
    }
}
