package com.postech.biblioteca.entity;

import com.postech.biblioteca.enums.Papel;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Representa um usuário (aluno ou membro) cadastrado na biblioteca.
 *
 * <p>{@code senha} guarda apenas o hash bcrypt (nunca a senha em texto
 * puro — ver {@code SecurityConfig#passwordEncoder}), e {@code papel}
 * controla o que o usuário pode acessar na API (ver {@code SecurityConfig},
 * que centraliza as regras de autorização por papel/rota).</p>
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

    // Nunca exposta em DTO de resposta (ver UsuarioResponseDTO) — só o hash
    // bcrypt é persistido, nunca a senha em texto puro.
    @Column(name = "senha", nullable = false, length = 255)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "papel", nullable = false, length = 20)
    private Papel papel;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private Instant dataCadastro;

    @PrePersist
    void aoPersistir() {
        if (dataCadastro == null) {
            dataCadastro = Instant.now();
        }
    }
}
