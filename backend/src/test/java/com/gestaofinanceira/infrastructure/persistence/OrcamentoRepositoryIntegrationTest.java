package com.gestaofinanceira.infrastructure.persistence;

import com.gestaofinanceira.application.ports.repository.OrcamentoRepository;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.domain.entities.Orcamento;
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
 * Testes de integração para OrcamentoRepository usando H2 in-memory database.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrcamentoRepositoryIntegrationTest {
    
    @Autowired
    private OrcamentoRepository orcamentoRepository;
    
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
    void deveSalvarOrcamentoComSucesso() {
        // Arrange
        Orcamento orcamento = Orcamento.criar(
            usuario.getId(),
            new Categoria("ALIMENTACAO", TipoCategoria.DESPESA),
            Valor.reais(new BigDecimal("500.00")),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        
        // Act
        Orcamento orcamentoSalvo = orcamentoRepository.salvar(orcamento);
        
        // Assert
        assertThat(orcamentoSalvo).isNotNull();
        assertThat(orcamentoSalvo.getId()).isNotNull();
        assertThat(orcamentoSalvo.getCategoria().nome()).isEqualTo("ALIMENTACAO");
        assertThat(orcamentoSalvo.getLimite().quantia()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(orcamentoSalvo.getStatus()).isEqualTo(StatusOrcamento.ATIVO);
        assertThat(orcamentoSalvo.getGastoAtual().quantia()).isEqualByComparingTo(BigDecimal.ZERO);
    }
    
    @Test
    void deveBuscarOrcamentoPorId() {
        // Arrange
        Orcamento orcamento = Orcamento.criar(
            usuario.getId(),
            new Categoria("TRANSPORTE", TipoCategoria.DESPESA),
            Valor.reais(new BigDecimal("300.00")),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        Orcamento orcamentoSalvo = orcamentoRepository.salvar(orcamento);
        
        // Act
        Optional<Orcamento> orcamentoEncontrado = orcamentoRepository.buscarPorId(orcamentoSalvo.getId());
        
        // Assert
        assertThat(orcamentoEncontrado).isPresent();
        assertThat(orcamentoEncontrado.get().getId()).isEqualTo(orcamentoSalvo.getId());
        assertThat(orcamentoEncontrado.get().getCategoria().nome()).isEqualTo("TRANSPORTE");
    }
    
    @Test
    void deveBuscarOrcamentosAtivosPorUsuario() {
        // Arrange
        Orcamento orcamento1 = Orcamento.criar(
            usuario.getId(),
            new Categoria("ALIMENTACAO", TipoCategoria.DESPESA),
            Valor.reais(new BigDecimal("500.00")),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        
        Orcamento orcamento2 = Orcamento.criar(
            usuario.getId(),
            new Categoria("TRANSPORTE", TipoCategoria.DESPESA),
            Valor.reais(new BigDecimal("300.00")),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        
        orcamentoRepository.salvar(orcamento1);
        orcamentoRepository.salvar(orcamento2);
        
        // Act
        List<Orcamento> orcamentosAtivos = orcamentoRepository.buscarAtivosPorUsuario(usuario.getId());
        
        // Assert
        assertThat(orcamentosAtivos).hasSize(2);
        assertThat(orcamentosAtivos)
            .extracting(o -> o.getCategoria().nome())
            .containsExactlyInAnyOrder("ALIMENTACAO", "TRANSPORTE");
    }
    
    @Test
    void deveBuscarOrcamentoAtivoPorUsuarioECategoria() {
        // Arrange
        Orcamento orcamento = Orcamento.criar(
            usuario.getId(),
            new Categoria("LAZER", TipoCategoria.DESPESA),
            Valor.reais(new BigDecimal("200.00")),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        orcamentoRepository.salvar(orcamento);
        
        // Act
        Optional<Orcamento> orcamentoEncontrado = orcamentoRepository
            .buscarAtivoPorUsuarioECategoria(usuario.getId(), "LAZER");
        
        // Assert
        assertThat(orcamentoEncontrado).isPresent();
        assertThat(orcamentoEncontrado.get().getCategoria().nome()).isEqualTo("LAZER");
        assertThat(orcamentoEncontrado.get().getStatus()).isEqualTo(StatusOrcamento.ATIVO);
    }
    
    @Test
    void deveAtualizarOrcamento() {
        // Arrange
        Orcamento orcamento = Orcamento.criar(
            usuario.getId(),
            new Categoria("SAUDE", TipoCategoria.DESPESA),
            Valor.reais(new BigDecimal("400.00")),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        Orcamento orcamentoSalvo = orcamentoRepository.salvar(orcamento);
        
        // Act
        orcamentoSalvo.atualizarLimite(Valor.reais(new BigDecimal("600.00")));
        orcamentoSalvo.adicionarGasto(Valor.reais(new BigDecimal("100.00")));
        Orcamento orcamentoAtualizado = orcamentoRepository.atualizar(orcamentoSalvo);
        
        // Assert
        assertThat(orcamentoAtualizado.getLimite().quantia()).isEqualByComparingTo(new BigDecimal("600.00"));
        assertThat(orcamentoAtualizado.getGastoAtual().quantia()).isEqualByComparingTo(new BigDecimal("100.00"));
    }
    
    @Test
    void deveBuscarOrcamentosPorStatus() {
        // Arrange
        Orcamento orcamento1 = Orcamento.criar(
            usuario.getId(),
            new Categoria("CATEGORIA1", TipoCategoria.DESPESA),
            Valor.reais(new BigDecimal("100.00")),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        
        Orcamento orcamento2 = Orcamento.criar(
            usuario.getId(),
            new Categoria("CATEGORIA2", TipoCategoria.DESPESA),
            Valor.reais(new BigDecimal("200.00")),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        
        orcamentoRepository.salvar(orcamento1);
        
        // Adiciona gasto que excede o limite
        orcamento2.adicionarGasto(Valor.reais(new BigDecimal("250.00")));
        orcamentoRepository.salvar(orcamento2);
        
        // Act
        List<Orcamento> orcamentosAtivos = orcamentoRepository
            .buscarPorUsuarioEStatus(usuario.getId(), StatusOrcamento.ATIVO);
        List<Orcamento> orcamentosExcedidos = orcamentoRepository
            .buscarPorUsuarioEStatus(usuario.getId(), StatusOrcamento.EXCEDIDO);
        
        // Assert
        assertThat(orcamentosAtivos).hasSize(1);
        assertThat(orcamentosExcedidos).hasSize(1);
        assertThat(orcamentosExcedidos.get(0).getCategoria().nome()).isEqualTo("CATEGORIA2");
    }
    
    @Test
    void deveArquivarOrcamentosVencidos() {
        // Arrange
        LocalDate dataPassada = LocalDate.now().minusMonths(2);
        
        Orcamento orcamentoVencido = Orcamento.criar(
            usuario.getId(),
            new Categoria("VENCIDO", TipoCategoria.DESPESA),
            Valor.reais(new BigDecimal("300.00")),
            PeriodoOrcamento.MENSAL,
            dataPassada
        );
        orcamentoRepository.salvar(orcamentoVencido);
        
        // Act
        int orcamentosArquivados = orcamentoRepository.arquivarVencidos(LocalDate.now());
        
        // Assert
        assertThat(orcamentosArquivados).isGreaterThan(0);
        
        Optional<Orcamento> orcamentoArquivado = orcamentoRepository.buscarPorId(orcamentoVencido.getId());
        assertThat(orcamentoArquivado).isPresent();
        assertThat(orcamentoArquivado.get().getStatus()).isEqualTo(StatusOrcamento.ARQUIVADO);
    }
    
    @Test
    void deveRemoverOrcamento() {
        // Arrange
        Orcamento orcamento = Orcamento.criar(
            usuario.getId(),
            new Categoria("REMOVER", TipoCategoria.DESPESA),
            Valor.reais(new BigDecimal("100.00")),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        Orcamento orcamentoSalvo = orcamentoRepository.salvar(orcamento);
        
        // Act
        orcamentoRepository.remover(orcamentoSalvo.getId());
        
        // Assert
        Optional<Orcamento> orcamentoRemovido = orcamentoRepository.buscarPorId(orcamentoSalvo.getId());
        assertThat(orcamentoRemovido).isEmpty(); // Hard delete para orçamentos
    }
}

