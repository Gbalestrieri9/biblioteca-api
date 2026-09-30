package com.postech.biblioteca.controller;

import com.postech.biblioteca.dto.request.LoginRequestDTO;
import com.postech.biblioteca.dto.request.RegistrarRequestDTO;
import com.postech.biblioteca.dto.response.TokenResponseDTO;
import com.postech.biblioteca.dto.response.UsuarioResponseDTO;
import com.postech.biblioteca.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de autenticação (login e autocadastro) e do perfil do usuário
 * autenticado.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login, autocadastro e perfil do usuário autenticado")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Autentica com e-mail e senha, retornando um token JWT Bearer")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/registrar")
    @Operation(
            summary = "Autocadastro de um novo usuário (sempre com papel USUARIO)",
            description = "Para criar um usuário com papel BIBLIOTECARIO, use 'POST /api/v1/usuarios' "
                    + "autenticado como um BIBLIOTECARIO existente."
    )
    public ResponseEntity<TokenResponseDTO> registrar(@Valid @RequestBody RegistrarRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(dto));
    }

    @GetMapping("/me")
    @Operation(summary = "Retorna os dados do usuário autenticado, a partir do token enviado")
    public ResponseEntity<UsuarioResponseDTO> me(Authentication authentication) {
        return ResponseEntity.ok(authService.usuarioAutenticado(authentication.getName()));
    }
}
