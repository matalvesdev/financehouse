package com.gestaofinanceira.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para resposta com resumo de investimentos.
 * 
 * Contém informações agregadas da carteira de investimentos
 * para exibição no dashboard (apenas para avaliação).
 */
public record ResumoInvestimentosResponse(
    BigDecimal valorTotalCarteira,
    BigDecimal ganhoPerda,
    BigDecimal percentualGanhoPerda,
    List<InvestimentoResumoResponse> topInvestimentos
) {}