package com.gestaofinanceira.application.usecases.transacao;

import com.gestaofinanceira.application.dto.command.ComandoCriarTransacao;
import com.gestaofinanceira.application.ports.repository.MetaFinanceiraRepository;
import com.gestaofinanceira.application.ports.repository.OrcamentoRepository;
import com.gestaofinanceira.application.ports.repository.TransacaoRepository;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.application.ports.service.NotificacaoPort;
import com.gestaofinanceira.domain.entities.MetaFinanceira;
import com.gestaofinanceira.domain.entities.Orcamento;
import com.gestaofinanceira.domain.entities.Transacao;
import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CriarTransacaoUseCaseTest {
    
    @Mock
    private TransacaoRepository transacaoRepository;
    
    @Mock
    private UsuarioRepository usuarioRepository;
    
    @Mock
    private OrcamentoRepository orcamentoRepository;
    
    @Mock
    private MetaFinanceiraRepository metaFinanceiraRepository;
    
    @Mock
    private NotificacaoPort notificacaoPort;
    
    private CriarTransacaoUseCase useCase;
    
    private UsuarioId usuarioId;
    private Usuario usuario;
    
    @BeforeEach
    void setUp() {
        useCase = new CriarTransacaoUseCase(
            transacaoRepository,
            usuarioRepository,
            orcamentoRepository,
            metaFinanceiraRepository,
            notificacaoPort
        );
        
        usuarioId = UsuarioId.gerar();
        usuario = Usuario.criar(
            new Email("teste@exemplo.com"),
            SenhaHash.criarDeSenhaTexto("MinhaSenh@123"),
            new Nome("Usuário Teste")
        );
    }
    
    @Test
    void deveExecutarCriacaoDeTransacaoComSucesso() {
        // Arrange
        ComandoCriarTransacao comando = new ComandoCriarTransacao(
            usuarioId,
            new BigDecimal("100.00"),
            "Compra no supermercado",
            "ALIMENTACAO",
            TipoTransacao.DESPESA,
            LocalDate.now()
        );
        
        Transacao transacaoEsperada = Transacao.criar(
            usuarioId,
            Valor.reais(new BigDecimal("100.00")),
            new Descricao("Compra no supermercado"),
            Categoria.ALIMENTACAO,
            LocalDate.now(),
            TipoTransacao.DESPESA
        );
        
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(transacaoRepository.salvar(any(Transacao.class))).thenReturn(transacaoEsperada);
        when(orcamentoRepository.buscarAtivoPorUsuarioECategoria(usuarioId, "ALIMENTACAO"))
            .thenReturn(Optional.empty());
        
        // Act
        Transacao resultado = useCase.executar(comando);
        
        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getValor().quantia()).isEqualTo(new BigDecimal("100.00"));
        assertThat(resultado.getDescricao().valor()).isEqualTo("Compra no supermercado");
        assertThat(resultado.getCategoria()).isEqualTo(Categoria.ALIMENTACAO);
        assertThat(resultado.getTipo()).isEqualTo(TipoTransacao.DESPESA);
        
        verify(transacaoRepository).salvar(any(Transacao.class));
        verify(usuarioRepository).buscarPorId(usuarioId);
    }
    
    @Test
    void deveAtualizarOrcamentoQuandoTransacaoAfetaOrcamento() {
        // Arrange
        ComandoCriarTransacao comando = new ComandoCriarTransacao(
            usuarioId,
            new BigDecimal("50.00"),
            "Almoço",
            "ALIMENTACAO",
            TipoTransacao.DESPESA,
            LocalDate.now()
        );
        
        Orcamento orcamento = Orcamento.criar(
            usuarioId,
            Categoria.ALIMENTACAO,
            Valor.reais(new BigDecimal("500.00")),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(transacaoRepository.salvar(any(Transacao.class))).thenReturn(any(Transacao.class));
        when(orcamentoRepository.buscarAtivoPorUsuarioECategoria(usuarioId, "ALIMENTACAO"))
            .thenReturn(Optional.of(orcamento));
        
        // Act
        useCase.executar(comando);
        
        // Assert
        verify(orcamentoRepository).atualizar(orcamento);
        assertThat(orcamento.getGastoAtual().quantia()).isEqualTo(new BigDecimal("50.00"));
    }
    
    @Test
    void deveNotificarQuandoOrcamentoProximoDoLimite() {
        // Arrange
        ComandoCriarTransacao comando = new ComandoCriarTransacao(
            usuarioId,
            new BigDecimal("400.00"),
            "Compra grande",
            "ALIMENTACAO",
            TipoTransacao.DESPESA,
            LocalDate.now()
        );
        
        Orcamento orcamento = Orcamento.criar(
            usuarioId,
            Categoria.ALIMENTACAO,
            Valor.reais(new BigDecimal("500.00")),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(transacaoRepository.salvar(any(Transacao.class))).thenReturn(any(Transacao.class));
        when(orcamentoRepository.buscarAtivoPorUsuarioECategoria(usuarioId, "ALIMENTACAO"))
            .thenReturn(Optional.of(orcamento));
        
        // Act
        useCase.executar(comando);
        
        // Assert
        verify(notificacaoPort).notificarOrcamentoProximoLimite(orcamento);
    }
    
    @Test
    void deveAtualizarMetasQuandoReceitaDeInvestimento() {
        // Arrange
        ComandoCriarTransacao comando = new ComandoCriarTransacao(
            usuarioId,
            new BigDecimal("200.00"),
            "Dividendos recebidos",
            "INVESTIMENTOS",
            TipoTransacao.RECEITA,
            LocalDate.now()
        );
        
        MetaFinanceira meta = MetaFinanceira.criar(
            usuarioId,
            new Nome("Reserva de Emergência"),
            Valor.reais(new BigDecimal("10000.00")),
            LocalDate.now().plusMonths(12),
            TipoMeta.RESERVA_EMERGENCIA
        );
        
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(transacaoRepository.salvar(any(Transacao.class))).thenReturn(any(Transacao.class));
        when(metaFinanceiraRepository.buscarAtivasPorUsuario(usuarioId))
            .thenReturn(List.of(meta));
        
        // Act
        useCase.executar(comando);
        
        // Assert
        verify(metaFinanceiraRepository).atualizar(meta);
        assertThat(meta.getValorAtual().quantia()).isEqualTo(new BigDecimal("200.00"));
    }
    
    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
        // Arrange
        ComandoCriarTransacao comando = new ComandoCriarTransacao(
            usuarioId,
            new BigDecimal("100.00"),
            "Teste",
            "ALIMENTACAO",
            TipoTransacao.DESPESA,
            LocalDate.now()
        );
        
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(comando))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Usuário não encontrado ou inativo");
    }
    
    @Test
    void deveLancarExcecaoQuandoComandoNulo() {
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Comando não pode ser nulo");
    }
    
    @Test
    void deveUsarCategoriaPersonalizadaQuandoNaoPredefinida() {
        // Arrange
        ComandoCriarTransacao comando = new ComandoCriarTransacao(
            usuarioId,
            new BigDecimal("75.00"),
            "Categoria personalizada",
            "CATEGORIA_CUSTOM",
            TipoTransacao.DESPESA,
            LocalDate.now()
        );
        
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(transacaoRepository.salvar(any(Transacao.class))).thenReturn(any(Transacao.class));
        when(orcamentoRepository.buscarAtivoPorUsuarioECategoria(usuarioId, "CATEGORIA_CUSTOM"))
            .thenReturn(Optional.empty());
        
        // Act
        Transacao resultado = useCase.executar(comando);
        
        // Assert
        assertThat(resultado.getCategoria().nome()).isEqualTo("CATEGORIA_CUSTOM");
        assertThat(resultado.getCategoria().ehPersonalizada()).isTrue();
    }
}