package com.gestaofinanceira.domain.entities;

import com.gestaofinanceira.domain.valueobjects.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.BigRange;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;
import net.jqwik.time.api.Dates;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for Domain Entities.
 * Tests universal properties that should hold for all valid entity operations.
 * 
 * **Validates: Requirements 3.6, 5.2**
 */
@Label("Feature: gestao-financeira-domestica, Domain Entities Properties")
class EntitiesPropertyTest {

    /**
     * **Property 10: Running balance invariant**
     * For any sequence of transactions, the running balance should equal the sum of all 
     * transaction values up to that point, regardless of the order transactions were entered.
     * 
     * **Validates: Requirements 3.6**
     */
    @Property(tries = 20)
    @Label("Property 10: Running balance invariant")
    void runningBalanceShouldEqualSumOfTransactions(
            @ForAll("validTransactionSequences") List<Transacao> transacoes) {
        
        // Act - Calculate running balance using transaction values with correct signs
        BigDecimal saldoCalculado = transacoes.stream()
            .filter(Transacao::isAtiva)
            .map(t -> t.getValorComSinal().quantia())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Act - Calculate expected balance by summing individual transaction impacts
        BigDecimal somaEsperada = BigDecimal.ZERO;
        for (Transacao transacao : transacoes) {
            if (transacao.isAtiva()) {
                if (transacao.getTipo().ehReceita()) {
                    somaEsperada = somaEsperada.add(transacao.getValor().quantia());
                } else {
                    somaEsperada = somaEsperada.subtract(transacao.getValor().quantia());
                }
            }
        }
        
        // Assert - Running balance should equal sum of all transaction values
        assertThat(saldoCalculado).isEqualTo(somaEsperada);
        
        // Assert - Balance calculation should be consistent regardless of processing order
        List<Transacao> transacoesEmbaralhadas = new ArrayList<>(transacoes);
        java.util.Collections.shuffle(transacoesEmbaralhadas);
        
        BigDecimal saldoEmbaralhado = transacoesEmbaralhadas.stream()
            .filter(Transacao::isAtiva)
            .map(t -> t.getValorComSinal().quantia())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        assertThat(saldoEmbaralhado).isEqualTo(saldoCalculado);
    }

    /**
     * **Property 12: Budget spending tracking accuracy**
     * For any budget and related transactions, the current spending amount should always 
     * equal the sum of all transactions in that category within the budget period.
     * 
     * **Validates: Requirements 5.2**
     */
    @Property(tries = 20)
    @Label("Property 12: Budget spending tracking accuracy")
    void budgetSpendingTrackingShouldBeAccurate(
            @ForAll("validBudgetWithTransactions") BudgetWithTransactions budgetData) {
        
        Orcamento orcamento = budgetData.orcamento();
        List<Transacao> transacoes = budgetData.transacoes();
        
        // Act - Apply all transactions to the budget
        Orcamento orcamentoAtualizado = recriarOrcamento(orcamento);
        
        for (Transacao transacao : transacoes) {
            if (shouldTransactionAffectBudget(transacao, orcamentoAtualizado)) {
                orcamentoAtualizado.adicionarGasto(transacao.getValor());
            }
        }
        
        // Act - Calculate expected spending by summing relevant transactions
        BigDecimal gastoEsperado = transacoes.stream()
            .filter(t -> shouldTransactionAffectBudget(t, orcamento))
            .map(t -> t.getValor().quantia())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Assert - Budget's current spending should equal sum of relevant transactions
        assertThat(orcamentoAtualizado.getGastoAtual().quantia()).isEqualTo(gastoEsperado);
        
        // Assert - Budget status should be consistent with spending amount
        if (gastoEsperado.compareTo(orcamento.getLimite().quantia()) > 0) {
            assertThat(orcamentoAtualizado.getStatus()).isEqualTo(StatusOrcamento.EXCEDIDO);
        } else if (gastoEsperado.compareTo(orcamento.getLimite().quantia().multiply(new BigDecimal("0.80"))) >= 0) {
            assertThat(orcamentoAtualizado.getStatus()).isIn(StatusOrcamento.PROXIMO_LIMITE, StatusOrcamento.EXCEDIDO);
        }
        
        // Assert - Percentage calculation should be accurate
        if (!orcamento.getLimite().ehZero()) {
            BigDecimal percentualEsperado = gastoEsperado
                .divide(orcamento.getLimite().quantia(), 4, java.math.RoundingMode.HALF_UP);
            assertThat(orcamentoAtualizado.calcularPercentualGasto()).isEqualTo(percentualEsperado);
        }
    }

