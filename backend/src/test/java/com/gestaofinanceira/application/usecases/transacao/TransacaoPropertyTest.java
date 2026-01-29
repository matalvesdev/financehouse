package com.gestaofinanceira.application.usecases.transacao;

import com.gestaofinanceira.application.dto.command.ComandoAtualizarTransacao;
import com.gestaofinanceira.application.dto.command.ComandoCriarTransacao;
import com.gestaofinanceira.application.ports.repository.MetaFinanceiraRepository;
import com.gestaofinanceira.application.ports.repository.OrcamentoRepository;
import com.gestaofinanceira.application.ports.repository.TransacaoRepository;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.application.ports.service.NotificacaoPort;
import com.gestaofinanceira.domain.entities.Transacao;
import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.*;

import net.jqwik.api.*;
import net.jqwik.api.constraints.BigRange;
import net.jqwik.api.constraints.StringLength;
import net.jqwik.time.api.Dates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for transaction use cases.
 * Tests universal properties that should hold for all valid transaction scenarios.
 * 
 * **Validates: Requirements 3.1, 3.2**
 */
@Label("Feature: gestao-financeira-domestica, Transaction Properties")
@ExtendWith(MockitoExtension.class)
class TransacaoPropertyTest {

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
    
    private CriarTransacaoUseCase criarTransacaoUseCase;
    private AtualizarTransacaoUseCase atualizarTransacaoUseCase;

    @BeforeEach
    void setUp() {
        criarTransacaoUseCase = new CriarTransacaoUseCase(
            transacaoRepository,
            usuarioRepository,
            orcamentoRepository,
            metaFinanceiraRepository,
            notificacaoPort
        );
        
        atualizarTransacaoUseCase = new AtualizarTransacaoUseCase(
            transacaoRepository,
            usuarioRepository,
            orcamentoRepository,
            metaFinanceiraRepository,
            notificacaoPort
        );
    }

    /**
     * Property 8: Transaction creation consistency
     * For any valid transaction data, creating a transaction should store it with all provided 
     * information and generate appropriate system metadata (ID, timestamp)
     * **Validates: Requirements 3.1**
     */
    @Property(tries = 20)
    @Label("Property 8: Transaction creation consistency")
    void transactionCreationShouldStoreAllProvidedInformation(
            @ForAll("validComandoCriarTransacao") ComandoCriarTransacao comando,
            @ForAll("validUsuarios") Usuario usuario) {
        
        // Arrange - Setup valid user scenario
        when(usuarioRepository.buscarPorId(comando.usuarioId()))
            .thenReturn(Optional.of(usuario));
        
        when(orcamentoRepository.buscarAtivoPorUsuarioECategoria(any(), any()))
            .thenReturn(Optional.empty());
        
        when(metaFinanceiraRepository.buscarAtivasPorUsuario(any()))
            .thenReturn(List.of());
        
        // Create expected transaction with generated ID and timestamp
        Transacao transacaoEsperada = Transacao.criar(
            comando.usuarioId(),
            Valor.reais(comando.valor()),
            new Descricao(comando.descricao()),
            criarCategoriaParaTeste(comando.categoria(), comando.tipo()),
            comando.data(),
            comando.tipo()
        );
        
        when(transacaoRepository.salvar(any(Transacao.class)))
            .thenReturn(transacaoEsperada);
        
        // Act - Create transaction
        Transacao resultado = criarTransacaoUseCase.executar(comando);
        
        // Assert - Verify all provided information is preserved
        assertThat(resultado).isNotNull();
        assertThat(resultado.getUsuarioId()).isEqualTo(comando.usuarioId());
        assertThat(resultado.getValor().quantia()).isEqualTo(comando.valor());
        assertThat(resultado.getDescricao().valor()).isEqualTo(comando.descricao());
        assertThat(resultado.getCategoria().nome()).isEqualTo(comando.categoria().toUpperCase());
        assertThat(resultado.getTipo()).isEqualTo(comando.tipo());
        assertThat(resultado.getData()).isEqualTo(comando.data());
        
        // Verify system metadata is generated
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getCriadoEm()).isNotNull();
        assertThat(resultado.getAtualizadoEm()).isNotNull();
        assertThat(resultado.isAtiva()).isTrue();
        
        // Verify creation timestamp is recent (within last minute)
        assertThat(resultado.getCriadoEm())
            .isAfter(LocalDateTime.now().minusMinutes(1))
            .isBefore(LocalDateTime.now().plusSeconds(1));
        
        // Verify initial state: created and updated timestamps should be equal
        assertThat(resultado.getCriadoEm()).isEqualTo(resultado.getAtualizadoEm());
        
