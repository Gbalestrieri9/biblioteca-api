package com.postech.biblioteca.service.impl;

import com.postech.biblioteca.dto.request.LoginRequestDTO;
import com.postech.biblioteca.dto.request.RegistrarRequestDTO;
import com.postech.biblioteca.dto.response.TokenResponseDTO;
import com.postech.biblioteca.dto.response.UsuarioResponseDTO;
import com.postech.biblioteca.entity.Usuario;
import com.postech.biblioteca.enums.Papel;
import com.postech.biblioteca.exception.BusinessException;
import com.postech.biblioteca.exception.ResourceNotFoundException;
import com.postech.biblioteca.mapper.UsuarioMapper;
import com.postech.biblioteca.repository.UsuarioRepository;
import com.postech.biblioteca.security.JwtService;
import com.postech.biblioteca.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public TokenResponseDTO login(LoginRequestDTO dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.email(), dto.senha()));
        } catch (BadCredentialsException | DisabledException e) {
            // Mensagem genérica de propósito: não revela se o e-mail existe
            // ou não, evitando enumeração de contas.
            throw new BusinessException("E-mail ou senha inválidos");
        }

        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new BusinessException("E-mail ou senha inválidos"));

        String token = jwtService.gerarToken(usuario.getEmail(), usuario.getPapel().name());
        return TokenResponseDTO.of(token, jwtService.expiracaoEmSegundos());
    }

    @Override
    public TokenResponseDTO registrar(RegistrarRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Já existe um usuário cadastrado com o e-mail " + dto.email());
        }

        Usuario usuario = Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .senha(passwordEncoder.encode(dto.senha()))
                .papel(Papel.USUARIO)
                .build();
        usuarioRepository.save(usuario);

        String token = jwtService.gerarToken(usuario.getEmail(), usuario.getPapel().name());
        return TokenResponseDTO.of(token, jwtService.expiracaoEmSegundos());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO usuarioAutenticado(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado: " + email));
        return UsuarioMapper.toResponseDTO(usuario);
    }
}
