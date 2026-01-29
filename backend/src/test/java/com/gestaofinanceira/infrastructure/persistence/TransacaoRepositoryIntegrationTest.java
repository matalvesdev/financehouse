package com.gestaofinanceira.infrastructure.persistence;

import com.gestaofinanceira.application.ports.repository.TransacaoRepository;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.domain.entities.Transacao;
import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para TransacaoRepository usando H2 in-memory database.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransacaoRepositoryIntegrationTest {
    
    @Autowired
    private TransacaoRepository transacaoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    private Usuario usuario;
    
    @BeforeEach
    void setUp() {
        usuario = Usuario.criar(
            new Email("teste@exemplo.com"),
            SenhaHash.criarDeSenhaTexto("ValidPass123!"),
            new Nome("João Silva")
        );
        usuario = usuarioRepository.salvar(usuario);
    }
    
    @Test
    void deveSalvarTransacaoComSucesso() {
        // Arrange
        Transacao transacao = Transacao.criar(
            usuario.getId(),
            Valor.reais(new BigDecimal("100.50")),
            new Descricao("Salário mensal"),
            new Categoria("SALARIO", TipoCategoria.RECEITA),
            LocalDate.now(),
            TipoTransacao.RECEITA
        );
        
        // Act
        Transacao transacaoSalva = transacaoRepository.salvar(transacao);
        
        // Assert
        assertThat(transacaoSalva).isNotNull();
        assertThat(transacaoSalva.getId()).isNotNull();
        assertThat(transacaoSalva.getValor().quantia()).isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(transacaoSalva.getDescricao().valor()).isEqualTo("Salário mensal");
        assertThat(transacaoSalva.getTipo()).isEqualTo(TipoTransacao.RECEITA);
        assertThat(transacaoSalva.isAtiva()).isTrue();
    }
    
    @Test
    void deveBuscarTransacaoPorId() {
        // Arrange
        Transacao transacao = Transacao.criar(
            usuario.getId(),
            Valor.reais(new BigDecimal("50.00")),
            new Descricao("Almoço"),
            new Categoria("ALIMENTACAO", TipoCategoria.DESPESA),
            LocalDate.now(),
            TipoTransacao.DESPESA
        );
        Transacao transacaoSalva = transacaoRepository.salvar(transacao);
        
        // Act
        Optional<Transacao> transacaoEncontrada = transacaoRepository.buscarPorId(transacaoSalva.getId());
        
        // Assert
        assertThat(transacaoEncontrada).isPresent();
        assertThat(transacaoEncontrada.get().getId()).isEqualTo(transacaoSalva.getId());
        assertThat(transacaoEncontrada.get().getDescricao().valor()).isEqualTo("Almoço");
    }
    
    @Test
    void deveBuscarTransacoesPorUsuarioEPeriodo() {
        // Arrange
        LocalDate hoje = LocalDate.now();
        LocalDate ontem = hoje.minusDays(1);
        LocalDate amanha = hoje.plusDays(1);
        
        // Transação dentro do período
        Transacao transacao1 = Transacao.criar(
            usuario.getId(),
            Valor.reais(new BigDecimal("100.00")),
            new Descricao("Transação hoje"),
            new Categoria("OUTROS", TipoCategoria.RECEITA),
            hoje,
            TipoTransacao.RECEITA
        );
        
        // Transação fora do período
        Transacao transacao2 = Transacao.criar(
            usuario.getId(),
            Valor.reais(new BigDecimal("50.00")),
            new Descricao("Transação semana passada"),
            new Categoria("OUTROS", TipoCategoria.DESPESA),
            hoje.minusDays(7),
            TipoTransacao.DESPESA
        );
        
        transacaoRepository.salvar(transacao1);
        transacaoRepository.salvar(transacao2);
        
        // Act
        List<Transacao> transacoes = transacaoRepository.buscarPorUsuarioEPeriodo(
            usuario.getId(), ontem, amanha
        );
        
        // Assert
        assertThat(transacoes).hasSize(1);
        assertThat(transacoes.get(0).getDescricao().valor()).isEqualTo("Transação hoje");
    }
    
    @Test
    void deveCalcularSaldoAtual() {
        // Arrange
        Transacao receita = Transacao.criar(
            usuario.getId(),
            Valor.reais(new BigDecimal("1000.00")),
            new Descricao("Salário"),
            new Categoria("SALARIO", TipoCategoria.RECEITA),
            LocalDate.now(),
            TipoTransacao.RECEITA
        );
        
        Transacao despesa = Transacao.criar(
            usuario.getId(),
            Valor.reais(new BigDecimal("300.00")),
            new Descricao("Aluguel"),
            new Categoria("MORADIA", TipoCategoria.DESPESA),
            LocalDate.now(),
            TipoTransacao.DESPESA
        );
        
        transacaoRepository.salvar(receita);
        transacaoRepository.salvar(despesa);
        
        // Act
        BigDecimal saldo = transacaoRepository.calcularSaldoAtual(usuario.getId());
        
        // Assert
        assertThat(saldo).isEqualByComparingTo(new BigDecimal("700.00"));
    }
    
    @Test
    void deveCalcularReceitasPeriodo() {
        // Arrange
        LocalDate hoje = LocalDate.now();
        
        Transacao receita1 = Transacao.criar(
            usuario.getId(),
            Valor.reais(new BigDecimal("1000.00")),
            new Descricao("Salário"),
            new Categoria("SALARIO", TipoCategoria.RECEITA),
            hoje,
            TipoTransacao.RECEITA
        );
        
        Transacao receita2 = Transacao.criar(
            usuario.getId(),
            Valor.reais(new BigDecimal("500.00")),
            new Descricao("Freelance"),
            new Categoria("FREELANCE", TipoCategoria.RECEITA),
            hoje,
            TipoTransacao.RECEITA
        );
        
        Transacao despesa = Transacao.criar(
            usuario.getId(),
            Valor.reais(new BigDecimal("200.00")),
            new Descricao("Compras"),
            new Categoria("ALIMENTACAO", TipoCategoria.DESPESA),
            hoje,
            TipoTransacao.DESPESA
        );
        
        transacaoRepository.salvar(receita1);
        transacaoRepository.salvar(receita2);
        transacaoRepository.salvar(despesa);
        
        // Act
        BigDecimal receitas = transacaoRepository.calcularReceitasPeriodo(
            usuario.getId(), hoje, hoje
        );
        
        // Assert
        assertThat(receitas).isEqualByComparingTo(new BigDecimal("1500.00"));
    }
    
    @Test
    void deveCalcularDespesasPeriodo() {
        // Arrange
        LocalDate hoje = LocalDate.now();
        
        Transacao despesa1 = Transacao.criar(
            usuario.getId(),
            Valor.reais(new BigDecimal("300.00")),
            new Descricao("Aluguel"),
            new Categoria("MORADIA", TipoCategoria.DESPESA),
            hoje,
            TipoTransacao.DESPESA
        );
        
        Transacao despesa2 = Transacao.criar(
            usuario.getId(),
            Valor.reais(new BigDecimal("150.00")),
            new Descricao("Supermercado"),
            new Categoria("ALIMENTACAO", TipoCategoria.DESPESA),
            hoje,
            TipoTransacao.DESPESA
        );
        
        Transacao receita = Transacao.criar(
            usuario.getId(),
            Valor.reais(new BigDecimal("1000.00")),
            new Descricao("Salário"),
            new Categoria("SALARIO", TipoCategoria.RECEITA),
            hoje,
            TipoTransacao.RECEITA
        );
        
        transacaoRepository.salvar(despesa1);
        transacaoRepository.salvar(despesa2);
        transacaoRepository.salvar(receita);
        
        // Act
        BigDecimal despesas = transacaoRepository.calcularDespesasPeriodo(
            usuario.getId(), hoje, hoje
        );
        
        // Assert
        assertThat(despesas).isEqualByComparingTo(new BigDecimal("450.00"));
    }
    
    @Test
    void deveBuscarTransacoesRecentes() {
        // Arrange
        for (int i = 1; i <= 5; i++) {
            Transacao transacao = Transacao.criar(
                usuario.getId(),
                Valor.reais(new BigDecimal(i * 10)),
                new Descricao("Transação " + i),
                new Categoria("OUTROS", TipoCategoria.RECEITA),
                LocalDate.now().minusDays(i),
                TipoTransacao.RECEITA
            );
            transacaoRepository.salvar(transacao);
        }
        
        // Act
        List<Transacao> transacoesRecentes = transacaoRepository.buscarRecentes(usuario.getId(), 3);
        
        // Assert
        assertThat(transacoesRecentes).hasSize(3);
        // Deve estar ordenado por data de criação (mais recente primeiro)
    }
    
    @Test
    void deveAtualizarTransacao() {
        // Arrange
        Transacao transacao = Transacao.criar(
            usuario.getId(),
            Valor.reais(new BigDecimal("100.00")),
            new Descricao("Descrição original"),
            new Categoria("OUTROS", TipoCategoria.RECEITA),
            LocalDate.now(),
            TipoTransacao.RECEITA
        );
        Transacao transacaoSalva = transacaoRepository.salvar(transacao);
        
        // Act
        transacaoSalva.atualizarDescricao(new Descricao("Descrição atualizada"));
        transacaoSalva.atualizarValor(Valor.reais(new BigDecimal("150.00")));
        Transacao transacaoAtualizada = transacaoRepository.atualizar(transacaoSalva);
        
        // Assert
        assertThat(transacaoAtualizada.getDescricao().valor()).isEqualTo("Descrição atualizada");
        assertThat(transacaoAtualizada.getValor().quantia()).isEqualByComparingTo(new BigDecimal("150.00"));
    }
    
    @Test
    void deveRemoverTransacao() {
        // Arrange
        Transacao transacao = Transacao.criar(
            usuario.getId(),
            Valor.reais(new BigDecimal("100.00")),
            new Descricao("Transação para remover"),
            new Categoria("OUTROS", TipoCategoria.RECEITA),
            LocalDate.now(),
            TipoTransacao.RECEITA
        );
        Transacao transacaoSalva = transacaoRepository.salvar(transacao);
        
        // Act
        transacaoRepository.remover(transacaoSalva.getId());
        
        // Assert
        Optional<Transacao> transacaoRemovida = transacaoRepository.buscarPorId(transacaoSalva.getId());
        assertThat(transacaoRemovida).isPresent();
        assertThat(transacaoRemovida.get().isAtiva()).isFalse(); // Soft delete
    }
}

