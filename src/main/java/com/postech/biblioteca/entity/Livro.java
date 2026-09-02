package com.postech.biblioteca.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Representa um livro do acervo da biblioteca.
 *
 * <p>{@code quantidadeDisponivel} é controlada pelo serviço de empréstimos:
 * decrementada a cada empréstimo concedido e incrementada a cada devolução,
 * nunca podendo ficar negativa nem exceder {@code quantidadeTotal}.</p>
 */
@Entity
@Table(name = "livros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "autor", nullable = false, length = 255)
    private String autor;

    @Column(name = "isbn", nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "quantidade_total", nullable = false)
    private Integer quantidadeTotal;

    @Column(name = "quantidade_disponivel", nullable = false)
    private Integer quantidadeDisponivel;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private Instant dataCadastro;

    @PrePersist
    void aoPersistir() {
        if (dataCadastro == null) {
            dataCadastro = Instant.now();
        }
        if (quantidadeDisponivel == null) {
            quantidadeDisponivel = quantidadeTotal;
        }
    }

    /** Indica se há ao menos uma cópia disponível para empréstimo. */
    public boolean isDisponivel() {
        return quantidadeDisponivel != null && quantidadeDisponivel > 0;
    }
}
