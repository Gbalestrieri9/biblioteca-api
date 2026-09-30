package com.postech.biblioteca.service;

import com.postech.biblioteca.dto.request.LoginRequestDTO;
import com.postech.biblioteca.dto.request.RegistrarRequestDTO;
import com.postech.biblioteca.dto.response.TokenResponseDTO;
import com.postech.biblioteca.entity.Usuario;
import com.postech.biblioteca.enums.Papel;
import com.postech.biblioteca.exception.BusinessException;
import com.postech.biblioteca.repository.UsuarioRepository;
import com.postech.biblioteca.security.JwtService;
import com.postech.biblioteca.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do login, autocadastro e resolução do usuário autenticado
 * — mockando {@link AuthenticationManager}/{@link JwtService} para não
 * depender da cadeia real do Spring Security nem de um token JWT real.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void deveAutenticarERetornarTokenComCredenciaisValidas() {
        LoginRequestDTO dto = new LoginRequestDTO("admin@biblioteca.com", "Admin@123");
        Usuario usuario = Usuario.builder()
                .id(1L).nome("Administrador").email("admin@biblioteca.com")
                .senha("hash-bcrypt").papel(Papel.BIBLIOTECARIO).dataCadastro(Instant.now())
                .build();
        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.of(usuario));
        when(jwtService.gerarToken("admin@biblioteca.com", "BIBLIOTECARIO")).thenReturn("token-jwt");
        when(jwtService.expiracaoEmSegundos()).thenReturn(86400L);

        TokenResponseDTO resposta = authService.login(dto);

        assertThat(resposta.accessToken()).isEqualTo("token-jwt");
        assertThat(resposta.tipo()).isEqualTo("Bearer");
        assertThat(resposta.expiraEmSegundos()).isEqualTo(86400L);
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void deveFalharComCredenciaisInvalidas() {
        LoginRequestDTO dto = new LoginRequestDTO("admin@biblioteca.com", "senhaErrada");
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inválidos");
        verifyNoInteractions(jwtService);
    }

    @Test
    void deveRegistrarNovoUsuarioComPapelUsuario() {
        RegistrarRequestDTO dto = new RegistrarRequestDTO("Ana Souza", "ana.souza@example.com", "Senha@123");
        when(usuarioRepository.existsByEmail(dto.email())).thenReturn(false);
        when(passwordEncoder.encode(dto.senha())).thenReturn("hash-codificado");
        when(jwtService.gerarToken(dto.email(), "USUARIO")).thenReturn("token-novo");
        when(jwtService.expiracaoEmSegundos()).thenReturn(86400L);

        TokenResponseDTO resposta = authService.registrar(dto);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario salvo = captor.getValue();
        assertThat(salvo.getPapel()).isEqualTo(Papel.USUARIO);
        assertThat(salvo.getSenha()).isEqualTo("hash-codificado");
        assertThat(resposta.accessToken()).isEqualTo("token-novo");
    }

    @Test
    void deveFalharAoRegistrarComEmailJaExistente() {
        RegistrarRequestDTO dto = new RegistrarRequestDTO("Ana Souza", "ana.souza@example.com", "Senha@123");
        when(usuarioRepository.existsByEmail(dto.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.registrar(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Já existe");

        verify(usuarioRepository, never()).save(any());
    }
}
