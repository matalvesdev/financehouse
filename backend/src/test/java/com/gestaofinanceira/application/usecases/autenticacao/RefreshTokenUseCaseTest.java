package com.gestaofinanceira.application.usecases.autenticacao;

import com.gestaofinanceira.application.dto.response.AutenticacaoResponse;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.application.ports.service.TokenJwtPort;
import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.Email;
import com.gestaofinanceira.domain.valueobjects.Nome;
import com.gestaofinanceira.domain.valueobjects.SenhaHash;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RefreshTokenUseCase.
 * 
 * Valida o comportamento do caso de uso de renovação de tokens,
 * incluindo validação de refresh tokens e geração de novos access tokens.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenUseCase")
class RefreshTokenUseCaseTest {
    
    @Mock
    private UsuarioRepository usuarioRepository;
    
    @Mock
    private TokenJwtPort tokenJwtPort;
    
    private RefreshTokenUseCase useCase;
    
    private final String REFRESH_TOKEN_VALIDO = "refresh-token-456";
    private final UsuarioId USUARIO_ID = new UsuarioId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
    
    @BeforeEach
    void setUp() {
        useCase = new RefreshTokenUseCase(usuarioRepository, tokenJwtPort);
    }
    
    @Test
    @DisplayName("Deve renovar access token com refresh token válido")
    void deveRenovarAccessTokenComRefreshTokenValido() {
        // Arrange
        Usuario usuario = criarUsuarioMock();
        
        when(tokenJwtPort.validarToken(REFRESH_TOKEN_VALIDO))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, USUARIO_ID, LocalDateTime.now().plusDays(7), 
                TokenJwtPort.TipoToken.REFRESH
            ));
        
        when(tokenJwtPort.tokenInvalidado(REFRESH_TOKEN_VALIDO))
            .thenReturn(false);
        
        when(usuarioRepository.buscarPorId(USUARIO_ID))
            .thenReturn(Optional.of(usuario));
        
        when(tokenJwtPort.gerarTokenAcesso(any(UsuarioId.class), any(Map.class)))
            .thenReturn("novo-access-token-789");
        
        // Act
        AutenticacaoResponse resultado = useCase.executar(REFRESH_TOKEN_VALIDO);
        
        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.accessToken()).isEqualTo("novo-access-token-789");
        assertThat(resultado.refreshToken()).isEqualTo(REFRESH_TOKEN_VALIDO); // Mantém o mesmo
        assertThat(resultado.tokenType()).isEqualTo("Bearer");
        assertThat(resultado.expiresIn()).isEqualTo(15 * 60); // 15 minutos
        assertThat(resultado.usuario().email()).isEqualTo("joao@email.com");
        
        // Verify interactions
        verify(tokenJwtPort).validarToken(REFRESH_TOKEN_VALIDO);
        verify(tokenJwtPort).tokenInvalidado(REFRESH_TOKEN_VALIDO);
        verify(usuarioRepository).buscarPorId(USUARIO_ID);
        verify(tokenJwtPort).gerarTokenAcesso(any(UsuarioId.class), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve rejeitar refresh token nulo")
    void deveRejeitarRefreshTokenNulo() {
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Refresh token não pode ser nulo");
    }
    
    @Test
    @DisplayName("Deve rejeitar refresh token vazio")
    void deveRejeitarRefreshTokenVazio() {
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Refresh token não pode estar vazio");
    }
    
    @Test
    @DisplayName("Deve rejeitar refresh token inválido")
    void deveRejeitarRefreshTokenInvalido() {
        // Arrange
        String tokenInvalido = "token-invalido";
        
        when(tokenJwtPort.validarToken(tokenInvalido))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                false, "Token malformado", null, null, null
            ));
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(tokenInvalido))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Refresh token inválido: Token malformado");
    }
    
    @Test
    @DisplayName("Deve rejeitar token que não é refresh token")
    void deveRejeitarTokenQueNaoEhRefreshToken() {
        // Arrange
        String accessToken = "access-token-123";
        
        when(tokenJwtPort.validarToken(accessToken))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, USUARIO_ID, LocalDateTime.now().plusMinutes(15), 
                TokenJwtPort.TipoToken.ACESSO
            ));
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(accessToken))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Token fornecido não é um refresh token");
    }
    
    @Test
    @DisplayName("Deve rejeitar refresh token invalidado")
    void deveRejeitarRefreshTokenInvalidado() {
        // Arrange
        when(tokenJwtPort.validarToken(REFRESH_TOKEN_VALIDO))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, USUARIO_ID, LocalDateTime.now().plusDays(7), 
                TokenJwtPort.TipoToken.REFRESH
            ));
        
        when(tokenJwtPort.tokenInvalidado(REFRESH_TOKEN_VALIDO))
            .thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(REFRESH_TOKEN_VALIDO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Refresh token foi invalidado");
    }
    
    @Test
    @DisplayName("Deve rejeitar quando usuário não existe")
    void deveRejeitarQuandoUsuarioNaoExiste() {
        // Arrange
        when(tokenJwtPort.validarToken(REFRESH_TOKEN_VALIDO))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, USUARIO_ID, LocalDateTime.now().plusDays(7), 
                TokenJwtPort.TipoToken.REFRESH
            ));
        
        when(tokenJwtPort.tokenInvalidado(REFRESH_TOKEN_VALIDO))
            .thenReturn(false);
        
        when(usuarioRepository.buscarPorId(USUARIO_ID))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(REFRESH_TOKEN_VALIDO))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Usuário não encontrado");
    }
    
    @Test
    @DisplayName("Deve rejeitar usuário inativo")
    void deveRejeitarUsuarioInativo() {
        // Arrange
        Usuario usuarioInativo = new Usuario(
            USUARIO_ID,
            new Email("joao@email.com"),
            SenhaHash.criarDeSenhaTexto("SenhaForte123!"),
            new Nome("João Silva"),
            LocalDateTime.now(),
            false, // inativo
            false
        );
        
        when(tokenJwtPort.validarToken(REFRESH_TOKEN_VALIDO))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, USUARIO_ID, LocalDateTime.now().plusDays(7), 
                TokenJwtPort.TipoToken.REFRESH
            ));
        
        when(tokenJwtPort.tokenInvalidado(REFRESH_TOKEN_VALIDO))
            .thenReturn(false);
        
        when(usuarioRepository.buscarPorId(USUARIO_ID))
            .thenReturn(Optional.of(usuarioInativo));
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(REFRESH_TOKEN_VALIDO))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Conta de usuário está inativa");
    }
    
    @Test
    @DisplayName("Deve incluir claims corretos no novo access token")
    void deveIncluirClaimsCorretosNoNovoAccessToken() {
        // Arrange
        Usuario usuario = criarUsuarioMock();
        
        when(tokenJwtPort.validarToken(REFRESH_TOKEN_VALIDO))
            .thenReturn(new TokenJwtPort.ResultadoValidacaoToken(
                true, null, USUARIO_ID, LocalDateTime.now().plusDays(7), 
                TokenJwtPort.TipoToken.REFRESH
            ));
        
        when(tokenJwtPort.tokenInvalidado(REFRESH_TOKEN_VALIDO))
            .thenReturn(false);
        
        when(usuarioRepository.buscarPorId(USUARIO_ID))
            .thenReturn(Optional.of(usuario));
        
        when(tokenJwtPort.gerarTokenAcesso(any(UsuarioId.class), any(Map.class)))
            .thenReturn("novo-access-token-789");
        
        // Act
        useCase.executar(REFRESH_TOKEN_VALIDO);
        
        // Assert - Verificar que os claims foram passados corretamente
        verify(tokenJwtPort).gerarTokenAcesso(eq(usuario.getId()), argThat(claims -> {
            Map<String, Object> claimsMap = (Map<String, Object>) claims;
            return claimsMap.get("email").equals("joao@email.com") &&
                   claimsMap.get("nome").equals("João Silva") &&
                   claimsMap.get("ativo").equals(true) &&
                   claimsMap.get("dadosIniciaisCarregados").equals(false);
        }));
    }
    
    private Usuario criarUsuarioMock() {
        return new Usuario(
            USUARIO_ID,
            new Email("joao@email.com"),
            new SenhaHash("$2a$10$hashedPassword", "salt123"),
            new Nome("João Silva"),
            LocalDateTime.now(),
            true,
            false
        );
    }
}