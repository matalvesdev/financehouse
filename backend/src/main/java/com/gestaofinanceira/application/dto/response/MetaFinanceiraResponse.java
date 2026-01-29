package com.gestaofinanceira.application.dto.response;

import com.gestaofinanceira.domain.valueobjects.StatusMeta;
import com.gestaofinanceira.domain.valueobjects.TipoMeta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para resposta com dados de meta financeira.
 * 
 * Contém informações completas da meta incluindo:
 * - Dados básicos (nome, valor alvo, prazo, tipo)
 * - Progresso atual e status
 * - Percentual de conclusão calculado
 * - Estimativa de data de conclusão
 */
public record MetaFinanceiraResponse(
    String id,
    String nome,
    BigDecimal valorAlvo,
    BigDecimal valorAtual,
    LocalDate prazo,
    TipoMeta tipo,
    StatusMeta status,
    LocalDateTime criadoEm,
    BigDecimal percentualConclusao,
    LocalDate estimativaConclusao
) {}