package com.gestaofinanceira.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entry point para tratamento de erros de autenticação JWT.
 * 
 * Responsável por:
 * - Interceptar tentativas de acesso não autenticado
 * - Retornar resposta JSON padronizada para erros 401
 * - Logar tentativas de acesso não autorizado
 * - Não expor informações sensíveis do sistema
 * 
 * Requirements: 1.1, 1.2, 10.2
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    
    private final ObjectMapper objectMapper;
    
    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        
        // Log da tentativa de acesso não autorizado
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        String remoteAddr = getClientIpAddress(request);
        
        logger.warn("Tentativa de acesso não autorizado: {} {} de {}", 
                   method, requestUri, remoteAddr);
        
        // Configurar resposta HTTP
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // Criar corpo da resposta de erro
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", "Token de acesso requerido");
        errorResponse.put("path", requestUri);
        
        // Adicionar informações específicas baseadas no tipo de erro
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            errorResponse.put("details", "Header Authorization não fornecido");
        } else if (!authHeader.startsWith("Bearer ")) {
            errorResponse.put("details", "Formato de token inválido. Use: Bearer <token>");
        } else {
            errorResponse.put("details", "Token inválido ou expirado");
        }
        
        // Escrever resposta JSON
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
    
    /**
     * Extrai o endereço IP real do cliente, considerando proxies.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Pegar o primeiro IP da lista (cliente original)
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}