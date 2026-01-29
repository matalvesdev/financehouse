package com.gestaofinanceira.application.usecases.autenticacao;

import com.gestaofinanceira.application.ports.service.TokenJwtPort;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para LogoutUsuarioUseCase.
 * 
 * Valida o comportamento do caso de uso de logout,
 * incluindo invalidação de tokens e verificações de segurança.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutUsuarioUseCase")
class LogoutUsuarioUseCaseTest {
    
    @Mock
    private TokenJwtPort tokenJwtPort;
    
    private LogoutUsuarioUseCase useCase;
    
    private final String ACCESS_TOKEN = "access-token-123";
    private final String REFRESH_TOKEN = "refresh-token-456";
    private final UsuarioId USUARIO_ID = UsuarioId.de("123e4567-e89b-12d3-a456-426614174000");
    
    @BeforeEach
    void setUp() {
        useCase = new LogoutUsuarioUseCase(tokenJwtPort);
    }
    
    @Test
    @DisplayName("Deve fazer logout com tokens válidos")
    void deveFazerLogoutComTokensValidos() {
        // Arrange
        when(tokenJwtPort.validarToken(ACCESS_TOKEN))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, USUARIO_ID, LocalDateTime.now().plusMinutes(15), 
                TokenJwtPort.TipoToken.ACESSO
            ));
        
        when(tokenJwtPort.validarToken(REFRESH_TOKEN))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, USUARIO_ID, LocalDateTime.now().plusDays(7), 
                TokenJwtPort.TipoToken.REFRESH
            ));
        
        when(tokenJwtPort.tokenExpirado(ACCESS_TOKEN))
            .thenReturn(false);
        
        // Act
        useCase.executar(ACCESS_TOKEN, REFRESH_TOKEN);
        
        // Assert
        verify(tokenJwtPort).validarToken(ACCESS_TOKEN);
        verify(tokenJwtPort).validarToken(REFRESH_TOKEN);
        verify(tokenJwtPort).invalidarToken(ACCESS_TOKEN);
        verify(tokenJwtPort).invalidarToken(REFRESH_TOKEN);
    }
    
    @Test
    @DisplayName("Deve fazer logout com access token expirado")
    void deveFazerLogoutComAccessTokenExpirado() {
        // Arrange
        when(tokenJwtPort.validarToken(ACCESS_TOKEN))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                false, "Token expirado", USUARIO_ID, LocalDateTime.now().minusMinutes(5), 
                TokenJwtPort.TipoToken.ACESSO
            ));
        
        when(tokenJwtPort.tokenExpirado(ACCESS_TOKEN))
            .thenReturn(true);
        
        when(tokenJwtPort.validarToken(REFRESH_TOKEN))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, USUARIO_ID, LocalDateTime.now().plusDays(7), 
                TokenJwtPort.TipoToken.REFRESH
            ));
        
        // Act
        useCase.executar(ACCESS_TOKEN, REFRESH_TOKEN);
        
        // Assert
        verify(tokenJwtPort).invalidarToken(ACCESS_TOKEN);
        verify(tokenJwtPort).invalidarToken(REFRESH_TOKEN);
    }
    
    @Test
    @DisplayName("Deve rejeitar access token nulo")
    void deveRejeitarAccessTokenNulo() {
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(null, REFRESH_TOKEN))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Access token não pode ser nulo");
    }
    
    @Test
    @DisplayName("Deve rejeitar refresh token nulo")
    void deveRejeitarRefreshTokenNulo() {
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(ACCESS_TOKEN, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Refresh token não pode ser nulo");
    }
    
    @Test
    @DisplayName("Deve rejeitar access token vazio")
    void deveRejeitarAccessTokenVazio() {
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar("", REFRESH_TOKEN))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Access token não pode estar vazio");
    }
    
    @Test
    @DisplayName("Deve rejeitar refresh token vazio")
    void deveRejeitarRefreshTokenVazio() {
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(ACCESS_TOKEN, ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Refresh token não pode estar vazio");
    }
    
    @Test
    @DisplayName("Deve rejeitar access token inválido")
    void deveRejeitarAccessTokenInvalido() {
        // Arrange
        when(tokenJwtPort.validarToken(ACCESS_TOKEN))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                false, "Token malformado", null, null, null
            ));
        
        when(tokenJwtPort.tokenExpirado(ACCESS_TOKEN))
            .thenReturn(false);
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(ACCESS_TOKEN, REFRESH_TOKEN))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Access token inválido: Token malformado");
    }
    
    @Test
    @DisplayName("Deve rejeitar refresh token inválido")
    void deveRejeitarRefreshTokenInvalido() {
        // Arrange
        when(tokenJwtPort.validarToken(ACCESS_TOKEN))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, USUARIO_ID, LocalDateTime.now().plusMinutes(15), 
                TokenJwtPort.TipoToken.ACESSO
            ));
        
        when(tokenJwtPort.tokenExpirado(ACCESS_TOKEN))
            .thenReturn(false);
        
        when(tokenJwtPort.validarToken(REFRESH_TOKEN))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                false, "Token malformado", null, null, null
            ));
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(ACCESS_TOKEN, REFRESH_TOKEN))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Refresh token inválido: Token malformado");
    }
    
    @Test
    @DisplayName("Deve rejeitar tokens de usuários diferentes")
    void deveRejeitarTokensDeUsuariosDiferentes() {
        // Arrange
        UsuarioId outroUsuarioId = UsuarioId.de("987e6543-e21c-34b5-a654-321987654000");
        
        when(tokenJwtPort.validarToken(ACCESS_TOKEN))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, USUARIO_ID, LocalDateTime.now().plusMinutes(15), 
                TokenJwtPort.TipoToken.ACESSO
            ));
        
        when(tokenJwtPort.tokenExpirado(ACCESS_TOKEN))
            .thenReturn(false);
        
        when(tokenJwtPort.validarToken(REFRESH_TOKEN))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, outroUsuarioId, LocalDateTime.now().plusDays(7), 
                TokenJwtPort.TipoToken.REFRESH
            ));
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(ACCESS_TOKEN, REFRESH_TOKEN))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Tokens pertencem a usuários diferentes");
    }
    
    @Test
    @DisplayName("Deve rejeitar quando primeiro token não é access token")
    void deveRejeitarQuandoPrimeiroTokenNaoEhAccessToken() {
        // Arrange
        when(tokenJwtPort.validarToken(ACCESS_TOKEN))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, USUARIO_ID, LocalDateTime.now().plusDays(7), 
                TokenJwtPort.TipoToken.REFRESH // Tipo errado
            ));
        
        when(tokenJwtPort.tokenExpirado(ACCESS_TOKEN))
            .thenReturn(false);
        
        when(tokenJwtPort.validarToken(REFRESH_TOKEN))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, USUARIO_ID, LocalDateTime.now().plusDays(7), 
                TokenJwtPort.TipoToken.REFRESH
            ));
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(ACCESS_TOKEN, REFRESH_TOKEN))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Primeiro token não é um access token");
    }
    
    @Test
    @DisplayName("Deve rejeitar quando segundo token não é refresh token")
    void deveRejeitarQuandoSegundoTokenNaoEhRefreshToken() {
        // Arrange
        when(tokenJwtPort.validarToken(ACCESS_TOKEN))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, USUARIO_ID, LocalDateTime.now().plusMinutes(15), 
                TokenJwtPort.TipoToken.ACESSO
            ));
        
        when(tokenJwtPort.tokenExpirado(ACCESS_TOKEN))
            .thenReturn(false);
        
        when(tokenJwtPort.validarToken(REFRESH_TOKEN))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, USUARIO_ID, LocalDateTime.now().plusMinutes(15), 
                TokenJwtPort.TipoToken.ACESSO // Tipo errado
            ));
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(ACCESS_TOKEN, REFRESH_TOKEN))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Segundo token não é um refresh token");
    }
    
    @Test
    @DisplayName("Deve fazer logout apenas com refresh token")
    void deveFazerLogoutApenasComRefreshToken() {
        // Arrange
        when(tokenJwtPort.validarToken(REFRESH_TOKEN))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, USUARIO_ID, LocalDateTime.now().plusDays(7), 
                TokenJwtPort.TipoToken.REFRESH
            ));
        
        // Act
        useCase.executarComRefreshToken(REFRESH_TOKEN);
        
        // Assert
        verify(tokenJwtPort).validarToken(REFRESH_TOKEN);
        verify(tokenJwtPort).invalidarToken(REFRESH_TOKEN);
    }
    
    @Test
    @DisplayName("Deve rejeitar refresh token nulo no método alternativo")
    void deveRejeitarRefreshTokenNuloNoMetodoAlternativo() {
        // Act & Assert
        assertThatThrownBy(() -> useCase.executarComRefreshToken(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Refresh token não pode ser nulo");
    }
    
    @Test
    @DisplayName("Deve rejeitar refresh token vazio no método alternativo")
    void deveRejeitarRefreshTokenVazioNoMetodoAlternativo() {
        // Act & Assert
        assertThatThrownBy(() -> useCase.executarComRefreshToken(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Refresh token não pode estar vazio");
    }
}