        // Verify repository interaction
        verify(transacaoRepository).salvar(argThat(transacao -> 
            transacao.getUsuarioId().equals(comando.usuarioId()) &&
            transacao.getValor().quantia().equals(comando.valor()) &&
            transacao.getDescricao().valor().equals(comando.descricao()) &&
            transacao.getTipo().equals(comando.tipo()) &&
            transacao.getData().equals(comando.data()) &&
            transacao.isAtiva()
        ));
        
        verify(usuarioRepository).buscarPorId(comando.usuarioId());
    }

    /**
     * Property 9: Transaction update audit preservation
     * For any transaction update operation, the system should preserve the original transaction 
     * data in audit logs while updating the current record
     * **Validates: Requirements 3.2**
     */
    @Property(tries = 20)
    @Label("Property 9: Transaction update audit preservation")
    void transactionUpdateShouldPreserveAuditTrail(
            @ForAll("validComandoAtualizarTransacao") ComandoAtualizarTransacao comando,
            @ForAll("validUsuarios") Usuario usuario,
            @ForAll("validTransacaoExistente") Transacao transacaoOriginal) {
        
        // Arrange - Setup existing transaction scenario
        when(usuarioRepository.buscarPorId(comando.usuarioId()))
            .thenReturn(Optional.of(usuario));
        
        // Ensure the original transaction belongs to the user
        Transacao transacaoComUsuarioCorreto = new Transacao(
            comando.transacaoId(),
            comando.usuarioId(), // Same user as in command
            transacaoOriginal.getValor(),
            transacaoOriginal.getDescricao(),
            transacaoOriginal.getCategoria(),
            transacaoOriginal.getData(),
            transacaoOriginal.getTipo(),
            transacaoOriginal.getCriadoEm(),
            transacaoOriginal.getAtualizadoEm(),
            true // Active
        );
        
        when(transacaoRepository.buscarPorId(comando.transacaoId()))
            .thenReturn(Optional.of(transacaoComUsuarioCorreto));
        
        when(orcamentoRepository.buscarAtivoPorUsuarioECategoria(any(), any()))
            .thenReturn(Optional.empty());
        
        when(metaFinanceiraRepository.buscarAtivasPorUsuario(any()))
            .thenReturn(List.of());
        
        // Capture original audit information
        LocalDateTime criadoEmOriginal = transacaoComUsuarioCorreto.getCriadoEm();
        LocalDateTime atualizadoEmOriginal = transacaoComUsuarioCorreto.getAtualizadoEm();
        
        // Mock the updated transaction returned by repository
        when(transacaoRepository.atualizar(any(Transacao.class)))
            .thenAnswer(invocation -> {
                Transacao transacaoAtualizada = invocation.getArgument(0);
                // Simulate that the repository preserves creation time but updates modification time
                return new Transacao(
                    transacaoAtualizada.getId(),
                    transacaoAtualizada.getUsuarioId(),
                    transacaoAtualizada.getValor(),
                    transacaoAtualizada.getDescricao(),
                    transacaoAtualizada.getCategoria(),
                    transacaoAtualizada.getData(),
                    transacaoAtualizada.getTipo(),
                    criadoEmOriginal, // Preserve original creation time
                    LocalDateTime.now(), // Update modification time
                    transacaoAtualizada.isAtiva()
                );
            });
        
        // Act - Update transaction
        Transacao resultado = atualizarTransacaoUseCase.executar(comando);
        
        // Assert - Verify audit trail preservation
        assertThat(resultado).isNotNull();
        
        // Verify updated data is applied
        assertThat(resultado.getValor().quantia()).isEqualTo(comando.valor());
        assertThat(resultado.getDescricao().valor()).isEqualTo(comando.descricao());
        assertThat(resultado.getCategoria().nome()).isEqualTo(comando.categoria().toUpperCase());
        
        // Verify audit trail preservation
        assertThat(resultado.getCriadoEm()).isEqualTo(criadoEmOriginal);
        assertThat(resultado.getAtualizadoEm()).isAfter(atualizadoEmOriginal);
        assertThat(resultado.getAtualizadoEm()).isAfter(criadoEmOriginal);
        
        // Verify immutable fields are preserved
        assertThat(resultado.getId()).isEqualTo(comando.transacaoId());
        assertThat(resultado.getUsuarioId()).isEqualTo(comando.usuarioId());
        assertThat(resultado.getData()).isEqualTo(comando.data());
        assertThat(resultado.getTipo()).isEqualTo(comando.tipo());
        
        // Verify the transaction was marked as modified
        assertThat(resultado.foiModificada()).isTrue();
        
        // Verify repository interactions
        verify(transacaoRepository).buscarPorId(comando.transacaoId());
        verify(transacaoRepository).atualizar(any(Transacao.class));
        verify(usuarioRepository).buscarPorId(comando.usuarioId());
    }

    /**
     * Property: Transaction creation should fail with inactive user
     * For any transaction creation attempt with an inactive user, the operation should fail
     */
    @Property(tries = 20)
    @Label("Property: Inactive user transaction creation rejection")
    void transactionCreationShouldFailWithInactiveUser(
            @ForAll("validComandoCriarTransacao") ComandoCriarTransacao comando,
            @ForAll("inactiveUsuarios") Usuario inactiveUsuario) {
        
        // Arrange - Setup inactive user scenario
        when(usuarioRepository.buscarPorId(comando.usuarioId()))
            .thenReturn(Optional.of(inactiveUsuario));
        
        // Act & Assert
        assertThatThrownBy(() -> criarTransacaoUseCase.executar(comando))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Usuário não encontrado ou inativo");
        
        // Verify no transaction was saved
        verify(transacaoRepository, never()).salvar(any());
    }

    /**
     * Property: Transaction update should fail with non-existent transaction
     * For any update attempt on a non-existent transaction, the operation should fail
     */
    @Property(tries = 20)
    @Label("Property: Non-existent transaction update rejection")
    void transactionUpdateShouldFailWithNonExistentTransaction(
            @ForAll("validComandoAtualizarTransacao") ComandoAtualizarTransacao comando,
            @ForAll("validUsuarios") Usuario usuario) {
        
        // Arrange - Setup non-existent transaction scenario
        when(usuarioRepository.buscarPorId(comando.usuarioId()))
            .thenReturn(Optional.of(usuario));
        
        when(transacaoRepository.buscarPorId(comando.transacaoId()))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> atualizarTransacaoUseCase.executar(comando))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Transação não encontrada");
        
        // Verify no update was attempted
        verify(transacaoRepository, never()).atualizar(any());
    }

    /**
     * Property: Transaction update should fail when transaction belongs to different user
     * For any update attempt on a transaction that belongs to a different user, the operation should fail
     */
    @Property(tries = 20)
    @Label("Property: Cross-user transaction update rejection")
    void transactionUpdateShouldFailWithDifferentUser(
            @ForAll("validComandoAtualizarTransacao") ComandoAtualizarTransacao comando,
            @ForAll("validUsuarios") Usuario usuario,
            @ForAll("validTransacaoExistente") Transacao transacaoDeOutroUsuario) {
        
        // Arrange - Setup transaction belonging to different user
        UsuarioId outroUsuarioId = UsuarioId.gerar(); // Different user
        
        Transacao transacaoComOutroUsuario = new Transacao(
            comando.transacaoId(),
            outroUsuarioId, // Different user
            transacaoDeOutroUsuario.getValor(),
            transacaoDeOutroUsuario.getDescricao(),
            transacaoDeOutroUsuario.getCategoria(),
            transacaoDeOutroUsuario.getData(),
            transacaoDeOutroUsuario.getTipo(),
            transacaoDeOutroUsuario.getCriadoEm(),
            transacaoDeOutroUsuario.getAtualizadoEm(),
            true
        );
        
        when(usuarioRepository.buscarPorId(comando.usuarioId()))
            .thenReturn(Optional.of(usuario));
        
        when(transacaoRepository.buscarPorId(comando.transacaoId()))
            .thenReturn(Optional.of(transacaoComOutroUsuario));
        
        // Act & Assert
        assertThatThrownBy(() -> atualizarTransacaoUseCase.executar(comando))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Transação não pertence ao usuário");
        
        // Verify no update was attempted
        verify(transacaoRepository, never()).atualizar(any());
    }

    // Generators for test data

    @Provide
    Arbitrary<ComandoCriarTransacao> validComandoCriarTransacao() {
        return Combinators.combine(
            validUsuarioIds(),
            validValores(),
            validDescricoes(),
            validNomesCategorias(),
            validTiposTransacao(),
            validDatasTransacao()
        ).as(ComandoCriarTransacao::new);
    }

    @Provide
    Arbitrary<ComandoAtualizarTransacao> validComandoAtualizarTransacao() {
        return Combinators.combine(
            validTransacaoIds(),
            validUsuarioIds(),
            validValores(),
            validDescricoes(),
            validNomesCategorias(),
            validTiposTransacao(),
            validDatasTransacao()
        ).as(ComandoAtualizarTransacao::new);
    }

    @Provide
    Arbitrary<Usuario> validUsuarios() {
        return Combinators.combine(
            validUsuarioIds(),
            validEmails(),
            validNomes()
        ).as((usuarioId, email, nome) -> new Usuario(
            usuarioId,
            email,
            SenhaHash.criarDeSenhaTexto("ValidPass123@"),
            nome,
            LocalDateTime.now().minusDays(1),
            true, // ativo
            Arbitraries.of(true, false).sample()
        ));
    }

    @Provide
    Arbitrary<Usuario> inactiveUsuarios() {
        return Combinators.combine(
            validUsuarioIds(),
            validEmails(),
            validNomes()
        ).as((usuarioId, email, nome) -> new Usuario(
            usuarioId,
            email,
            SenhaHash.criarDeSenhaTexto("ValidPass123@"),
            nome,
            LocalDateTime.now().minusDays(1),
            false, // inativo
            Arbitraries.of(true, false).sample()
        ));
    }

    @Provide
    Arbitrary<Transacao> validTransacaoExistente() {
        return Combinators.combine(
            validTransacaoIds(),
            validUsuarioIds(),
            validValores(),
            validDescricoes(),
            validCategorias(),
            validDatasTransacao(),
            validTiposTransacao()
        ).as((transacaoId, usuarioId, valor, descricao, categoria, data, tipo) -> 
            new Transacao(
                transacaoId,
                usuarioId,
                Valor.reais(valor),
                new Descricao(descricao),
                categoria,
                data,
                tipo,
                LocalDateTime.now().minusHours(1), // criadoEm
                LocalDateTime.now().minusHours(1), // atualizadoEm
                true // ativa
            )
        );
    }

    @Provide
    Arbitrary<UsuarioId> validUsuarioIds() {
        return Arbitraries.create(() -> new UsuarioId(UUID.randomUUID()));
    }

    @Provide
    Arbitrary<TransacaoId> validTransacaoIds() {
        return Arbitraries.create(() -> new TransacaoId(UUID.randomUUID()));
    }

    @Provide
    Arbitrary<BigDecimal> validValores() {
        return Arbitraries.bigDecimals()
            .between(BigDecimal.valueOf(0.01), BigDecimal.valueOf(999999.99))
            .ofScale(2);
    }

    @Provide
    Arbitrary<String> validDescricoes() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars(' ', '.', ',', '-', '_')
            .ofMinLength(5)
            .ofMaxLength(100)
            .map(s -> s.trim())
            .filter(s -> !s.isEmpty());
    }

    @Provide
    Arbitrary<String> validNomesCategorias() {
        return Arbitraries.oneOf(
            // Categorias predefinidas
            Arbitraries.of("ALIMENTACAO", "TRANSPORTE", "MORADIA", "LAZER", "SAUDE", 
                          "SALARIO", "FREELANCE", "INVESTIMENTOS", "VENDAS"),
            // Categorias personalizadas
            Arbitraries.strings()
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .withChars('_')
                .ofMinLength(3)
                .ofMaxLength(20)
                .map(s -> "CUSTOM_" + s)
        );
    }

    @Provide
    Arbitrary<TipoTransacao> validTiposTransacao() {
        return Arbitraries.of(TipoTransacao.class);
    }

    @Provide
    Arbitrary<LocalDate> validDatasTransacao() {
        return Dates.dates()
            .between(LocalDate.now().minusYears(2), LocalDate.now());
    }

    @Provide
    Arbitrary<Categoria> validCategorias() {
        return Arbitraries.oneOf(
            Arbitraries.of(Categoria.ALIMENTACAO, Categoria.TRANSPORTE, Categoria.MORADIA,
                          Categoria.SALARIO, Categoria.FREELANCE, Categoria.INVESTIMENTOS),
            Combinators.combine(
                validNomesCategorias(),
                Arbitraries.of(TipoCategoria.class)
            ).as(Categoria::new)
        );
    }

    @Provide
    Arbitrary<Email> validEmails() {
        Arbitrary<String> localPart = Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('0', '9')
            .ofMinLength(3)
            .ofMaxLength(10);
            
        Arbitrary<String> domain = Arbitraries.of("gmail.com", "yahoo.com", "hotmail.com", "example.com");
        
        return Combinators.combine(localPart, domain)
            .as((local, dom) -> new Email(local + "@" + dom));
    }

    @Provide
    Arbitrary<Nome> validNomes() {
        Arbitrary<String> firstName = Arbitraries.of("João", "Maria", "Pedro", "Ana", "Carlos", "Lucia");
        Arbitrary<String> lastName = Arbitraries.of("Silva", "Santos", "Oliveira", "Souza", "Costa", "Pereira");
        
        return Combinators.combine(firstName, lastName)
            .as((first, last) -> new Nome(first + " " + last));
    }

    // Helper method to create categories for tests
    private Categoria criarCategoriaParaTeste(String nomeCategoria, TipoTransacao tipo) {
        Categoria categoriaPredefinida = Categoria.buscarPredefinida(nomeCategoria);
        if (categoriaPredefinida != null) {
            return categoriaPredefinida;
        }
        
        return tipo.ehReceita() 
            ? Categoria.receitaPersonalizada(nomeCategoria)
            : Categoria.despesaPersonalizada(nomeCategoria);
    }
}
