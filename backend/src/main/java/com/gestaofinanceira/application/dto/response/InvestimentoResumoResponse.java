package com.gestaofinanceira.application.dto.response;

import java.math.BigDecimal;

/**
 * DTO para resposta com resumo de investimento individual.
 * 
 * Contém dados básicos de um investimento para exibição
 * em listas e resumos da carteira.
 */
public record InvestimentoResumoResponse(
    String id,
    String ativo,
    String nomeAtivo,
    BigDecimal quantidade,
    BigDecimal precoCompra,
    BigDecimal precoAtual,
    BigDecimal valorAtual,
    BigDecimal ganhoPerda,
    BigDecimal percentualGanhoPerda,
    String tipoAtivo
) {}