    /**
     * Property: Transaction state consistency
     * For any transaction, state changes should maintain data integrity
     */
    @Property(tries = 20)
    @Label("Property: Transaction state consistency")
    void transactionStateChangesShouldMaintainIntegrity(
            @ForAll("validTransactions") Transacao transacao,
            @ForAll("validValores") Valor novoValor,
            @ForAll("validDescricoes") Descricao novaDescricao) {
        
        Assume.that(transacao.isAtiva());
        Assume.that(novoValor.ehPositivo());
        
        // Store original state
        Valor valorOriginal = transacao.getValor();
        Descricao descricaoOriginal = transacao.getDescricao();
        boolean foiModificadaOriginal = transacao.foiModificada();
        
        // Act - Update transaction
        transacao.atualizarValor(novoValor);
        transacao.atualizarDescricao(novaDescricao);
        
        // Assert - State should be updated correctly
        assertThat(transacao.getValor()).isEqualTo(novoValor);
        assertThat(transacao.getDescricao()).isEqualTo(novaDescricao);
        assertThat(transacao.foiModificada()).isTrue();
        assertThat(transacao.isAtiva()).isTrue();
        
        // Assert - Immutable fields should remain unchanged
        assertThat(transacao.getData()).isNotNull();
        assertThat(transacao.getTipo()).isNotNull();
        assertThat(transacao.getId()).isNotNull();
        assertThat(transacao.getUsuarioId()).isNotNull();
    }

    /**
     * Property: Budget lifecycle consistency
     * For any budget, lifecycle operations should maintain valid states
     */
    @Property(tries = 20)
    @Label("Property: Budget lifecycle consistency")
    void budgetLifecycleShouldBeConsistent(
            @ForAll("validBudgets") Orcamento orcamento,
            @ForAll("validValores") Valor gasto) {
        
        Assume.that(orcamento.getStatus().ehAtivo());
        Assume.that(gasto.ehPositivo());
        Assume.that(!orcamento.periodoExpirou());
        
        // Store original state
        Valor gastoOriginal = orcamento.getGastoAtual();
        StatusOrcamento statusOriginal = orcamento.getStatus();
        
        // Act - Add spending
        orcamento.adicionarGasto(gasto);
        
        // Assert - Spending should be accumulated correctly
        assertThat(orcamento.getGastoAtual()).isEqualTo(gastoOriginal.somar(gasto));
        
        // Assert - Status should be updated based on spending
        BigDecimal percentualGasto = orcamento.calcularPercentualGasto();
        if (percentualGasto.compareTo(BigDecimal.ONE) > 0) {
            assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.EXCEDIDO);
        } else if (percentualGasto.compareTo(new BigDecimal("0.80")) >= 0) {
            assertThat(orcamento.getStatus()).isIn(StatusOrcamento.PROXIMO_LIMITE, StatusOrcamento.EXCEDIDO);
        }
        
        // Assert - Calculations should be consistent
        assertThat(orcamento.calcularPercentualGasto().compareTo(BigDecimal.ZERO)).isGreaterThanOrEqualTo(0);
        
