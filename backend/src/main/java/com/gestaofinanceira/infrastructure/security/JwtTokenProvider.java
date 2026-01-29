package com.gestaofinanceira.infrastructure.security;

import com.gestaofinanceira.application.ports.service.TokenJwtPort;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementação do TokenJwtPort usando JJWT.
 * 
 * Responsável por:
 * - Geração de tokens JWT (access e refresh)
 * - Validação e parsing de tokens
 * - Gerenciamento de blacklist de tokens
 * - Extração de claims e metadados
 * 
 * Requirements: 1.1, 1.2, 1.3, 1.4, 10.2
 */
@Component
public class JwtTokenProvider implements TokenJwtPort {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    
    private final SecretKey secretKey;
    private final long accessTokenValiditySeconds;
    private final long refreshTokenValiditySeconds;
    private final String issuer;
    
    // Blacklist de tokens invalidados (em produção, usar Redis ou banco)
    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();
    
    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-validity-seconds:900}") long accessTokenValiditySeconds,
            @Value("${app.jwt.refresh-token-validity-seconds:604800}") long refreshTokenValiditySeconds,
            @Value("${app.jwt.issuer:gestao-financeira}") String issuer) {
        
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
        this.issuer = issuer;
        
        logger.info("JWT Provider inicializado - Access token validity: {}s, Refresh token validity: {}s", 
                   accessTokenValiditySeconds, refreshTokenValiditySeconds);
    }
    
    @Override
    public String gerarTokenAcesso(UsuarioId usuarioId, Map<String, Object> claims) {
        Objects.requireNonNull(usuarioId, "UsuarioId não pode ser nulo");
        Objects.requireNonNull(claims, "Claims não podem ser nulos");
        
        Date now = new Date();
        Date expiration = new Date(now.getTime() + (accessTokenValiditySeconds * 1000));
        
        JwtBuilder builder = Jwts.builder()
                .subject(usuarioId.valor().toString())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiration)
                .claim("tipo", TipoToken.ACESSO.name())
                .signWith(secretKey);
        
        // Adicionar claims customizados
        claims.forEach(builder::claim);
        
        String token = builder.compact();
        
        logger.debug("Token de acesso gerado para usuário: {}", usuarioId.valor());
        return token;
    }
    
    @Override
    public String gerarTokenRefresh(UsuarioId usuarioId) {
        Objects.requireNonNull(usuarioId, "UsuarioId não pode ser nulo");
        
        Date now = new Date();
        Date expiration = new Date(now.getTime() + (refreshTokenValiditySeconds * 1000));
        
        String token = Jwts.builder()
                .subject(usuarioId.valor().toString())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiration)
                .claim("tipo", TipoToken.REFRESH.name())
                .signWith(secretKey)
                .compact();
        
        logger.debug("Token de refresh gerado para usuário: {}", usuarioId.valor());
        return token;
    }
    
    @Override
    public ResultadoValidacaoToken validarToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return new ResultadoValidacaoToken(false, "Token não pode ser vazio", null, null, null);
        }
        
        // Verificar se o token está na blacklist
        if (tokenInvalidado(token)) {
            return new ResultadoValidacaoToken(false, "Token foi invalidado", null, null, null);
        }
        
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // Extrair informações do token
            String subject = claims.getSubject();
            Date expiration = claims.getExpiration();
            String tipoStr = claims.get("tipo", String.class);
            
            if (subject == null || expiration == null || tipoStr == null) {
                return new ResultadoValidacaoToken(false, "Token malformado", null, null, null);
            }
            
            UsuarioId usuarioId = new UsuarioId(UUID.fromString(subject));
            LocalDateTime expirationDateTime = LocalDateTime.ofInstant(expiration.toInstant(), ZoneOffset.systemDefault());
            TipoToken tipo = TipoToken.valueOf(tipoStr);
            
            // Verificar se o token não está expirado
            if (expiration.before(new Date())) {
                return new ResultadoValidacaoToken(false, "Token expirado", usuarioId, expirationDateTime, tipo);
            }
            
            return new ResultadoValidacaoToken(true, null, usuarioId, expirationDateTime, tipo);
            
        } catch (ExpiredJwtException e) {
            logger.debug("Token expirado: {}", e.getMessage());
            return new ResultadoValidacaoToken(false, "Token expirado", null, null, null);
        } catch (UnsupportedJwtException e) {
            logger.warn("Token não suportado: {}", e.getMessage());
            return new ResultadoValidacaoToken(false, "Formato de token não suportado", null, null, null);
        } catch (MalformedJwtException e) {
            logger.warn("Token malformado: {}", e.getMessage());
            return new ResultadoValidacaoToken(false, "Token malformado", null, null, null);
        } catch (SecurityException | IllegalArgumentException e) {
            logger.warn("Erro de segurança no token: {}", e.getMessage());
            return new ResultadoValidacaoToken(false, "Token inválido", null, null, null);
        } catch (Exception e) {
            logger.error("Erro inesperado ao validar token", e);
            return new ResultadoValidacaoToken(false, "Erro interno na validação", null, null, null);
        }
    }
    
    @Override
    public UsuarioId extrairUsuarioId(String token) {
        Claims claims = extrairClaimsInterno(token);
        String subject = claims.getSubject();
        return new UsuarioId(UUID.fromString(subject));
    }
    
    @Override
    public Map<String, Object> extrairClaims(String token) {
        Claims claims = extrairClaimsInterno(token);
        return new HashMap<>(claims);
    }
    
    @Override
    public boolean tokenExpirado(String token) {
        try {
            Claims claims = extrairClaimsInterno(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true; // Se não conseguir extrair, considera expirado
        }
    }
    
    @Override
    public LocalDateTime obterDataExpiracao(String token) {
        Claims claims = extrairClaimsInterno(token);
        Date expiration = claims.getExpiration();
        return LocalDateTime.ofInstant(expiration.toInstant(), ZoneOffset.systemDefault());
    }
    
    @Override
    public void invalidarToken(String token) {
        Objects.requireNonNull(token, "Token não pode ser nulo");
        tokenBlacklist.add(token);
        logger.debug("Token adicionado à blacklist");
    }
    
    @Override
    public boolean tokenInvalidado(String token) {
        return tokenBlacklist.contains(token);
    }
    
    @Override
    public String renovarTokenAcesso(String tokenRefresh) {
        ResultadoValidacaoToken resultado = validarToken(tokenRefresh);
        
        if (!resultado.valido()) {
            throw new IllegalArgumentException("Token de refresh inválido: " + resultado.motivo());
        }
        
        if (resultado.tipo() != TipoToken.REFRESH) {
            throw new IllegalArgumentException("Token fornecido não é um token de refresh");
        }
        
        // Extrair claims do token de refresh para recriar o token de acesso
        Map<String, Object> claims = extrairClaims(tokenRefresh);
        
        // Remover claims específicos do refresh token
        claims.remove("tipo");
        claims.remove("iat");
        claims.remove("exp");
        claims.remove("iss");
        claims.remove("sub");
        
        return gerarTokenAcesso(resultado.usuarioId(), claims);
    }
    
    /**
     * Extrai claims de um token sem validação adicional.
     * Usado internamente quando já sabemos que o token é válido.
     */
    private Claims extrairClaimsInterno(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}