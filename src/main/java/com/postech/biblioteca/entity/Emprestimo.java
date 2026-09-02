package com.postech.biblioteca.entity;

import com.postech.biblioteca.enums.StatusEmprestimo;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Representa o empréstimo de um {@link Livro} a um {@link Usuario}.
 *
 * <p>Datas com componente de hora ({@code dataEmprestimo}, {@code
 * dataDevolucao}) são armazenadas como {@link Instant} (UTC). A data prevista
 * de devolução é um {@link LocalDate}, calculada em {@code
 * com.postech.biblioteca.config.AppConstants#ZONE_ID} somando-se {@code
 * AppConstants#PRAZO_EMPRESTIMO_DIAS} à data do empréstimo.</p>
 */
@Entity
@Table(name = "emprestimos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_emprestimo", nullable = false, updatable = false)
    private Instant dataEmprestimo;

    @Column(name = "data_prevista_devolucao", nullable = false)
    private LocalDate dataPrevistaDevolucao;

    @Column(name = "data_devolucao")
    private Instant dataDevolucao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusEmprestimo status;

    /** Indica se o empréstimo está ativo e a data prevista já passou. */
    public boolean isAtrasado(LocalDate hoje) {
        return status == StatusEmprestimo.ATIVO && dataPrevistaDevolucao.isBefore(hoje);
    }
}
