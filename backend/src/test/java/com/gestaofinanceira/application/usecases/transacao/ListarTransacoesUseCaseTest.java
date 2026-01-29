package com.gestaofinanceira.application.usecases.transacao;

import com.gestaofinanceira.application.dto.response.TransacaoResponse;
import com.gestaofinanceira.application.ports.repository.TransacaoRepository;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListarTransacoesUseCaseTest {
    
    @Mock
    private TransacaoRepository transacaoRepository;
    
    @Mock
    private UsuarioRepository usuarioRepository;
    
    private ListarTransacoesUseCase useCase;
    
    private UsuarioId usuarioId;
    private Usuario usuario;
    private Transacao transacao1;
    private Transacao transacao2;
    
    @BeforeEach
    void setUp() {
        useCase = new ListarTransacoesUseCase(transacaoRepository, usuarioRepository);
        
        usuarioId = UsuarioId.gerar();
        usuario = Usuario.criar(
            new Email("teste@exemplo.com"),
            SenhaHash.criarDeSenhaTexto("MinhaSenh@123"),
            new Nome("Usuário Teste")
        );
        
        // Criar transações de teste
        transacao1 = new Transacao(
            TransacaoId.gerar(),
            usuarioId,
            Valor.reais(new BigDecimal("100.00")),
            new Descricao("Transação 1"),
            Categoria.ALIMENTACAO,
            LocalDate.now().minusDays(1),
            TipoTransacao.DESPESA,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().minusDays(1),
            true
        );
        
        transacao2 = new Transacao(
            TransacaoId.gerar(),
            usuarioId,
            Valor.reais(new BigDecimal("200.00")),
            new Descricao("Transação 2"),
            Categoria.SALARIO,
            LocalDate.now(),
            TipoTransacao.RECEITA,
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );
    }
    
    @Test
    void deveListarTransacoesPorPeriodo() {
        // Arrange
        LocalDate dataInicio = LocalDate.now().minusDays(7);
        LocalDate dataFim = LocalDate.now();
        List<Transacao> transacoes = List.of(transacao1, transacao2);
        
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(transacaoRepository.buscarPorUsuarioEPeriodo(usuarioId, dataInicio, dataFim))
            .thenReturn(transacoes);
        
        // Act
        List<TransacaoResponse> resultado = useCase.listarPorPeriodo(usuarioId, dataInicio, dataFim);
        
        // Assert
        assertThat(resultado).hasSize(2);
        
        // Verifica ordenação cronológica (mais recentes primeiro)
        assertThat(resultado.get(0).descricao()).isEqualTo("Transação 2");
        assertThat(resultado.get(1).descricao()).isEqualTo("Transação 1");
        
        verify(usuarioRepository).buscarPorId(usuarioId);
        verify(transacaoRepository).buscarPorUsuarioEPeriodo(usuarioId, dataInicio, dataFim);
    }
    
    @Test
    void deveListarTransacoesPorCategoria() {
        // Arrange
        String categoria = "ALIMENTACAO";
        List<Transacao> transacoes = List.of(transacao1);
        
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(transacaoRepository.buscarPorUsuarioECategoria(usuarioId, categoria))
            .thenReturn(transacoes);
        
        // Act
        List<TransacaoResponse> resultado = useCase.listarPorCategoria(usuarioId, categoria);
        
        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).categoria()).isEqualTo("ALIMENTACAO");
        assertThat(resultado.get(0).valor()).isEqualTo(new BigDecimal("100.00"));
        
        verify(transacaoRepository).buscarPorUsuarioECategoria(usuarioId, "ALIMENTACAO");
    }
    
    @Test
    void deveListarTransacoesPorCategoriaEPeriodo() {
        // Arrange
        String categoria = "ALIMENTACAO";
        LocalDate dataInicio = LocalDate.now().minusDays(7);
        LocalDate dataFim = LocalDate.now();
        List<Transacao> transacoes = List.of(transacao1);
        
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(transacaoRepository.buscarPorUsuarioCategoriaEPeriodo(usuarioId, "ALIMENTACAO", dataInicio, dataFim))
            .thenReturn(transacoes);
        
        // Act
        List<TransacaoResponse> resultado = useCase.listarPorCategoriaEPeriodo(
            usuarioId, categoria, dataInicio, dataFim);
        
        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).categoria()).isEqualTo("ALIMENTACAO");
        
        verify(transacaoRepository).buscarPorUsuarioCategoriaEPeriodo(
            usuarioId, "ALIMENTACAO", dataInicio, dataFim);
    }
    
    @Test
    void deveListarTransacoesRecentes() {
        // Arrange
        int limite = 5;
        List<Transacao> transacoes = List.of(transacao2, transacao1);
        
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(transacaoRepository.buscarRecentes(usuarioId, limite))
            .thenReturn(transacoes);
        
        // Act
        List<TransacaoResponse> resultado = useCase.listarRecentes(usuarioId, limite);
        
        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).descricao()).isEqualTo("Transação 2");
        assertThat(resultado.get(1).descricao()).isEqualTo("Transação 1");
        
        verify(transacaoRepository).buscarRecentes(usuarioId, limite);
    }
    
    @Test
    void deveListarTransacoesRecentesComLimitePadrao() {
        // Arrange
        List<Transacao> transacoes = List.of(transacao2, transacao1);
        
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(transacaoRepository.buscarRecentes(usuarioId, 10))
            .thenReturn(transacoes);
        
        // Act
        List<TransacaoResponse> resultado = useCase.listarRecentes(usuarioId);
        
        // Assert
        assertThat(resultado).hasSize(2);
        verify(transacaoRepository).buscarRecentes(usuarioId, 10);
    }
    
    @Test
    void deveListarTransacoesPorTipo() {
        // Arrange
        LocalDate dataInicio = LocalDate.now().minusDays(7);
        LocalDate dataFim = LocalDate.now();
        List<Transacao> transacoes = List.of(transacao1, transacao2);
        
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(transacaoRepository.buscarPorUsuarioEPeriodo(usuarioId, dataInicio, dataFim))
            .thenReturn(transacoes);
        
        // Act
        List<TransacaoResponse> resultado = useCase.listarPorTipo(
            usuarioId, TipoTransacao.RECEITA, dataInicio, dataFim);
        
        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).tipo()).isEqualTo(TipoTransacao.RECEITA);
        assertThat(resultado.get(0).descricao()).isEqualTo("Transação 2");
    }
    
    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
        // Arrange
        LocalDate dataInicio = LocalDate.now().minusDays(7);
        LocalDate dataFim = LocalDate.now();
        
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.listarPorPeriodo(usuarioId, dataInicio, dataFim))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Usuário não encontrado");
    }
    
    @Test
    void deveLancarExcecaoQuandoDataInicioMaiorQueDataFim() {
        // Arrange
        LocalDate dataInicio = LocalDate.now();
        LocalDate dataFim = LocalDate.now().minusDays(1);
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.listarPorPeriodo(usuarioId, dataInicio, dataFim))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Data de início deve ser anterior à data de fim");
    }
    
    @Test
    void deveLancarExcecaoQuandoCategoriaVazia() {
        // Act & Assert
        assertThatThrownBy(() -> useCase.listarPorCategoria(usuarioId, ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Categoria não pode estar vazia");
    }
    
    @Test
    void deveLancarExcecaoQuandoLimiteInvalido() {
        // Act & Assert
        assertThatThrownBy(() -> useCase.listarRecentes(usuarioId, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Limite deve ser maior que zero");
        
        assertThatThrownBy(() -> useCase.listarRecentes(usuarioId, 101))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Limite não pode ser maior que 100");
    }
    
    @Test
    void deveConverterTransacaoParaResponseCorretamente() {
        // Arrange
        List<Transacao> transacoes = List.of(transacao1);
        
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(transacaoRepository.buscarRecentes(usuarioId, 10))
            .thenReturn(transacoes);
        
        // Act
        List<TransacaoResponse> resultado = useCase.listarRecentes(usuarioId);
        
        // Assert
        TransacaoResponse response = resultado.get(0);
        assertThat(response.id()).isEqualTo(transacao1.getId().valor());
        assertThat(response.valor()).isEqualTo(transacao1.getValor().quantia());
        assertThat(response.moeda()).isEqualTo(transacao1.getValor().moeda().getCodigo());
        assertThat(response.descricao()).isEqualTo(transacao1.getDescricao().valor());
        assertThat(response.categoria()).isEqualTo(transacao1.getCategoria().nome());
        assertThat(response.tipo()).isEqualTo(transacao1.getTipo());
        assertThat(response.data()).isEqualTo(transacao1.getData());
        assertThat(response.criadoEm()).isEqualTo(transacao1.getCriadoEm());
        assertThat(response.ativa()).isEqualTo(transacao1.isAtiva());
    }
}