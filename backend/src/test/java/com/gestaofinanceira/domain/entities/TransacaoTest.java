package com.gestaofinanceira.domain.entities;

import com.gestaofinanceira.domain.valueobjects.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Transacao Entity Tests")
class TransacaoTest {
    
    private UsuarioId usuarioId;
    private Valor valor;
    private Descricao descricao;
    private Categoria categoria;
    private LocalDate data;
    
    @BeforeEach
    void setUp() {
        usuarioId = UsuarioId.gerar();
        valor = Valor.reais(new BigDecimal("100.00"));
        descricao = new Descricao("Compra no supermercado");
        categoria = Categoria.ALIMENTACAO;
        data = LocalDate.now().minusDays(1);
    }
    
    @Test
    void deveCriarTransacaoDeReceita() {
        Transacao transacao = Transacao.criar(usuarioId, valor, descricao, 
            Categoria.SALARIO, data, TipoTransacao.RECEITA);
        
        assertThat(transacao.getId()).isNotNull();
        assertThat(transacao.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(transacao.getValor()).isEqualTo(valor);
        assertThat(transacao.getDescricao()).isEqualTo(descricao);
        assertThat(transacao.getCategoria()).isEqualTo(Categoria.SALARIO);
        assertThat(transacao.getData()).isEqualTo(data);
        assertThat(transacao.getTipo()).isEqualTo(TipoTransacao.RECEITA);
        assertThat(transacao.isAtiva()).isTrue();
        assertThat(transacao.getCriadoEm()).isNotNull();
    }
    
    @Test
    void deveCriarTransacaoDeDespesa() {
        Transacao transacao = Transacao.criar(usuarioId, valor, descricao, 
            categoria, data, TipoTransacao.DESPESA);
        
        assertEquals(TipoTransacao.DESPESA, transacao.getTipo());
        assertTrue(transacao.afetaOrcamento());
    }
    
    @Test
    void deveRejeitarValorNegativo() {
        Valor valorNegativo = Valor.reais(new BigDecimal("-50.00"));
        
        assertThrows(IllegalArgumentException.class, 
            () -> Transacao.criar(usuarioId, valorNegativo, descricao, categoria, data, TipoTransacao.DESPESA));
    }
    
    @Test
    void deveRejeitarDataFutura() {
        LocalDate dataFutura = LocalDate.now().plusDays(1);
        
        assertThrows(IllegalArgumentException.class, 
            () -> Transacao.criar(usuarioId, valor, descricao, categoria, dataFutura, TipoTransacao.DESPESA));
    }
    
    @Test
    void deveRejeitarCategoriaIncompativel() {
        // Categoria de despesa em receita
        assertThrows(IllegalArgumentException.class, 
            () -> Transacao.criar(usuarioId, valor, descricao, Categoria.ALIMENTACAO, data, TipoTransacao.RECEITA));
        
        // Categoria de receita em despesa
        assertThrows(IllegalArgumentException.class, 
            () -> Transacao.criar(usuarioId, valor, descricao, Categoria.SALARIO, data, TipoTransacao.DESPESA));
    }
    
    @Test
    void deveAtualizarValor() {
        Transacao transacao = Transacao.criar(usuarioId, valor, descricao, categoria, data, TipoTransacao.DESPESA);
        Valor novoValor = Valor.reais(new BigDecimal("150.00"));
        
        transacao.atualizarValor(novoValor);
        
        assertEquals(novoValor, transacao.getValor());
        assertTrue(transacao.foiModificada());
    }
    
    @Test
    void deveAtualizarDescricao() {
        Transacao transacao = Transacao.criar(usuarioId, valor, descricao, categoria, data, TipoTransacao.DESPESA);
        Descricao novaDescricao = new Descricao("Compra na farmácia");
        
        transacao.atualizarDescricao(novaDescricao);
        
        assertEquals(novaDescricao, transacao.getDescricao());
    }
    
    @Test
    void deveAtualizarCategoria() {
        Transacao transacao = Transacao.criar(usuarioId, valor, descricao, categoria, data, TipoTransacao.DESPESA);
        Categoria novaCategoria = Categoria.SAUDE;
        
        transacao.atualizarCategoria(novaCategoria);
        
        assertEquals(novaCategoria, transacao.getCategoria());
    }
    
    @Test
    void deveDesativarTransacao() {
        Transacao transacao = Transacao.criar(usuarioId, valor, descricao, categoria, data, TipoTransacao.DESPESA);
        
        assertTrue(transacao.isAtiva());
        assertTrue(transacao.afetaOrcamento());
        
        transacao.desativar();
        
        assertFalse(transacao.isAtiva());
        assertFalse(transacao.afetaOrcamento());
    }
    
    @Test
    void deveReativarTransacao() {
        Transacao transacao = Transacao.criar(usuarioId, valor, descricao, categoria, data, TipoTransacao.DESPESA);
        transacao.desativar();
        
        assertFalse(transacao.isAtiva());
        
        transacao.reativar();
        
        assertTrue(transacao.isAtiva());
    }
    
    @Test
    void naoDevePermitirAtualizacaoEmTransacaoInativa() {
        Transacao transacao = Transacao.criar(usuarioId, valor, descricao, categoria, data, TipoTransacao.DESPESA);
        transacao.desativar();
        
        Valor novoValor = Valor.reais(new BigDecimal("150.00"));
        Descricao novaDescricao = new Descricao("Nova descrição");
        Categoria novaCategoria = Categoria.SAUDE;
        
        assertThrows(IllegalStateException.class, 
            () -> transacao.atualizarValor(novoValor));
        assertThrows(IllegalStateException.class, 
            () -> transacao.atualizarDescricao(novaDescricao));
        assertThrows(IllegalStateException.class, 
            () -> transacao.atualizarCategoria(novaCategoria));
    }
    
    @Test
    void deveCalcularValorComSinalCorreto() {
        Transacao receita = Transacao.criar(usuarioId, valor, descricao, 
            Categoria.SALARIO, data, TipoTransacao.RECEITA);
        Transacao despesa = Transacao.criar(usuarioId, valor, descricao, 
            categoria, data, TipoTransacao.DESPESA);
        
        assertEquals(valor, receita.getValorComSinal());
        assertEquals(valor.negar(), despesa.getValorComSinal());
    }
    
    @Test
    void deveVerificarPertencimentoAoPeriodo() {
        LocalDate dataTransacao = LocalDate.of(2024, 1, 15);
        Transacao transacao = Transacao.criar(usuarioId, valor, descricao, 
            categoria, dataTransacao, TipoTransacao.DESPESA);
        
        LocalDate inicioJaneiro = LocalDate.of(2024, 1, 1);
        LocalDate fimJaneiro = LocalDate.of(2024, 1, 31);
        LocalDate inicioFevereiro = LocalDate.of(2024, 2, 1);
        LocalDate fimFevereiro = LocalDate.of(2024, 2, 29);
        
        assertTrue(transacao.pertenceAoPeriodo(inicioJaneiro, fimJaneiro));
        assertFalse(transacao.pertenceAoPeriodo(inicioFevereiro, fimFevereiro));
    }
    
    @Test
    void deveImplementarEqualsEHashCodeCorretamente() {
        TransacaoId id = TransacaoId.gerar();
        Transacao transacao1 = new Transacao(id, usuarioId, valor, descricao, categoria, data, TipoTransacao.DESPESA);
        Transacao transacao2 = new Transacao(id, usuarioId, valor, descricao, categoria, data, TipoTransacao.DESPESA);
        Transacao transacao3 = Transacao.criar(usuarioId, valor, descricao, categoria, data, TipoTransacao.DESPESA);
        
        assertEquals(transacao1, transacao2);
        assertNotEquals(transacao1, transacao3);
        assertEquals(transacao1.hashCode(), transacao2.hashCode());
    }
    
    @Test
    void deveRejeitarParametrosNulos() {
        assertThrows(NullPointerException.class, 
            () -> Transacao.criar(null, valor, descricao, categoria, data, TipoTransacao.DESPESA));
        assertThrows(NullPointerException.class, 
            () -> Transacao.criar(usuarioId, null, descricao, categoria, data, TipoTransacao.DESPESA));
        assertThrows(NullPointerException.class, 
            () -> Transacao.criar(usuarioId, valor, null, categoria, data, TipoTransacao.DESPESA));
        assertThrows(NullPointerException.class, 
            () -> Transacao.criar(usuarioId, valor, descricao, null, data, TipoTransacao.DESPESA));
        assertThrows(NullPointerException.class, 
            () -> Transacao.criar(usuarioId, valor, descricao, categoria, null, TipoTransacao.DESPESA));
        assertThrows(NullPointerException.class, 
            () -> Transacao.criar(usuarioId, valor, descricao, categoria, data, null));
    }
}