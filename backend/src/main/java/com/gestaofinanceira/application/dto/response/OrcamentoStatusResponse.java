package com.gestaofinanceira.application.dto.response;

import com.gestaofinanceira.domain.valueobjects.StatusOrcamento;

import java.math.BigDecimal;

/**
 * DTO para resposta com status resumido de orçamento.
 * 
 * Versão simplificada dos dados de orçamento para exibição
 * em dashboards e listas resumidas.
 */
public record OrcamentoStatusResponse(
    String id,
    String categoria,
    BigDecimal limite,
    BigDecimal gastoAtual,
    StatusOrcamento status,
    BigDecimal percentualUtilizado,
    boolean proximoDoLimite,
    boolean excedeuLimite
) {}