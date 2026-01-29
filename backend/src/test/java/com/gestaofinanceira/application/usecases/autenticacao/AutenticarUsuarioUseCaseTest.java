package com.gestaofinanceira.application.usecases.autenticacao;

import com.gestaofinanceira.application.dto.command.ComandoAutenticarUsuario;
import com.gestaofinanceira.application.dto.response.AutenticacaoResponse;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.application.ports.service.CriptografiaPort;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para AutenticarUsuarioUseCase.
 * 
 * Valida o comportamento do caso de uso de autenticação,
 * incluindo validação de credenciais e geração de tokens.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AutenticarUsuarioUseCase")
class AutenticarUsuarioUseCaseTest {
    
    @Mock
    private UsuarioRepository usuarioRepository;
    
    @Mock
    private CriptografiaPort criptografiaPort;
    
    @Mock
    private TokenJwtPort tokenJwtPort;
    
    private AutenticarUsuarioUseCase useCase;
    
    @BeforeEach
    void setUp() {
        useCase = new AutenticarUsuarioUseCase(usuarioRepository, criptografiaPort, tokenJwtPort);
    }
    
    @Test
    @DisplayName("Deve autenticar usuário com credenciais válidas")
    void deveAutenticarUsuarioComCredenciaisValidas() {
        // Arrange
        ComandoAutenticarUsuario comando = new ComandoAutenticarUsuario(
            "joao@email.com",
            "MinhaSenh@123"
        );
        
        Usuario usuario = criarUsuarioMock();
        when(usuarioRepository.buscarPorEmail(any(Email.class)))
            .thenReturn(Optional.of(usuario));
        
        when(criptografiaPort.verificarSenha("MinhaSenh@123", "$2a$10$hashedPassword"))
            .thenReturn(true);
        
        when(tokenJwtPort.gerarTokenAcesso(any(UsuarioId.class), any(Map.class)))
            .thenReturn("access-token-123");
        
        when(tokenJwtPort.gerarTokenRefresh(any(UsuarioId.class)))
            .thenReturn("refresh-token-456");
        
        // Act
        AutenticacaoResponse resultado = useCase.executar(comando);
        
        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.accessToken()).isEqualTo("access-token-123");
        assertThat(resultado.refreshToken()).isEqualTo("refresh-token-456");
        assertThat(resultado.tokenType()).isEqualTo("Bearer");
        assertThat(resultado.expiresIn()).isEqualTo(15 * 60); // 15 minutos
        assertThat(resultado.usuario().email()).isEqualTo("joao@email.com");
        
        // Verify interactions
        verify(usuarioRepository).buscarPorEmail(any(Email.class));
        verify(criptografiaPort).verificarSenha("MinhaSenh@123", "$2a$10$hashedPassword");
        verify(tokenJwtPort).gerarTokenAcesso(any(UsuarioId.class), any(Map.class));
        verify(tokenJwtPort).gerarTokenRefresh(any(UsuarioId.class));
    }
    
    @Test
    @DisplayName("Deve rejeitar comando nulo")
    void deveRejeitarComandoNulo() {
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Comando não pode ser nulo");
    }
    
    @Test
    @DisplayName("Deve rejeitar email vazio")
    void deveRejeitarEmailVazio() {
        // Arrange
        ComandoAutenticarUsuario comando = new ComandoAutenticarUsuario("", "senha123");
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(comando))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email é obrigatório");
    }
    
    @Test
    @DisplayName("Deve rejeitar senha vazia")
    void deveRejeitarSenhaVazia() {
        // Arrange
        ComandoAutenticarUsuario comando = new ComandoAutenticarUsuario("joao@email.com", "");
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(comando))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Senha é obrigatória");
    }
    
    @Test
    @DisplayName("Deve rejeitar usuário não encontrado")
    void deveRejeitarUsuarioNaoEncontrado() {
        // Arrange
        ComandoAutenticarUsuario comando = new ComandoAutenticarUsuario(
            "inexistente@email.com",
            "MinhaSenh@123"
        );
        
        when(usuarioRepository.buscarPorEmail(any(Email.class)))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(comando))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Credenciais inválidas");
    }
    
    @Test
    @DisplayName("Deve rejeitar usuário inativo")
    void deveRejeitarUsuarioInativo() {
        // Arrange
        ComandoAutenticarUsuario comando = new ComandoAutenticarUsuario(
            "joao@email.com",
            "MinhaSenh@123"
        );
        
        Usuario usuarioInativo = new Usuario(
            UsuarioId.de("123e4567-e89b-12d3-a456-426614174000"),
            new Email("joao@email.com"),
            new SenhaHash("$2a$10$hashedPassword", "salt123"),
            new Nome("João Silva"),
            LocalDateTime.now(),
            false, // inativo
            false
        );
        
        when(usuarioRepository.buscarPorEmail(any(Email.class)))
            .thenReturn(Optional.of(usuarioInativo));
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(comando))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Conta de usuário está inativa");
    }
    
    @Test
    @DisplayName("Deve rejeitar senha incorreta")
    void deveRejeitarSenhaIncorreta() {
        // Arrange
        ComandoAutenticarUsuario comando = new ComandoAutenticarUsuario(
            "joao@email.com",
            "SenhaErrada123"
        );
        
        Usuario usuario = criarUsuarioMock();
        when(usuarioRepository.buscarPorEmail(any(Email.class)))
            .thenReturn(Optional.of(usuario));
        
        when(criptografiaPort.verificarSenha("SenhaErrada123", "$2a$10$hashedPassword"))
            .thenReturn(false);
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(comando))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Credenciais inválidas");
        
        // Verify que não tentou gerar tokens
        verify(tokenJwtPort, never()).gerarTokenAcesso(any(), any());
        verify(tokenJwtPort, never()).gerarTokenRefresh(any());
    }
    
    @Test
    @DisplayName("Deve incluir claims corretos no token")
    void deveIncluirClaimsCorretosNoToken() {
        // Arrange
        ComandoAutenticarUsuario comando = new ComandoAutenticarUsuario(
            "joao@email.com",
            "MinhaSenh@123"
        );
        
        Usuario usuario = criarUsuarioMock();
        when(usuarioRepository.buscarPorEmail(any(Email.class)))
            .thenReturn(Optional.of(usuario));
        
        when(criptografiaPort.verificarSenha("MinhaSenh@123", "$2a$10$hashedPassword"))
            .thenReturn(true);
        
        when(tokenJwtPort.gerarTokenAcesso(any(UsuarioId.class), any(Map.class)))
            .thenReturn("access-token-123");
        
        when(tokenJwtPort.gerarTokenRefresh(any(UsuarioId.class)))
            .thenReturn("refresh-token-456");
        
        // Act
        useCase.executar(comando);
        
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
            UsuarioId.de("123e4567-e89b-12d3-a456-426614174000"),
            new Email("joao@email.com"),
            new SenhaHash("$2a$10$hashedPassword", "salt123"),
            new Nome("João Silva"),
            LocalDateTime.now(),
            true,
            false
        );
    }
}