package com.gestaofinanceira.application.usecases.autenticacao;

import com.gestaofinanceira.application.dto.response.AutenticacaoResponse;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.application.ports.service.TokenJwtPort;
import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.Email;
import com.gestaofinanceira.domain.valueobjects.Nome;
import com.gestaofinanceira.domain.valueobjects.SenhaHash;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;

import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for authentication use cases.
 * Tests universal properties that should hold for all valid authentication scenarios.
 * 
 * **Validates: Requirements 1.3, 1.4**
 */
@Label("Feature: gestao-financeira-domestica, Authentication Properties")
@ExtendWith(MockitoExtension.class)
class AutenticacaoPropertyTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    
    @Mock
    private TokenJwtPort tokenJwtPort;
    
    private RefreshTokenUseCase refreshTokenUseCase;
    private LogoutUsuarioUseCase logoutUsuarioUseCase;

    @BeforeEach
    void setUp() {
        refreshTokenUseCase = new RefreshTokenUseCase(usuarioRepository, tokenJwtPort);
        logoutUsuarioUseCase = new LogoutUsuarioUseCase(tokenJwtPort);
    }

    /**
     * Property 3: Token refresh round-trip
     * For any valid refresh token, using it to refresh should produce a new valid access token 
     * that can be used for authenticated requests
     * **Validates: Requirements 1.3**
     */
    @Property(tries = 20)
    @Label("Property 3: Token refresh round-trip")
    void tokenRefreshRoundTripShouldProduceValidAccessToken(
            @ForAll("validRefreshTokens") String refreshToken,
            @ForAll("validUsuarios") Usuario usuario) {
        
        // Arrange - Setup valid refresh token scenario
        UsuarioId usuarioId = usuario.getId();
        String expectedNewAccessToken = "new-access-token-" + UUID.randomUUID();
        
        when(tokenJwtPort.validarToken(refreshToken))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, usuarioId, LocalDateTime.now().plusDays(7), 
                TokenJwtPort.TipoToken.REFRESH
            ));
        
        when(tokenJwtPort.tokenInvalidado(refreshToken))
            .thenReturn(false);
        
        when(usuarioRepository.buscarPorId(usuarioId))
            .thenReturn(Optional.of(usuario));
        
        when(tokenJwtPort.gerarTokenAcesso(eq(usuarioId), any(Map.class)))
            .thenReturn(expectedNewAccessToken);
        
        // Simulate that the new access token is valid for authentication
        when(tokenJwtPort.validarToken(expectedNewAccessToken))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, usuarioId, LocalDateTime.now().plusMinutes(15), 
                TokenJwtPort.TipoToken.ACESSO
            ));
        
        // Act - Perform token refresh
        AutenticacaoResponse response = refreshTokenUseCase.executar(refreshToken);
        
        // Assert - Verify round-trip properties
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(expectedNewAccessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken); // Should maintain same refresh token
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(15 * 60); // 15 minutes
        assertThat(response.usuario().id()).isEqualTo(usuarioId.valor());
        
        // Verify the new access token is valid for authentication
        var validationResult = tokenJwtPort.validarToken(response.accessToken());
        assertThat(validationResult.valido()).isTrue();
        assertThat(validationResult.usuarioId()).isEqualTo(usuarioId);
        assertThat(validationResult.tipo()).isEqualTo(TokenJwtPort.TipoToken.ACESSO);
        
        // Verify interactions
        verify(tokenJwtPort).validarToken(refreshToken);
        verify(tokenJwtPort).tokenInvalidado(refreshToken);
        verify(usuarioRepository).buscarPorId(usuarioId);
        verify(tokenJwtPort).gerarTokenAcesso(eq(usuarioId), any(Map.class));
    }

    /**
     * Property 4: Logout token invalidation
     * For any authenticated user session, logging out should invalidate both access and refresh tokens,
     * making them unusable for subsequent requests
     * **Validates: Requirements 1.4**
     */
    @Property(tries = 20)
    @Label("Property 4: Logout token invalidation")
    void logoutShouldInvalidateBothTokens(
            @ForAll("validAccessTokens") String accessToken,
            @ForAll("validRefreshTokens") String refreshToken,
            @ForAll("validUsuarios") Usuario usuario) {
        
        // Arrange - Setup valid token pair for same user
        UsuarioId usuarioId = usuario.getId();
        
        when(tokenJwtPort.validarToken(accessToken))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, usuarioId, LocalDateTime.now().plusMinutes(15), 
                TokenJwtPort.TipoToken.ACESSO
            ));
        
        when(tokenJwtPort.validarToken(refreshToken))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, usuarioId, LocalDateTime.now().plusDays(7), 
                TokenJwtPort.TipoToken.REFRESH
            ));
        
        when(tokenJwtPort.tokenExpirado(accessToken))
            .thenReturn(false);
        
        // Act - Perform logout
        logoutUsuarioUseCase.executar(accessToken, refreshToken);
        
        // Assert - Verify both tokens are invalidated
        verify(tokenJwtPort).invalidarToken(accessToken);
        verify(tokenJwtPort).invalidarToken(refreshToken);
        
        // Simulate post-logout state - tokens should be in blacklist
        when(tokenJwtPort.tokenInvalidado(accessToken)).thenReturn(true);
        when(tokenJwtPort.tokenInvalidado(refreshToken)).thenReturn(true);
        
        // Verify tokens are now unusable
        assertThat(tokenJwtPort.tokenInvalidado(accessToken)).isTrue();
        assertThat(tokenJwtPort.tokenInvalidado(refreshToken)).isTrue();
        
        // Verify validation interactions
        verify(tokenJwtPort).validarToken(accessToken);
        verify(tokenJwtPort).validarToken(refreshToken);
        verify(tokenJwtPort).tokenExpirado(accessToken);
    }

    /**
     * Property: Logout with refresh token only should invalidate the token
     * For any valid refresh token, logout should invalidate it making it unusable
     */
    @Property(tries = 20)
    @Label("Property: Logout with refresh token only invalidation")
    void logoutWithRefreshTokenOnlyShouldInvalidateToken(
            @ForAll("validRefreshTokens") String refreshToken,
            @ForAll("validUsuarios") Usuario usuario) {
        
        // Arrange
        UsuarioId usuarioId = usuario.getId();
        
        when(tokenJwtPort.validarToken(refreshToken))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, usuarioId, LocalDateTime.now().plusDays(7), 
                TokenJwtPort.TipoToken.REFRESH
            ));
        
        // Act
        logoutUsuarioUseCase.executarComRefreshToken(refreshToken);
        
        // Assert
        verify(tokenJwtPort).invalidarToken(refreshToken);
        verify(tokenJwtPort).validarToken(refreshToken);
        
        // Simulate post-logout state
        when(tokenJwtPort.tokenInvalidado(refreshToken)).thenReturn(true);
        assertThat(tokenJwtPort.tokenInvalidado(refreshToken)).isTrue();
    }

    /**
     * Property: Token refresh should fail with invalidated refresh token
     * For any refresh token that has been invalidated, refresh attempts should fail
     */
    @Property(tries = 20)
    @Label("Property: Invalidated refresh token rejection")
    void refreshShouldFailWithInvalidatedToken(
            @ForAll("validRefreshTokens") String refreshToken,
            @ForAll("validUsuarios") Usuario usuario) {
        
        // Arrange - Setup invalidated refresh token
        UsuarioId usuarioId = usuario.getId();
        
        when(tokenJwtPort.validarToken(refreshToken))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, usuarioId, LocalDateTime.now().plusDays(7), 
                TokenJwtPort.TipoToken.REFRESH
            ));
        
        when(tokenJwtPort.tokenInvalidado(refreshToken))
            .thenReturn(true); // Token is in blacklist
        
        // Act & Assert
        assertThatThrownBy(() -> refreshTokenUseCase.executar(refreshToken))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Refresh token foi invalidado");
        
        // Verify no new token was generated
        verify(tokenJwtPort, never()).gerarTokenAcesso(any(), any());
        verify(usuarioRepository, never()).buscarPorId(any());
    }

    /**
     * Property: Token refresh should fail with inactive user
     * For any refresh token belonging to an inactive user, refresh should fail
     */
    @Property(tries = 20)
    @Label("Property: Inactive user refresh rejection")
    void refreshShouldFailWithInactiveUser(
            @ForAll("validRefreshTokens") String refreshToken,
            @ForAll("inactiveUsuarios") Usuario inactiveUsuario) {
        
        // Arrange
        UsuarioId usuarioId = inactiveUsuario.getId();
        
        when(tokenJwtPort.validarToken(refreshToken))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, usuarioId, LocalDateTime.now().plusDays(7), 
                TokenJwtPort.TipoToken.REFRESH
            ));
        
        when(tokenJwtPort.tokenInvalidado(refreshToken))
            .thenReturn(false);
        
        when(usuarioRepository.buscarPorId(usuarioId))
            .thenReturn(Optional.of(inactiveUsuario));
        
        // Act & Assert
        assertThatThrownBy(() -> refreshTokenUseCase.executar(refreshToken))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Conta de usuário está inativa");
        
        // Verify no new token was generated
        verify(tokenJwtPort, never()).gerarTokenAcesso(any(), any());
    }

    // Generators for test data

    @Provide
    Arbitrary<String> validRefreshTokens() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars('.', '-', '_')
            .ofMinLength(50)
            .ofMaxLength(200)
            .map(token -> "refresh-token-" + token);
    }

    @Provide
    Arbitrary<String> validAccessTokens() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars('.', '-', '_')
            .ofMinLength(50)
            .ofMaxLength(200)
            .map(token -> "access-token-" + token);
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
    Arbitrary<UsuarioId> validUsuarioIds() {
        return Arbitraries.create(() -> new UsuarioId(UUID.randomUUID()));
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
}
