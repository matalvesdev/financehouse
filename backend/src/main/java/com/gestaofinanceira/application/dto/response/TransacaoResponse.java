package com.gestaofinanceira.application.dto.response;

import com.gestaofinanceira.domain.valueobjects.TipoTransacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para resposta com dados de transação.
 * 
 * Contém todos os dados de uma transação para exibição,
 * incluindo metadados de auditoria.
 */
public record TransacaoResponse(
    String id,
    BigDecimal valor,
    String moeda,
    String descricao,
    String categoria,
    TipoTransacao tipo,
    LocalDate data,
    LocalDateTime criadoEm,
    boolean ativa
) {}