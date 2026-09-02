package com.postech.biblioteca.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Dados de entrada para cadastro/alteração de um livro.
 */
@Schema(description = "Dados para cadastro ou atualização de um livro")
public record LivroRequestDTO(

        @Schema(description = "Título do livro", example = "Clean Code")
        @NotBlank(message = "O título é obrigatório")
        String titulo,

        @Schema(description = "Autor do livro", example = "Robert C. Martin")
        @NotBlank(message = "O autor é obrigatório")
        String autor,

        @Schema(description = "ISBN do livro (10 ou 13 dígitos)", example = "9780132350884")
        @NotBlank(message = "O ISBN é obrigatório")
        String isbn,

        @Schema(description = "Quantidade total de exemplares", example = "3")
        @NotNull(message = "A quantidade total é obrigatória")
        @Min(value = 0, message = "A quantidade total não pode ser negativa")
        Integer quantidadeTotal
) {
}
