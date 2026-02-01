package com.gestaofinanceira.infrastructure.service;

import com.gestaofinanceira.application.ports.service.IAAssessoraPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação mock do serviço de IA Assessora para desenvolvimento local.
 * Em produção, deve ser substituído por implementação real com integração a serviços de IA.
 */
@Service
public class IAAssessoraAdapter implements IAAssessoraPort {
    
    private static final Logger logger = LoggerFactory.getLogger(IAAssessoraAdapter.class);
    
    @Override
    public List<RecomendacaoIA> analisarGastos(DadosFinanceiros dadosFinanceiros) {
        logger.info("🤖 [MOCK] Analisando gastos - Saldo: {}, Receita: {}, Despesa: {}", 
                   dadosFinanceiros.saldoAtual(), 
                   dadosFinanceiros.receitaMensal(), 
                   dadosFinanceiros.despesaMensal());
        
        List<RecomendacaoIA> recomendacoes = new ArrayList<>();
        recomendacoes.add(new RecomendacaoIA(
            TipoRecomendacao.REDUCAO_GASTOS,
            "Reduza gastos com alimentação fora de casa",
            "Identificamos que você gasta 30% da sua renda com restaurantes",
            "Baseado no seu histórico dos últimos 3 meses",
            NivelPrioridade.MEDIA,
            new BigDecimal("500.00"),
            false
        ));
        
        return recomendacoes;
    }
    
    @Override
    public List<OportunidadeEconomia> identificarEconomias(HistoricoTransacoes historicoTransacoes) {
        logger.info("🤖 [MOCK] Identificando oportunidades de economia - {} transações", 
                   historicoTransacoes.transacoes().size());
        
        List<OportunidadeEconomia> oportunidades = new ArrayList<>();
        oportunidades.add(new OportunidadeEconomia(
            "Transporte",
            "Considere usar transporte público em vez de aplicativos de transporte",
            new BigDecimal("200.00"),
            new BigDecimal("25.0"),
            "Substitua 50% das viagens de app por transporte público",
            NivelConfianca.ALTA
        ));
        
        return oportunidades;
    }
    
    @Override
    public RecomendacaoInvestimento avaliarCarteira(CarteiraInvestimentos carteiraInvestimentos) {
        logger.info("🤖 [MOCK] Avaliando carteira - Valor total: {}", 
                   carteiraInvestimentos.valorTotal());
        
        return new RecomendacaoInvestimento(
            "Carteira bem diversificada",
            "Sua carteira apresenta boa diversificação entre renda fixa e variável",
            List.of("Diversificação adequada", "Baixo risco"),
            List.of("Considere aumentar exposição a ações"),
            List.of(new SugestaoRebalanceamento(
                "Renda Variável",
                new BigDecimal("30.0"),
                new BigDecimal("40.0"),
                "Aumentar exposição para melhor rentabilidade no longo prazo"
            )),
            "Esta é uma análise automatizada e não constitui recomendação de investimento"
        );
    }
    
    @Override
    public SugestaoCategoria sugerirCategoria(String descricaoTransacao, Map<String, String> historicoCategorizacao) {
        logger.debug("🤖 [MOCK] Sugerindo categoria para: {}", descricaoTransacao);
        
        String descricaoLower = descricaoTransacao.toLowerCase();
        String categoria = "Outros";
        List<String> alternativas = new ArrayList<>();
        
        if (descricaoLower.contains("mercado") || descricaoLower.contains("supermercado")) {
            categoria = "Alimentação";
            alternativas = List.of("Compras", "Despesas Domésticas");
        } else if (descricaoLower.contains("gasolina") || descricaoLower.contains("combustível")) {
            categoria = "Transporte";
            alternativas = List.of("Combustível", "Veículo");
        } else if (descricaoLower.contains("restaurante") || descricaoLower.contains("lanche")) {
            categoria = "Alimentação";
            alternativas = List.of("Restaurantes", "Lazer");
        }
        
        return new SugestaoCategoria(
            categoria,
            NivelConfianca.ALTA,
            "Baseado em padrões similares no seu histórico",
            alternativas
        );
    }
    
    @Override
    public AnaliseTendencias analisarTendencias(DadosHistoricos dadosHistoricos, int horizonteProjecao) {
        logger.info("🤖 [MOCK] Analisando tendências - Horizonte: {} meses", horizonteProjecao);
        
        TendenciaGeral tendenciaGeral = new TendenciaGeral(
            DirecaoTendencia.ESTAVEL,
            "Suas finanças estão estáveis nos últimos meses",
            new BigDecimal("2.5")
        );
        
        Map<String, TendenciaCategoria> tendenciasPorCategoria = new HashMap<>();
        tendenciasPorCategoria.put("Alimentação", new TendenciaCategoria(
            "Alimentação",
            DirecaoTendencia.CRESCENTE,
            new BigDecimal("5.0"),
            "Gastos aumentando gradualmente"
        ));
        
        List<ProjecaoMensal> projecoes = new ArrayList<>();
        List<AlertaTendencia> alertas = new ArrayList<>();
        
        return new AnaliseTendencias(
            tendenciaGeral,
            tendenciasPorCategoria,
            projecoes,
            alertas
        );
    }
}
