package com.gestaofinanceira.application.dto.response;

import com.gestaofinanceira.domain.valueobjects.PeriodoOrcamento;
import com.gestaofinanceira.domain.valueobjects.StatusOrcamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para resposta com dados de orçamento.
 * 
 * Contém informações completas do orçamento incluindo:
 * - Dados básicos (categoria, limite, período)
 * - Status atual e gasto acumulado
 * - Datas de vigência
 * - Percentual utilizado calculado
 */
public record OrcamentoResponse(
    String id,
    String categoria,
    BigDecimal limite,
    PeriodoOrcamento periodo,
    BigDecimal gastoAtual,
    StatusOrcamento status,
    LocalDate inicioVigencia,
    LocalDate fimVigencia,
    LocalDateTime criadoEm,
    BigDecimal percentualUtilizado
) {}