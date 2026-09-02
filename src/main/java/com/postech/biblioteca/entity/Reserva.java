package com.postech.biblioteca.entity;

import com.postech.biblioteca.enums.StatusReserva;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Representa a reserva de um {@link Livro} feita por um {@link Usuario}
 * enquanto não há cópias disponíveis para empréstimo imediato.
 */
@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_reserva", nullable = false, updatable = false)
    private Instant dataReserva;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusReserva status;

    @PrePersist
    void aoPersistir() {
        if (dataReserva == null) {
            dataReserva = Instant.now();
        }
        if (status == null) {
            status = StatusReserva.ATIVA;
        }
    }
}
