package com.gestaofinanceira.domain.entities;

import com.gestaofinanceira.domain.valueobjects.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class OrcamentoTest {
    
    private UsuarioId usuarioId;
    private Categoria categoria;
    private Valor limite;
    private PeriodoOrcamento periodo;
    private LocalDate inicioPeriodo;
    
    @BeforeEach
    void setUp() {
        usuarioId = UsuarioId.gerar();
        categoria = Categoria.ALIMENTACAO;
        limite = Valor.reais(new BigDecimal("500.00"));
        periodo = PeriodoOrcamento.MENSAL;
        inicioPeriodo = LocalDate.now().withDayOfMonth(1);
    }
    
    @Test
    void deveCriarOrcamentoComDadosValidos() {
        Orcamento orcamento = Orcamento.criar(usuarioId, categoria, limite, periodo, inicioPeriodo);
        
        assertNotNull(orcamento.getId());
        assertEquals(usuarioId, orcamento.getUsuarioId());
        assertEquals(categoria, orcamento.getCategoria());
        assertEquals(limite, orcamento.getLimite());
        assertEquals(periodo, orcamento.getPeriodo());
        assertEquals(inicioPeriodo, orcamento.getInicioPeriodo());
        assertEquals(StatusOrcamento.ATIVO, orcamento.getStatus());
        assertTrue(orcamento.getGastoAtual().ehZero());
        assertNotNull(orcamento.getCriadoEm());
    }
    
    @Test
    void deveRejeitarLimiteNegativo() {
        Valor limiteNegativo = Valor.reais(new BigDecimal("-100.00"));
        
        assertThrows(IllegalArgumentException.class, 
            () -> Orcamento.criar(usuarioId, categoria, limiteNegativo, periodo, inicioPeriodo));
    }
    
    @Test
    void deveRejeitarCategoriaDeReceita() {
        assertThrows(IllegalArgumentException.class, 
            () -> Orcamento.criar(usuarioId, Categoria.SALARIO, limite, periodo, inicioPeriodo));
    }
    
    @Test
    void deveAdicionarGasto() {
        Orcamento orcamento = Orcamento.criar(usuarioId, categoria, limite, periodo, inicioPeriodo);
        Valor gasto = Valor.reais(new BigDecimal("100.00"));
        
        orcamento.adicionarGasto(gasto);
        
        assertEquals(gasto, orcamento.getGastoAtual());
        assertEquals(StatusOrcamento.ATIVO, orcamento.getStatus());
    }
    
    @Test
    void deveDetectarOrcamentoProximoDoLimite() {
        Orcamento orcamento = Orcamento.criar(usuarioId, categoria, limite, periodo, inicioPeriodo);
        Valor gasto = Valor.reais(new BigDecimal("400.00")); // 80% do limite
        
        orcamento.adicionarGasto(gasto);
        
        assertTrue(orcamento.estaProximoDoLimite());
        assertEquals(StatusOrcamento.PROXIMO_LIMITE, orcamento.getStatus());
    }
    
    @Test
    void deveDetectarOrcamentoExcedido() {
        Orcamento orcamento = Orcamento.criar(usuarioId, categoria, limite, periodo, inicioPeriodo);
        Valor gasto = Valor.reais(new BigDecimal("600.00")); // Mais que o limite
        
        orcamento.adicionarGasto(gasto);
        
        assertTrue(orcamento.excedeuLimite());
        assertEquals(StatusOrcamento.EXCEDIDO, orcamento.getStatus());
    }
    
    @Test
    void deveRemoverGasto() {
        Orcamento orcamento = Orcamento.criar(usuarioId, categoria, limite, periodo, inicioPeriodo);
        Valor gasto1 = Valor.reais(new BigDecimal("200.00"));
        Valor gasto2 = Valor.reais(new BigDecimal("100.00"));
        
        orcamento.adicionarGasto(gasto1);
        orcamento.adicionarGasto(gasto2);
        
        assertEquals(Valor.reais(new BigDecimal("300.00")), orcamento.getGastoAtual());
        
        orcamento.removerGasto(gasto2);
        
        assertEquals(gasto1, orcamento.getGastoAtual());
    }
    
    @Test
    void naoDevePermitirGastoNegativo() {
        Orcamento orcamento = Orcamento.criar(usuarioId, categoria, limite, periodo, inicioPeriodo);
        Valor gastoMaiorQueAtual = Valor.reais(new BigDecimal("100.00"));
        
        orcamento.removerGasto(gastoMaiorQueAtual);
        
        assertTrue(orcamento.getGastoAtual().ehZero());
    }
    
    @Test
    void deveAtualizarLimite() {
        Orcamento orcamento = Orcamento.criar(usuarioId, categoria, limite, periodo, inicioPeriodo);
        Valor novoLimite = Valor.reais(new BigDecimal("800.00"));
        
        orcamento.atualizarLimite(novoLimite);
        
        assertEquals(novoLimite, orcamento.getLimite());
    }
    
    @Test
    void deveArquivarOrcamento() {
        Orcamento orcamento = Orcamento.criar(usuarioId, categoria, limite, periodo, inicioPeriodo);
        
        assertNotEquals(StatusOrcamento.ARQUIVADO, orcamento.getStatus());
        
        orcamento.arquivar();
        
        assertEquals(StatusOrcamento.ARQUIVADO, orcamento.getStatus());
    }
    
    @Test
    void naoDevePermitirOperacoesEmOrcamentoArquivado() {
        Orcamento orcamento = Orcamento.criar(usuarioId, categoria, limite, periodo, inicioPeriodo);
        orcamento.arquivar();
        
        Valor gasto = Valor.reais(new BigDecimal("100.00"));
        Valor novoLimite = Valor.reais(new BigDecimal("800.00"));
        
        assertThrows(IllegalStateException.class, 
            () -> orcamento.adicionarGasto(gasto));
        assertThrows(IllegalStateException.class, 
            () -> orcamento.removerGasto(gasto));
        assertThrows(IllegalStateException.class, 
            () -> orcamento.atualizarLimite(novoLimite));
    }
    
    @Test
    void deveCalcularPercentualGasto() {
        Orcamento orcamento = Orcamento.criar(usuarioId, categoria, limite, periodo, inicioPeriodo);
        Valor gasto = Valor.reais(new BigDecimal("250.00")); // 50% do limite
        
        orcamento.adicionarGasto(gasto);
        
        BigDecimal percentual = orcamento.calcularPercentualGasto();
        assertEquals(new BigDecimal("0.5000"), percentual);
    }
    
    @Test
    void deveCalcularValorRestante() {
        Orcamento orcamento = Orcamento.criar(usuarioId, categoria, limite, periodo, inicioPeriodo);
        Valor gasto = Valor.reais(new BigDecimal("200.00"));
        
        orcamento.adicionarGasto(gasto);
        
        Valor valorRestante = orcamento.calcularValorRestante();
        assertEquals(Valor.reais(new BigDecimal("300.00")), valorRestante);
    }
    
    @Test
    void deveCalcularValorExcedido() {
        Orcamento orcamento = Orcamento.criar(usuarioId, categoria, limite, periodo, inicioPeriodo);
        Valor gasto = Valor.reais(new BigDecimal("600.00"));
        
        orcamento.adicionarGasto(gasto);
        
        Valor valorExcedido = orcamento.calcularValorExcedido();
        assertEquals(Valor.reais(new BigDecimal("100.00")), valorExcedido);
    }
    
    @Test
    void deveVerificarSeContemData() {
        Orcamento orcamento = Orcamento.criar(usuarioId, categoria, limite, periodo, inicioPeriodo);
        
        LocalDate dataNoMes = inicioPeriodo.plusDays(15);
        LocalDate dataForaDoMes = inicioPeriodo.plusMonths(1);
        
        assertTrue(orcamento.contemData(dataNoMes));
        assertFalse(orcamento.contemData(dataForaDoMes));
    }
    
    @Test
    void deveVerificarPeriodoExpirado() {
        LocalDate inicioPeriodoPassado = LocalDate.now().minusMonths(2);
        Orcamento orcamento = Orcamento.criar(usuarioId, categoria, limite, periodo, inicioPeriodoPassado);
        
        assertTrue(orcamento.periodoExpirou());
        assertFalse(orcamento.podeReceberGastos());
    }
    
    @Test
    void deveImplementarEqualsEHashCodeCorretamente() {
        OrcamentoId id = OrcamentoId.gerar();
        Orcamento orcamento1 = new Orcamento(id, usuarioId, categoria, limite, periodo, inicioPeriodo);
        Orcamento orcamento2 = new Orcamento(id, usuarioId, categoria, limite, periodo, inicioPeriodo);
        Orcamento orcamento3 = Orcamento.criar(usuarioId, categoria, limite, periodo, inicioPeriodo);
        
        assertEquals(orcamento1, orcamento2);
        assertNotEquals(orcamento1, orcamento3);
        assertEquals(orcamento1.hashCode(), orcamento2.hashCode());
    }
    
    @Test
    void deveRejeitarParametrosNulos() {
        assertThrows(NullPointerException.class, 
            () -> Orcamento.criar(null, categoria, limite, periodo, inicioPeriodo));
        assertThrows(NullPointerException.class, 
            () -> Orcamento.criar(usuarioId, null, limite, periodo, inicioPeriodo));
        assertThrows(NullPointerException.class, 
            () -> Orcamento.criar(usuarioId, categoria, null, periodo, inicioPeriodo));
        assertThrows(NullPointerException.class, 
            () -> Orcamento.criar(usuarioId, categoria, limite, null, inicioPeriodo));
        assertThrows(NullPointerException.class, 
            () -> Orcamento.criar(usuarioId, categoria, limite, periodo, null));
    }
}