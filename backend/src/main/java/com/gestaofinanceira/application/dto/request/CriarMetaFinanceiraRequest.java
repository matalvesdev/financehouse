package com.gestaofinanceira.application.dto.request;

import com.gestaofinanceira.domain.valueobjects.TipoMeta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para requisição de criação de meta financeira.
 * 
 * Valida os parâmetros necessários para uma meta:
 * - Nome descritivo da meta
 * - Valor alvo positivo
 * - Prazo futuro para alcançar a meta
 * - Tipo da meta (EMERGENCIA, VIAGEM, COMPRA, INVESTIMENTO)
 */
public record CriarMetaFinanceiraRequest(
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    String nome,
    
    @NotNull(message = "Valor alvo é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor alvo deve ser maior que zero")
    BigDecimal valorAlvo,
    
    @NotNull(message = "Prazo é obrigatório")
    @Future(message = "Prazo deve ser uma data futura")
    LocalDate prazo,
    
    @NotNull(message = "Tipo da meta é obrigatório")
    TipoMeta tipo
) {}