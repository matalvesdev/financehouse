package com.gestaofinanceira.application.dto.request;

import com.gestaofinanceira.domain.valueobjects.PeriodoOrcamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para requisição de criação de orçamento.
 * 
 * Valida os parâmetros necessários para um orçamento:
 * - Categoria para controle de gastos
 * - Limite positivo
 * - Período de vigência (MENSAL, TRIMESTRAL, ANUAL)
 * - Data de início da vigência
 */
public record CriarOrcamentoRequest(
    @NotBlank(message = "Categoria é obrigatória")
    String categoria,
    
    @NotNull(message = "Limite é obrigatório")
    @DecimalMin(value = "0.01", message = "Limite deve ser maior que zero")
    BigDecimal limite,
    
    @NotNull(message = "Período é obrigatório")
    PeriodoOrcamento periodo,
    
    @NotNull(message = "Data de início é obrigatória")
    LocalDate inicioVigencia
) {}