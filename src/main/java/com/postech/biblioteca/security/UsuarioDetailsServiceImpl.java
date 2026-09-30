package com.postech.biblioteca.security;

import com.postech.biblioteca.entity.Usuario;
import com.postech.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Carrega um {@link Usuario} pelo e-mail (username, para o Spring Security)
 * e o expõe como {@link UserDetails}, com uma única authority
 * {@code ROLE_<papel>} (ver {@code SecurityConfig}, que usa
 * {@code hasRole(...)} nas regras de autorização).
 */
@Service
@RequiredArgsConstructor
public class UsuarioDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Nenhum usuário encontrado com o e-mail " + email));

        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenha())
                .authorities("ROLE_" + usuario.getPapel().name())
                .build();
    }
}
