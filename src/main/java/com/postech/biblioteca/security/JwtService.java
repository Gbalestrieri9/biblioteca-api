package com.postech.biblioteca.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

/**
 * Geração e validação de tokens JWT (assinados com HMAC-SHA256) usados como
 * Bearer token de autenticação — ver {@link JwtAuthenticationFilter}.
 *
 * <p>O segredo de assinatura ({@code biblioteca.jwt.secret}) deve ser
 * sobrescrito em qualquer ambiente real (variável de ambiente
 * {@code JWT_SECRET}) — o valor padrão do {@code application.yml} serve só
 * para desenvolvimento local, nunca para produção.</p>
 */
@Component
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoMs;

    public JwtService(@Value("${biblioteca.jwt.secret}") String segredo,
                       @Value("${biblioteca.jwt.expiracao-ms}") long expiracaoMs) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMs = expiracaoMs;
    }

    /** Gera um token cujo subject é o e-mail do usuário e carrega o papel como claim. */
    public String gerarToken(String email, String papel) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("papel", papel)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusMillis(expiracaoMs)))
                .signWith(chave)
                .compact();
    }

    /** @return o e-mail (subject) do token, ou {@code null} se o token for inválido/expirado/malformado. */
    public String extrairEmail(String token) {
        try {
            return extrairClaim(token, Claims::getSubject);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public boolean tokenValido(String token, String emailEsperado) {
        String email = extrairEmail(token);
        return email != null && email.equals(emailEsperado) && !tokenExpirado(token);
    }

    private boolean tokenExpirado(String token) {
        try {
            return extrairClaim(token, Claims::getExpiration).before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }

    private <T> T extrairClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }

    public long expiracaoEmSegundos() {
        return expiracaoMs / 1000;
    }
}
