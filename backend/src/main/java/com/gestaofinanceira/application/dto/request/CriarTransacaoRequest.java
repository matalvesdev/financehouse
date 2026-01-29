package com.gestaofinanceira.application.dto.request;

import com.gestaofinanceira.domain.valueobjects.TipoTransacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para requisição de criação de transação.
 * 
 * Valida os dados obrigatórios para uma transação:
 * - Valor positivo com precisão decimal
 * - Descrição não vazia
 * - Categoria válida
 * - Tipo de transação (RECEITA ou DESPESA)
 * - Data da transação
 */
public record CriarTransacaoRequest(
    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    BigDecimal valor,
    
    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 1, max = 255, message = "Descrição deve ter entre 1 e 255 caracteres")
    String descricao,
    
    @NotBlank(message = "Categoria é obrigatória")
    String categoria,
    
    @NotNull(message = "Tipo de transação é obrigatório")
    TipoTransacao tipo,
    
    @NotNull(message = "Data é obrigatória")
    LocalDate data
) {}