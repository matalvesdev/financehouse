package com.gestaofinanceira.application.ports.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Port para integração com serviços de IA Assessora.
 * 
 * Define as operações necessárias para obter insights e recomendações
 * de inteligência artificial sobre dados financeiros, sempre mantendo
 * o usuário no controle das decisões.
 */
public interface IAAssessoraPort {
    
    /**
     * Analisa padrões de gastos e fornece recomendações.
     * 
     * @param dadosFinanceiros dados financeiros do usuário
     * @return lista de recomendações de IA
     */
    List<RecomendacaoIA> analisarGastos(DadosFinanceiros dadosFinanceiros);
    
    /**
     * Identifica oportunidades de economia baseadas no histórico.
     * 
     * @param historicoTransacoes histórico de transações do usuário
     * @return lista de oportunidades de economia identificadas
     */
    List<OportunidadeEconomia> identificarEconomias(HistoricoTransacoes historicoTransacoes);
    
    /**
     * Avalia carteira de investimentos e fornece insights.
     * 
     * @param carteiraInvestimentos dados da carteira de investimentos
     * @return recomendações sobre a carteira
     */
    RecomendacaoInvestimento avaliarCarteira(CarteiraInvestimentos carteiraInvestimentos);
    
    /**
     * Sugere categorização automática para transações.
     * 
     * @param descricaoTransacao descrição da transação
     * @param historicoCategorizacao histórico de categorizações do usuário
     * @return sugestão de categoria com nível de confiança
     */
    SugestaoCategoria sugerirCategoria(String descricaoTransacao, Map<String, String> historicoCategorizacao);
    
    /**
     * Analisa tendências financeiras e projeta cenários futuros.
     * 
     * @param dadosHistoricos dados históricos do usuário
     * @param horizonteProjecao período para projeção (em meses)
     * @return análise de tendências e projeções
     */
    AnaliseTendencias analisarTendencias(DadosHistoricos dadosHistoricos, int horizonteProjecao);
    
    /**
     * Representa dados financeiros para análise.
     */
    record DadosFinanceiros(
        BigDecimal saldoAtual,
        BigDecimal receitaMensal,
        BigDecimal despesaMensal,
        Map<String, BigDecimal> gastosPorCategoria,
        List<TransacaoAnalise> transacoesRecentes
    ) {}
    
    /**
     * Representa histórico de transações para análise.
     */
    record HistoricoTransacoes(
        List<TransacaoAnalise> transacoes,
        Map<String, BigDecimal> mediaPorCategoria,
        Map<String, List<BigDecimal>> tendenciasPorCategoria
    ) {}
    
    /**
     * Representa dados de carteira de investimentos.
     */
    record CarteiraInvestimentos(
        BigDecimal valorTotal,
        List<InvestimentoAnalise> investimentos,
        Map<String, BigDecimal> alocacaoPorTipo,
        BigDecimal rentabilidadeTotal
    ) {}
    
    /**
     * Representa dados históricos para análise de tendências.
     */
    record DadosHistoricos(
        List<TransacaoAnalise> transacoes,
        List<SaldoMensal> saldosMensais,
        List<OrcamentoHistorico> orcamentosHistoricos
    ) {}
    
    /**
     * Representa uma transação para análise de IA.
     */
    record TransacaoAnalise(
        LocalDate data,
        BigDecimal valor,
        String descricao,
        String categoria,
        String tipo
    ) {}
    
    /**
     * Representa um investimento para análise.
     */
    record InvestimentoAnalise(
        String ativo,
        BigDecimal quantidade,
        BigDecimal precoCompra,
        BigDecimal precoAtual,
        String tipoAtivo,
        LocalDate dataCompra
    ) {}
    
    /**
     * Representa saldo mensal para análise de tendências.
     */
    record SaldoMensal(
        int ano,
        int mes,
        BigDecimal saldo,
        BigDecimal receitas,
        BigDecimal despesas
    ) {}
    
