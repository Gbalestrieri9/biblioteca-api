package com.postech.biblioteca.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuração central de autenticação (JWT Bearer, stateless) e autorização
 * por papel (BIBLIOTECARIO/USUARIO).
 *
 * <p>Todas as regras de acesso por rota/método ficam centralizadas aqui, em
 * vez de espalhadas em {@code @PreAuthorize} pelos controllers — mantém os
 * services focados em regra de negócio e as regras de acesso visíveis em um
 * único lugar, fácil de auditar.</p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UsuarioDetailsServiceImpl usuarioDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API stateless (o próprio Bearer token carrega a
                // identidade a cada requisição) — sem sessão, CSRF não se
                // aplica.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // Público: login, autocadastro e documentação
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/registrar").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Gestão de acervo e de usuários: só BIBLIOTECARIO
                        .requestMatchers(HttpMethod.POST, "/api/v1/livros", "/api/v1/livros/lote").hasRole("BIBLIOTECARIO")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/livros/**").hasRole("BIBLIOTECARIO")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/livros/**").hasRole("BIBLIOTECARIO")
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios").hasRole("BIBLIOTECARIO")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/usuarios/**").hasRole("BIBLIOTECARIO")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/usuarios/**").hasRole("BIBLIOTECARIO")

                        // Demais endpoints (consultas, empréstimos, reservas,
                        // relatórios, /auth/me): qualquer usuário autenticado
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS permissivo (qualquer origem) — a API não usa cookies/sessão
     * (autenticação stateless via Bearer token), então liberar a origem não
     * expõe credenciais de terceiros; simplifica testar a API a partir de
     * um front-end servido de outra porta/domínio.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
