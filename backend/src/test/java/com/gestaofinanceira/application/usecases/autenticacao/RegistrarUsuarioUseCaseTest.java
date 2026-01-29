package com.gestaofinanceira.application.usecases.autenticacao;

import com.gestaofinanceira.application.dto.command.ComandoCriarUsuario;
import com.gestaofinanceira.application.dto.response.UsuarioResponse;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.application.ports.service.CriptografiaPort;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RegistrarUsuarioUseCase.
 * 
 * Valida o comportamento do caso de uso de registro de usuários,
 * incluindo validações, verificações de segurança e persistência.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegistrarUsuarioUseCase")
class RegistrarUsuarioUseCaseTest {
    
    @Mock
    private UsuarioRepository usuarioRepository;
    
    @Mock
    private CriptografiaPort criptografiaPort;
    
    private RegistrarUsuarioUseCase useCase;
    
    @BeforeEach
    void setUp() {
        useCase = new RegistrarUsuarioUseCase(usuarioRepository, criptografiaPort);
    }
    
    @Test
    @DisplayName("Deve registrar usuário com dados válidos")
    void deveRegistrarUsuarioComDadosValidos() {
        // Arrange
        ComandoCriarUsuario comando = new ComandoCriarUsuario(
            "João Silva",
            "joao@email.com",
            "MinhaSenh@123"
        );
        
        // Mock validação de senha
        when(criptografiaPort.validarForcaSenha("MinhaSenh@123"))
            .thenReturn(new CriptografiaPort.ResultadoValidacaoSenha(
                true, 85, CriptografiaPort.NivelForca.FORTE, List.of()
            ));
        
        // Mock hash da senha
        when(criptografiaPort.hashearSenha("MinhaSenh@123"))
            .thenReturn("$2a$10$hashedPassword");
        
        // Mock verificação de email único
        when(usuarioRepository.existePorEmail(any(Email.class)))
            .thenReturn(false);
        
        // Mock salvamento
        Usuario usuarioMock = criarUsuarioMock();
        when(usuarioRepository.salvar(any(Usuario.class)))
            .thenReturn(usuarioMock);
        
        // Act
        UsuarioResponse resultado = useCase.executar(comando);
        
        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.email()).isEqualTo("joao@email.com");
        assertThat(resultado.nome()).isEqualTo("João Silva");
        assertThat(resultado.ativo()).isTrue();
        assertThat(resultado.dadosIniciaisCarregados()).isFalse();
        
        // Verify interactions
        verify(criptografiaPort).validarForcaSenha("MinhaSenh@123");
        verify(criptografiaPort).hashearSenha("MinhaSenh@123");
        verify(usuarioRepository).existePorEmail(any(Email.class));
        verify(usuarioRepository).salvar(any(Usuario.class));
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
    @DisplayName("Deve rejeitar nome vazio")
    void deveRejeitarNomeVazio() {
        // Arrange
        ComandoCriarUsuario comando = new ComandoCriarUsuario(
            "",
            "joao@email.com",
            "MinhaSenh@123"
        );
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(comando))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Nome é obrigatório");
    }
    
    @Test
    @DisplayName("Deve rejeitar email vazio")
    void deveRejeitarEmailVazio() {
        // Arrange
        ComandoCriarUsuario comando = new ComandoCriarUsuario(
            "João Silva",
            "",
            "MinhaSenh@123"
        );
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(comando))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email é obrigatório");
    }
    
    @Test
    @DisplayName("Deve rejeitar senha vazia")
    void deveRejeitarSenhaVazia() {
        // Arrange
        ComandoCriarUsuario comando = new ComandoCriarUsuario(
            "João Silva",
            "joao@email.com",
            ""
        );
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(comando))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Senha é obrigatória");
    }
    
    @Test
    @DisplayName("Deve rejeitar email já existente")
    void deveRejeitarEmailJaExistente() {
        // Arrange
        ComandoCriarUsuario comando = new ComandoCriarUsuario(
            "João Silva",
            "joao@email.com",
            "MinhaSenh@123"
        );
        
        when(usuarioRepository.existePorEmail(any(Email.class)))
            .thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(comando))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Email já está em uso: joao@email.com");
    }
    
    @Test
    @DisplayName("Deve rejeitar senha fraca")
    void deveRejeitarSenhaFraca() {
        // Arrange
        ComandoCriarUsuario comando = new ComandoCriarUsuario(
            "João Silva",
            "joao@email.com",
            "123"
        );
        
        when(usuarioRepository.existePorEmail(any(Email.class)))
            .thenReturn(false);
        
        when(criptografiaPort.validarForcaSenha("123"))
            .thenReturn(new CriptografiaPort.ResultadoValidacaoSenha(
                false, 20, CriptografiaPort.NivelForca.MUITO_FRACA, 
                List.of("Adicione letras maiúsculas", "Adicione caracteres especiais")
            ));
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(comando))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Senha não atende aos critérios de segurança");
    }
    
    @Test
    @DisplayName("Deve rejeitar email inválido")
    void deveRejeitarEmailInvalido() {
        // Arrange
        ComandoCriarUsuario comando = new ComandoCriarUsuario(
            "João Silva",
            "email-invalido",
            "MinhaSenh@123"
        );
        
        // Act & Assert - O value object Email deve lançar exceção
        assertThatThrownBy(() -> useCase.executar(comando))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    private Usuario criarUsuarioMock() {
        return new Usuario(
            new UsuarioId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")),
            new Email("joao@email.com"),
            SenhaHash.criarDeSenhaTexto("SenhaForte123!"),
            new Nome("João Silva"),
            LocalDateTime.now(),
            true,
            false
        );
    }
}