        if (!orcamento.excedeuLimite()) {
            assertThat(orcamento.calcularValorRestante().quantia().compareTo(BigDecimal.ZERO)).isGreaterThanOrEqualTo(0);
        }
    }

    /**
     * Property: Transaction balance calculation consistency
     * For any transaction, the value with sign should be consistent with transaction type
     */
    @Property(tries = 20)
    @Label("Property: Transaction balance calculation consistency")
    void transactionBalanceCalculationShouldBeConsistent(@ForAll("validTransactions") Transacao transacao) {
        // Act
        Valor valorComSinal = transacao.getValorComSinal();
        
        // Assert - Receitas should have positive sign, despesas should have negative sign
        if (transacao.getTipo().ehReceita()) {
            assertThat(valorComSinal.quantia()).isEqualTo(transacao.getValor().quantia());
            assertThat(valorComSinal.ehPositivo()).isTrue();
        } else {
            assertThat(valorComSinal.quantia()).isEqualTo(transacao.getValor().quantia().negate());
            assertThat(valorComSinal.ehNegativo()).isTrue();
        }
        
        // Assert - Currency should be preserved
        assertThat(valorComSinal.moeda()).isEqualTo(transacao.getValor().moeda());
    }

    /**
     * Property: Budget percentage calculation accuracy
     * For any budget with spending, percentage calculation should be mathematically correct
     */
    @Property(tries = 20)
    @Label("Property: Budget percentage calculation accuracy")
    void budgetPercentageCalculationShouldBeAccurate(
            @ForAll("validBudgets") Orcamento orcamento,
            @ForAll @BigRange(min = "0.01", max = "1000.00") BigDecimal gastoAmount) {
        
        Assume.that(orcamento.getStatus().ehAtivo());
        Assume.that(!orcamento.periodoExpirou());
        
        Valor gasto = Valor.reais(gastoAmount);
        
        // Act
        orcamento.adicionarGasto(gasto);
        BigDecimal percentualCalculado = orcamento.calcularPercentualGasto();
        
        // Assert - Percentage should be mathematically correct
        BigDecimal percentualEsperado = gasto.quantia()
            .divide(orcamento.getLimite().quantia(), 4, java.math.RoundingMode.HALF_UP);
        
        assertThat(percentualCalculado).isEqualTo(percentualEsperado);
        
        // Assert - Percentage should be non-negative
        assertThat(percentualCalculado.compareTo(BigDecimal.ZERO)).isGreaterThanOrEqualTo(0);
        
        // Assert - If spending equals limit, percentage should be 1.0
        if (gasto.equals(orcamento.getLimite())) {
            assertThat(percentualCalculado).isEqualTo(BigDecimal.ONE);
        }
    }

    private boolean shouldTransactionAffectBudget(Transacao transacao, Orcamento orcamento) {
        return transacao.isAtiva() &&
               transacao.getTipo().ehDespesa() &&
               transacao.getCategoria().equals(orcamento.getCategoria()) &&
               orcamento.contemData(transacao.getData());
    }

    private Orcamento recriarOrcamento(Orcamento original) {
        return Orcamento.criar(
            original.getUsuarioId(),
            original.getCategoria(),
            original.getLimite(),
            original.getPeriodo(),
            original.getInicioPeriodo()
        );
    }

    // Data generators

    @Provide
    Arbitrary<List<Transacao>> validTransactionSequences() {
        return validTransactions()
            .list()
            .ofMinSize(1)
            .ofMaxSize(20);
    }

    @Provide
    Arbitrary<Transacao> validTransactions() {
        Arbitrary<UsuarioId> usuarioIds = Arbitraries.create(() -> UsuarioId.gerar());
        Arbitrary<Valor> valores = validValores();
        Arbitrary<Descricao> descricoes = validDescricoes();
        Arbitrary<Categoria> categorias = Arbitraries.of(
            Categoria.ALIMENTACAO, Categoria.TRANSPORTE, Categoria.MORADIA,
            Categoria.LAZER, Categoria.SAUDE, Categoria.EDUCACAO, Categoria.OUTROS_GASTOS
        );
        Arbitrary<LocalDate> datas = Dates.dates()
            .between(LocalDate.now().minusYears(1), LocalDate.now());
        Arbitrary<TipoTransacao> tipos = Arbitraries.of(TipoTransacao.class);

        return Combinators.combine(usuarioIds, valores, descricoes, categorias, datas, tipos)
            .as((usuarioId, valor, descricao, categoria, data, tipo) -> {
                // Ensure category is compatible with transaction type
                Categoria categoriaCompativel = categoria;
                if (tipo.ehReceita() && categoria.ehDespesa()) {
                    categoriaCompativel = Categoria.SALARIO;
                } else if (tipo.ehDespesa() && categoria.ehReceita()) {
                    categoriaCompativel = Categoria.ALIMENTACAO;
                }
                
                return Transacao.criar(usuarioId, valor, descricao, categoriaCompativel, data, tipo);
            });
    }

    @Provide
    Arbitrary<BudgetWithTransactions> validBudgetWithTransactions() {
        return Combinators.combine(validBudgets(), validTransactionSequences())
            .as((orcamento, transacoes) -> {
                // Filter transactions to be relevant to the budget
                List<Transacao> transacoesRelevantes = transacoes.stream()
                    .map(t -> createTransactionForBudget(t, orcamento))
                    .collect(Collectors.toList());
                
                return new BudgetWithTransactions(orcamento, transacoesRelevantes);
            });
    }

    @Provide
    Arbitrary<Orcamento> validBudgets() {
        Arbitrary<UsuarioId> usuarioIds = Arbitraries.create(() -> UsuarioId.gerar());
        Arbitrary<Categoria> categoriasDespesa = Arbitraries.of(
            Categoria.ALIMENTACAO, Categoria.TRANSPORTE, Categoria.MORADIA,
            Categoria.LAZER, Categoria.SAUDE, Categoria.EDUCACAO, Categoria.OUTROS_GASTOS
        );
        Arbitrary<Valor> limites = Arbitraries.bigDecimals()
            .between(BigDecimal.valueOf(100.00), BigDecimal.valueOf(5000.00))
            .ofScale(2)
            .map(bd -> Valor.reais(bd));
        Arbitrary<PeriodoOrcamento> periodos = Arbitraries.of(PeriodoOrcamento.class);
        Arbitrary<LocalDate> iniciosPeriodo = Dates.dates()
            .between(LocalDate.now().minusMonths(1), LocalDate.now());

        return Combinators.combine(usuarioIds, categoriasDespesa, limites, periodos, iniciosPeriodo)
            .as(Orcamento::criar);
    }

    @Provide
    Arbitrary<Valor> validValores() {
        return Arbitraries.bigDecimals()
            .between(BigDecimal.valueOf(0.01), BigDecimal.valueOf(9999.99))
            .ofScale(2)
            .map(bd -> Valor.reais(bd));
    }

    @Provide
    Arbitrary<Descricao> validDescricoes() {
        return Arbitraries.strings()
            .alpha()
            .ofMinLength(5)
            .ofMaxLength(100)
            .map(Descricao::new);
    }

    private Transacao createTransactionForBudget(Transacao original, Orcamento orcamento) {
        // Create a transaction that might affect the budget
        LocalDate dataNoPerido = orcamento.getInicioPeriodo().plusDays(
            (int) (Math.random() * 28)); // Random day within budget period
        
        return Transacao.criar(
            original.getUsuarioId(),
            original.getValor(),
            original.getDescricao(),
            orcamento.getCategoria(), // Use budget's category
            dataNoPerido,
            TipoTransacao.DESPESA // Only expenses affect budgets
        );
    }

    // Helper record for budget with transactions
    record BudgetWithTransactions(Orcamento orcamento, List<Transacao> transacoes) {}
}
