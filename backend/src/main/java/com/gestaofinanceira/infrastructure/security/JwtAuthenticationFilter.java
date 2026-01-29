package com.gestaofinanceira.infrastructure.security;

import com.gestaofinanceira.application.ports.service.TokenJwtPort;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Filtro de autenticação JWT que intercepta requisições HTTP.
 * 
 * Responsável por:
 * - Extrair token JWT do header Authorization
 * - Validar o token usando TokenJwtPort
 * - Configurar contexto de segurança do Spring Security
 * - Permitir acesso a endpoints protegidos
 * 
 * Requirements: 1.1, 1.2, 10.2
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    private final TokenJwtPort tokenJwtPort;
    
    public JwtAuthenticationFilter(TokenJwtPort tokenJwtPort) {
        this.tokenJwtPort = tokenJwtPort;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String token = extrairTokenDoHeader(request);
            
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                processarAutenticacaoJwt(token, request);
            }
            
        } catch (Exception e) {
            logger.error("Erro ao processar autenticação JWT", e);
            // Não interrompe a cadeia de filtros - deixa o Spring Security lidar com a falta de autenticação
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extrai o token JWT do header Authorization.
     */
    private String extrairTokenDoHeader(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }
    
    /**
     * Processa a autenticação JWT e configura o contexto de segurança.
     */
    private void processarAutenticacaoJwt(String token, HttpServletRequest request) {
        TokenJwtPort.ResultadoValidacaoToken resultado = tokenJwtPort.validarToken(token);
        
        if (!resultado.valido()) {
            logger.debug("Token JWT inválido: {}", resultado.motivo());
            return;
        }
        
        // Verificar se é um token de acesso (não aceitar refresh tokens para autenticação)
        if (resultado.tipo() != TokenJwtPort.TipoToken.ACESSO) {
            logger.debug("Token não é de acesso: {}", resultado.tipo());
            return;
        }
        
        UsuarioId usuarioId = resultado.usuarioId();
        
        // Extrair claims para informações adicionais
        Map<String, Object> claims = tokenJwtPort.extrairClaims(token);
        String email = (String) claims.get("email");
        Boolean ativo = (Boolean) claims.get("ativo");
        
        // Verificar se o usuário ainda está ativo
        if (ativo == null || !ativo) {
            logger.debug("Usuário inativo no token: {}", usuarioId.valor());
            return;
        }
        
        // Criar principal com informações do usuário
        JwtUserPrincipal principal = new JwtUserPrincipal(
            usuarioId.valor().toString(),
            email,
            claims
        );
        
        // Criar authorities (por enquanto, todos os usuários autenticados têm role USER)
        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        
        // Criar token de autenticação do Spring Security
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(principal, null, authorities);
        
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        
        // Configurar contexto de segurança
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        logger.debug("Usuário autenticado via JWT: {} ({})", email, usuarioId.valor());
    }
    
    /**
     * Principal customizado para carregar informações do usuário JWT.
     */
    public static class JwtUserPrincipal {
        private final String userId;
        private final String email;
        private final Map<String, Object> claims;
        
        public JwtUserPrincipal(String userId, String email, Map<String, Object> claims) {
            this.userId = userId;
            this.email = email;
            this.claims = claims;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public String getEmail() {
            return email;
        }
        
        public Map<String, Object> getClaims() {
            return claims;
        }
        
        public UsuarioId getUsuarioId() {
            return new UsuarioId(java.util.UUID.fromString(userId));
        }
        
        @Override
        public String toString() {
            return "JwtUserPrincipal{" +
                   "userId='" + userId + '\'' +
                   ", email='" + email + '\'' +
                   '}';
        }
    }
}