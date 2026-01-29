package com.gestaofinanceira.domain.entities;

import com.gestaofinanceira.domain.valueobjects.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MetaFinanceiraTest {
    
    private UsuarioId usuarioId;
    private Nome nome;
    private Valor valorAlvo;
    private LocalDate prazo;
    private TipoMeta tipo;
    
    @BeforeEach
    void setUp() {
        usuarioId = UsuarioId.gerar();
        nome = new Nome("Reserva de Emergência");
        valorAlvo = Valor.reais(new BigDecimal("10000.00"));
        prazo = LocalDate.now().plusMonths(12);
        tipo = TipoMeta.RESERVA_EMERGENCIA;
    }
    
    @Test
    void deveCriarMetaComDadosValidos() {
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazo, tipo);
        
        assertNotNull(meta.getId());
        assertEquals(usuarioId, meta.getUsuarioId());
        assertEquals(nome, meta.getNome());
        assertEquals(valorAlvo, meta.getValorAlvo());
        assertTrue(meta.getValorAtual().ehZero());
        assertEquals(prazo, meta.getPrazo());
        assertEquals(tipo, meta.getTipo());
        assertEquals(StatusMeta.ATIVA, meta.getStatus());
        assertNotNull(meta.getCriadoEm());
    }
    
    @Test
    void deveRejeitarValorAlvoNegativo() {
        Valor valorNegativo = Valor.reais(new BigDecimal("-1000.00"));
        
        assertThrows(IllegalArgumentException.class, 
            () -> MetaFinanceira.criar(usuarioId, nome, valorNegativo, prazo, tipo));
    }
    
    @Test
    void deveAdicionarProgresso() {
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazo, tipo);
        Valor contribuicao = Valor.reais(new BigDecimal("1000.00"));
        
        meta.adicionarProgresso(contribuicao);
        
        assertEquals(contribuicao, meta.getValorAtual());
        assertEquals(StatusMeta.ATIVA, meta.getStatus());
    }
    
    @Test
    void deveMarcarMetaComoConcluida() {
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazo, tipo);
        
        meta.adicionarProgresso(valorAlvo);
        
        assertEquals(StatusMeta.CONCLUIDA, meta.getStatus());
        assertTrue(meta.getStatus().foiConcluida());
    }
    
    @Test
    void deveRemoverProgresso() {
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazo, tipo);
        Valor contribuicao1 = Valor.reais(new BigDecimal("2000.00"));
        Valor contribuicao2 = Valor.reais(new BigDecimal("500.00"));
        
        meta.adicionarProgresso(contribuicao1);
        meta.adicionarProgresso(contribuicao2);
        
        assertEquals(Valor.reais(new BigDecimal("2500.00")), meta.getValorAtual());
        
        meta.removerProgresso(contribuicao2);
        
        assertEquals(contribuicao1, meta.getValorAtual());
    }
    
    @Test
    void naoDevePermitirValorAtualNegativo() {
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazo, tipo);
        Valor remocaoMaiorQueAtual = Valor.reais(new BigDecimal("1000.00"));
        
        meta.removerProgresso(remocaoMaiorQueAtual);
        
        assertTrue(meta.getValorAtual().ehZero());
    }
    
    @Test
    void deveAtualizarNome() {
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazo, tipo);
        Nome novoNome = new Nome("Férias na Europa");
        
        meta.atualizarNome(novoNome);
        
        assertEquals(novoNome, meta.getNome());
    }
    
    @Test
    void deveAtualizarValorAlvo() {
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazo, tipo);
        Valor novoValorAlvo = Valor.reais(new BigDecimal("15000.00"));
        
        meta.atualizarValorAlvo(novoValorAlvo);
        
        assertEquals(novoValorAlvo, meta.getValorAlvo());
    }
    
    @Test
    void deveAtualizarPrazo() {
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazo, tipo);
        LocalDate novoPrazo = LocalDate.now().plusMonths(18);
        
        meta.atualizarPrazo(novoPrazo);
        
        assertEquals(novoPrazo, meta.getPrazo());
    }
    
    @Test
    void devePausarMeta() {
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazo, tipo);
        
        assertTrue(meta.getStatus().ehAtiva());
        
        meta.pausar();
        
        assertEquals(StatusMeta.PAUSADA, meta.getStatus());
        assertTrue(meta.getStatus().estaPausada());
    }
    
    @Test
    void deveRetomarMeta() {
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazo, tipo);
        meta.pausar();
        
        assertTrue(meta.getStatus().estaPausada());
        
        meta.retomar();
        
        assertEquals(StatusMeta.ATIVA, meta.getStatus());
    }
    
    @Test
    void deveCancelarMeta() {
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazo, tipo);
        
        assertFalse(meta.getStatus().foiCancelada());
        
        meta.cancelar();
        
        assertEquals(StatusMeta.CANCELADA, meta.getStatus());
        assertTrue(meta.getStatus().foiCancelada());
    }
    
    @Test
    void naoDevePermitirCancelarMetaConcluida() {
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazo, tipo);
        meta.adicionarProgresso(valorAlvo);
        
        assertTrue(meta.getStatus().foiConcluida());
        
        assertThrows(IllegalStateException.class, () -> meta.cancelar());
    }
    
    @Test
    void naoDevePermitirContribuicaoEmMetaPausada() {
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazo, tipo);
        meta.pausar();
        
        Valor contribuicao = Valor.reais(new BigDecimal("1000.00"));
        
        assertThrows(IllegalStateException.class, 
            () -> meta.adicionarProgresso(contribuicao));
    }
    
    @Test
    void deveCalcularPercentualConclusao() {
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazo, tipo);
        Valor contribuicao = Valor.reais(new BigDecimal("2500.00")); // 25% do valor alvo
        
        meta.adicionarProgresso(contribuicao);
        
        BigDecimal percentual = meta.calcularPercentualConclusao();
        assertEquals(new BigDecimal("25.0000"), percentual);
    }
    
    @Test
    void deveCalcularValorRestante() {
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazo, tipo);
        Valor contribuicao = Valor.reais(new BigDecimal("3000.00"));
        
        meta.adicionarProgresso(contribuicao);
        
        Valor valorRestante = meta.calcularValorRestante();
        assertEquals(Valor.reais(new BigDecimal("7000.00")), valorRestante);
    }
    
    @Test
    void deveEstimarDataConclusao() {
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazo, tipo);
        
        // Sem progresso, deve retornar o prazo original
        assertEquals(prazo, meta.estimarDataConclusao());
        
        // Com progresso, deve calcular baseado na velocidade
        Valor contribuicao = Valor.reais(new BigDecimal("1000.00"));
        meta.adicionarProgresso(contribuicao);
        
        LocalDate dataEstimada = meta.estimarDataConclusao();
        assertNotNull(dataEstimada);
        assertFalse(dataEstimada.isBefore(LocalDate.now()));
    }
    
    @Test
    void deveCalcularDiasRestantes() {
        LocalDate prazoProximo = LocalDate.now().plusDays(30);
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazoProximo, tipo);
        
        long diasRestantes = meta.calcularDiasRestantes();
        assertEquals(30, diasRestantes);
    }
    
    @Test
    void deveDetectarMetaAtrasada() {
        LocalDate prazoPassado = LocalDate.now().minusDays(10);
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazoPassado, tipo);
        
        assertTrue(meta.estaAtrasada());
    }
    
    @Test
    void deveDetectarMetaProximaDoPrazo() {
        LocalDate prazoProximo = LocalDate.now().plusDays(15);
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazoProximo, tipo);
        
        assertTrue(meta.estaProximaDoPrazo());
    }
    
    @Test
    void deveVerificarVencimento() {
        LocalDate prazoPassado = LocalDate.now().minusDays(1);
        MetaFinanceira meta = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazoPassado, tipo);
        
        meta.verificarVencimento();
        
        assertEquals(StatusMeta.VENCIDA, meta.getStatus());
    }
    
    @Test
    void deveImplementarEqualsEHashCodeCorretamente() {
        MetaId id = MetaId.gerar();
        MetaFinanceira meta1 = new MetaFinanceira(id, usuarioId, nome, valorAlvo, prazo, tipo);
        MetaFinanceira meta2 = new MetaFinanceira(id, usuarioId, nome, valorAlvo, prazo, tipo);
        MetaFinanceira meta3 = MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazo, tipo);
        
        assertEquals(meta1, meta2);
        assertNotEquals(meta1, meta3);
        assertEquals(meta1.hashCode(), meta2.hashCode());
    }
    
    @Test
    void deveRejeitarParametrosNulos() {
        assertThrows(NullPointerException.class, 
            () -> MetaFinanceira.criar(null, nome, valorAlvo, prazo, tipo));
        assertThrows(NullPointerException.class, 
            () -> MetaFinanceira.criar(usuarioId, null, valorAlvo, prazo, tipo));
        assertThrows(NullPointerException.class, 
            () -> MetaFinanceira.criar(usuarioId, nome, null, prazo, tipo));
        assertThrows(NullPointerException.class, 
            () -> MetaFinanceira.criar(usuarioId, nome, valorAlvo, null, tipo));
        assertThrows(NullPointerException.class, 
            () -> MetaFinanceira.criar(usuarioId, nome, valorAlvo, prazo, null));
    }
}