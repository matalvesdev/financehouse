package com.gestaofinanceira.application.usecases.autenticacao;

import com.gestaofinanceira.application.ports.service.TokenJwtPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Caso de uso para logout de usuários do sistema.
 * 
 * Implementa o processo completo de logout:
 * - Validação dos tokens fornecidos
 * - Invalidação do access token
 * - Invalidação do refresh token
 * - Adição dos tokens à blacklist
 * 
 * Requirements: 1.4, 10.2
 */
@Service
@Transactional
public class LogoutUsuarioUseCase {
    
    private final TokenJwtPort tokenJwtPort;
    
    public LogoutUsuarioUseCase(TokenJwtPort tokenJwtPort) {
        this.tokenJwtPort = Objects.requireNonNull(tokenJwtPort, 
            "TokenJwtPort não pode ser nulo");
    }
    
    /**
     * Executa o logout do usuário invalidando ambos os tokens.
     * 
     * @param accessToken token de acesso a ser invalidado
     * @param refreshToken token de refresh a ser invalidado
     * @throws IllegalArgumentException se algum token é inválido
     */
    public void executar(String accessToken, String refreshToken) {
        Objects.requireNonNull(accessToken, "Access token não pode ser nulo");
        Objects.requireNonNull(refreshToken, "Refresh token não pode ser nulo");
        
        if (accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Access token não pode estar vazio");
        }
        
        if (refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Refresh token não pode estar vazio");
        }
        
        // 1. Validar access token
        var resultadoValidacaoAccess = tokenJwtPort.validarToken(accessToken);
        if (!resultadoValidacaoAccess.valido() && 
            !tokenJwtPort.tokenExpirado(accessToken)) {
            // Se o token não é válido E não está apenas expirado, é um erro
            throw new IllegalArgumentException("Access token inválido: " + 
                resultadoValidacaoAccess.motivo());
        }
        
        // 2. Validar refresh token
        var resultadoValidacaoRefresh = tokenJwtPort.validarToken(refreshToken);
        if (!resultadoValidacaoRefresh.valido()) {
            throw new IllegalArgumentException("Refresh token inválido: " + 
                resultadoValidacaoRefresh.motivo());
        }
        
        // 3. Verificar se os tokens pertencem ao mesmo usuário
        if (resultadoValidacaoAccess.valido() && 
            !resultadoValidacaoAccess.usuarioId().equals(resultadoValidacaoRefresh.usuarioId())) {
            throw new IllegalArgumentException("Tokens pertencem a usuários diferentes");
        }
        
        // 4. Verificar tipos de token
        if (resultadoValidacaoAccess.valido() && 
            resultadoValidacaoAccess.tipo() != TokenJwtPort.TipoToken.ACESSO) {
            throw new IllegalArgumentException("Primeiro token não é um access token");
        }
        
        if (resultadoValidacaoRefresh.tipo() != TokenJwtPort.TipoToken.REFRESH) {
            throw new IllegalArgumentException("Segundo token não é um refresh token");
        }
        
        // 5. Invalidar ambos os tokens (adicionar à blacklist)
        tokenJwtPort.invalidarToken(accessToken);
        tokenJwtPort.invalidarToken(refreshToken);
    }
    
    /**
     * Executa o logout usando apenas o refresh token.
     * Útil quando o access token já expirou.
     * 
     * @param refreshToken token de refresh a ser invalidado
     * @throws IllegalArgumentException se o refresh token é inválido
     */
    public void executarComRefreshToken(String refreshToken) {
        Objects.requireNonNull(refreshToken, "Refresh token não pode ser nulo");
        
        if (refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Refresh token não pode estar vazio");
        }
        
        // 1. Validar refresh token
        var resultadoValidacao = tokenJwtPort.validarToken(refreshToken);
        if (!resultadoValidacao.valido()) {
            throw new IllegalArgumentException("Refresh token inválido: " + 
                resultadoValidacao.motivo());
        }
        
        // 2. Verificar tipo de token
        if (resultadoValidacao.tipo() != TokenJwtPort.TipoToken.REFRESH) {
            throw new IllegalArgumentException("Token fornecido não é um refresh token");
        }
        
        // 3. Invalidar o refresh token
        tokenJwtPort.invalidarToken(refreshToken);
    }
}