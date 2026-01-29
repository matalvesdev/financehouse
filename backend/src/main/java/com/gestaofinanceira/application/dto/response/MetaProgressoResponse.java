package com.gestaofinanceira.application.dto.response;

import com.gestaofinanceira.domain.valueobjects.StatusMeta;
import com.gestaofinanceira.domain.valueobjects.TipoMeta;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para resposta com progresso resumido de meta financeira.
 * 
 * Versão simplificada dos dados de meta para exibição
 * em dashboards e listas resumidas.
 */
public record MetaProgressoResponse(
    String id,
    String nome,
    BigDecimal valorAlvo,
    BigDecimal valorAtual,
    LocalDate prazo,
    TipoMeta tipo,
    StatusMeta status,
    BigDecimal percentualConclusao,
    boolean proximaDoPrazo,
    LocalDate estimativaConclusao
) {}