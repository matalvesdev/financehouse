package com.gestaofinanceira.application.dto.response;

import java.time.LocalDateTime;

/**
 * DTO para resposta com dados do usuário.
 * 
 * Contém informações básicas do usuário, excluindo dados sensíveis
 * como senha hash. Inclui status de dados iniciais para controle
 * de fluxo de importação.
 */
public record UsuarioResponse(
    String id,
    String nome,
    String email,
    LocalDateTime criadoEm,
    boolean ativo,
    boolean dadosIniciaisCarregados
) {}