    /**
     * Representa histórico de orçamento.
     */
    record OrcamentoHistorico(
        String categoria,
        BigDecimal limite,
        BigDecimal gastoReal,
        int ano,
        int mes
    ) {}
    
    /**
     * Representa uma recomendação de IA.
     */
    record RecomendacaoIA(
        TipoRecomendacao tipo,
        String titulo,
        String descricao,
        String justificativa,
        NivelPrioridade prioridade,
        BigDecimal impactoEstimado,
        boolean requerConfirmacao
    ) {}
    
    /**
     * Representa uma oportunidade de economia.
     */
    record OportunidadeEconomia(
        String categoria,
        String descricao,
        BigDecimal economiaEstimada,
        BigDecimal economiaPercentual,
        String sugestaoAcao,
        NivelConfianca confianca
    ) {}
    
    /**
     * Representa recomendação de investimento.
     */
    record RecomendacaoInvestimento(
        String titulo,
        String descricao,
        List<String> pontosFortesCarteira,
        List<String> pontosAtencao,
        List<SugestaoRebalanceamento> sugestoesRebalanceamento,
        String avisoLegal
    ) {}
    
    /**
     * Representa sugestão de categoria.
     */
    record SugestaoCategoria(
        String categoria,
        NivelConfianca confianca,
        String justificativa,
        List<String> categoriasAlternativas
    ) {}
    
    /**
     * Representa análise de tendências.
     */
    record AnaliseTendencias(
        TendenciaGeral tendenciaGeral,
        Map<String, TendenciaCategoria> tendenciasPorCategoria,
        List<ProjecaoMensal> projecoes,
        List<AlertaTendencia> alertas
    ) {}
    
    /**
     * Representa sugestão de rebalanceamento de carteira.
     */
    record SugestaoRebalanceamento(
        String tipoAtivo,
        BigDecimal alocacaoAtual,
        BigDecimal alocacaoSugerida,
        String justificativa
    ) {}
    
    /**
     * Representa tendência geral das finanças.
     */
    record TendenciaGeral(
        DirecaoTendencia direcao,
        String descricao,
        BigDecimal variacaoPercentual
    ) {}
    
    /**
     * Representa tendência por categoria.
     */
    record TendenciaCategoria(
        String categoria,
        DirecaoTendencia direcao,
        BigDecimal variacaoMedia,
        String observacao
    ) {}
    
    /**
     * Representa projeção mensal.
     */
    record ProjecaoMensal(
        int ano,
        int mes,
        BigDecimal receitaProjetada,
        BigDecimal despesaProjetada,
        BigDecimal saldoProjetado,
        NivelConfianca confiancaProjecao
    ) {}
    
    /**
     * Representa alerta de tendência.
     */
    record AlertaTendencia(
        TipoAlerta tipo,
        String categoria,
        String mensagem,
        NivelPrioridade prioridade
    ) {}
    
    /**
     * Tipos de recomendação.
     */
    enum TipoRecomendacao {
        REDUCAO_GASTOS,
        OTIMIZACAO_ORCAMENTO,
        AJUSTE_META,
        MELHORIA_HABITO,
        OPORTUNIDADE_INVESTIMENTO
    }
    
    /**
     * Níveis de prioridade.
     */
    enum NivelPrioridade {
        BAIXA,
        MEDIA,
        ALTA,
        CRITICA
    }
    
    /**
     * Níveis de confiança.
     */
    enum NivelConfianca {
        BAIXA,
        MEDIA,
        ALTA
    }
    
    /**
     * Direções de tendência.
     */
    enum DirecaoTendencia {
        CRESCENTE,
        DECRESCENTE,
        ESTAVEL,
        VOLATIL
    }
    
    /**
     * Tipos de alerta.
     */
    enum TipoAlerta {
        GASTO_CRESCENTE,
        ORCAMENTO_RISCO,
        META_ATRASO,
        PADRAO_INCOMUM
    }
}