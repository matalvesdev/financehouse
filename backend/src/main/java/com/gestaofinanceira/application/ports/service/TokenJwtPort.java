package com.gestaofinanceira.application.ports.service;

import com.gestaofinanceira.domain.valueobjects.UsuarioId;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Port para geração e validação de tokens JWT.
 * 
 * Define as operações necessárias para gerenciamento de tokens JWT
 * para autenticação e autorização no sistema.
 */
public interface TokenJwtPort {
    
    /**
     * Gera token de acesso JWT para um usuário.
     * 
     * @param usuarioId ID do usuário
     * @param claims claims adicionais para incluir no token
     * @return token de acesso gerado
     */
    String gerarTokenAcesso(UsuarioId usuarioId, Map<String, Object> claims);
    
    /**
     * Gera token de refresh JWT para um usuário.
     * 
     * @param usuarioId ID do usuário
     * @return token de refresh gerado
     */
    String gerarTokenRefresh(UsuarioId usuarioId);
    
    /**
     * Valida um token JWT.
     * 
     * @param token token a ser validado
     * @return resultado da validação
     */
    ResultadoValidacaoToken validarToken(String token);
    
    /**
     * Extrai ID do usuário de um token válido.
     * 
     * @param token token válido
     * @return ID do usuário
     */
    UsuarioId extrairUsuarioId(String token);
    
    /**
     * Extrai claims de um token válido.
     * 
     * @param token token válido
     * @return mapa de claims
     */
    Map<String, Object> extrairClaims(String token);
    
    /**
     * Verifica se um token está expirado.
     * 
     * @param token token a ser verificado
     * @return true se o token está expirado
     */
    boolean tokenExpirado(String token);
    
    /**
     * Obtém data de expiração de um token.
     * 
     * @param token token para verificar expiração
     * @return data de expiração
     */
    LocalDateTime obterDataExpiracao(String token);
    
    /**
     * Invalida um token (adiciona à blacklist).
     * 
     * @param token token a ser invalidado
     */
    void invalidarToken(String token);
    
    /**
     * Verifica se um token está na blacklist.
     * 
     * @param token token a ser verificado
     * @return true se o token está invalidado
     */
    boolean tokenInvalidado(String token);
    
    /**
     * Renova token de acesso usando token de refresh.
     * 
     * @param tokenRefresh token de refresh válido
     * @return novo token de acesso
     */
    String renovarTokenAcesso(String tokenRefresh);
    
    /**
     * Representa resultado da validação de token.
     */
    record ResultadoValidacaoToken(
        boolean valido,
        String motivo,
        UsuarioId usuarioId,
        LocalDateTime expiracao,
        TipoToken tipo
    ) {}
    
    /**
     * Tipos de token JWT.
     */
    enum TipoToken {
        ACESSO,
        REFRESH
    }
}