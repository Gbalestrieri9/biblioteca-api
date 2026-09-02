package com.postech.biblioteca.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Representa um usuário (aluno ou membro) cadastrado na biblioteca.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private Instant dataCadastro;

    @PrePersist
    void aoPersistir() {
        if (dataCadastro == null) {
            dataCadastro = Instant.now();
        }
    }
}
