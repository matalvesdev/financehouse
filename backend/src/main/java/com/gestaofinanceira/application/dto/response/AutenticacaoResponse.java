package com.gestaofinanceira.application.dto.response;

/**
 * DTO para resposta de autenticação.
 * 
 * Contém os tokens JWT necessários para manter a sessão:
 * - Access token: Para autenticação de requests (curta duração)
 * - Refresh token: Para renovação do access token (longa duração)
 * - Dados básicos do usuário autenticado
 */
public record AutenticacaoResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn,
    UsuarioResponse usuario
) {}