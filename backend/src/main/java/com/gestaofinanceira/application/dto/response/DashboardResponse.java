package com.gestaofinanceira.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para resposta do dashboard financeiro.
 * 
 * Agrega todas as informações principais para exibição no dashboard:
 * - Saldo atual e movimentação mensal
 * - Status dos orçamentos ativos
 * - Progresso das metas ativas
 * - Transações recentes
 * - Resumo de investimentos (se houver)
 */
public record DashboardResponse(
    BigDecimal saldoAtual,
    BigDecimal receitaMensal,
    BigDecimal despesaMensal,
    BigDecimal saldoMensal,
    List<OrcamentoStatusResponse> statusOrcamentos,
    List<MetaProgressoResponse> progressoMetas,
    List<TransacaoResponse> transacoesRecentes,
    ResumoInvestimentosResponse resumoInvestimentos
) {}