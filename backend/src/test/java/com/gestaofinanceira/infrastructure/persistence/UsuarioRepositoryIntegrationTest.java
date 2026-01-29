package com.gestaofinanceira.infrastructure.persistence;

import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.Email;
import com.gestaofinanceira.domain.valueobjects.Nome;
import com.gestaofinanceira.domain.valueobjects.SenhaHash;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes de integração para UsuarioRepository usando H2 in-memory database.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioRepositoryIntegrationTest {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Test
    void deveSalvarUsuarioComSucesso() {
        // Arrange
        Usuario usuario = Usuario.criar(
            new Email("teste@exemplo.com"),
            SenhaHash.criarDeSenhaTexto("ValidPass123!"),
            new Nome("João Silva")
        );
        
        // Act
        Usuario usuarioSalvo = usuarioRepository.salvar(usuario);
        
        // Assert
        assertThat(usuarioSalvo).isNotNull();
        assertThat(usuarioSalvo.getId()).isNotNull();
        assertThat(usuarioSalvo.getEmail().valor()).isEqualTo("teste@exemplo.com");
        assertThat(usuarioSalvo.getNome().valor()).isEqualTo("João Silva");
        assertThat(usuarioSalvo.isAtivo()).isTrue();
        assertThat(usuarioSalvo.isDadosIniciaisCarregados()).isFalse();
        assertThat(usuarioSalvo.getCriadoEm()).isNotNull();
    }
    
    @Test
    void deveBuscarUsuarioPorId() {
        // Arrange
        Usuario usuario = Usuario.criar(
            new Email("busca@exemplo.com"),
            SenhaHash.criarDeSenhaTexto("ValidPass123!"),
            new Nome("Maria Santos")
        );
        Usuario usuarioSalvo = usuarioRepository.salvar(usuario);
        
        // Act
        Optional<Usuario> usuarioEncontrado = usuarioRepository.buscarPorId(usuarioSalvo.getId());
        
        // Assert
        assertThat(usuarioEncontrado).isPresent();
        assertThat(usuarioEncontrado.get().getId()).isEqualTo(usuarioSalvo.getId());
        assertThat(usuarioEncontrado.get().getEmail().valor()).isEqualTo("busca@exemplo.com");
    }
    
    @Test
    void deveBuscarUsuarioPorEmail() {
        // Arrange
        Email email = new Email("email@exemplo.com");
        Usuario usuario = Usuario.criar(
            email,
            SenhaHash.criarDeSenhaTexto("ValidPass123!"),
            new Nome("Pedro Costa")
        );
        usuarioRepository.salvar(usuario);
        
        // Act
        Optional<Usuario> usuarioEncontrado = usuarioRepository.buscarPorEmail(email);
        
        // Assert
        assertThat(usuarioEncontrado).isPresent();
        assertThat(usuarioEncontrado.get().getEmail()).isEqualTo(email);
        assertThat(usuarioEncontrado.get().getNome().valor()).isEqualTo("Pedro Costa");
    }
    
    @Test
    void deveVerificarSeExisteUsuarioPorEmail() {
        // Arrange
        Email email = new Email("existe@exemplo.com");
        Usuario usuario = Usuario.criar(
            email,
            SenhaHash.criarDeSenhaTexto("ValidPass123!"),
            new Nome("Ana Silva")
        );
        usuarioRepository.salvar(usuario);
        
        // Act & Assert
        assertThat(usuarioRepository.existePorEmail(email)).isTrue();
        assertThat(usuarioRepository.existePorEmail(new Email("naoexiste@exemplo.com"))).isFalse();
    }
    
    @Test
    void deveAtualizarUsuario() {
        // Arrange
        Usuario usuario = Usuario.criar(
            new Email("atualizar@exemplo.com"),
            SenhaHash.criarDeSenhaTexto("ValidPass123!"),
            new Nome("Nome Original")
        );
        Usuario usuarioSalvo = usuarioRepository.salvar(usuario);
        
        // Act
        usuarioSalvo.atualizarNome(new Nome("Nome Atualizado"));
        usuarioSalvo.marcarDadosIniciaisCarregados();
        Usuario usuarioAtualizado = usuarioRepository.atualizar(usuarioSalvo);
        
        // Assert
        assertThat(usuarioAtualizado.getNome().valor()).isEqualTo("Nome Atualizado");
        assertThat(usuarioAtualizado.isDadosIniciaisCarregados()).isTrue();
    }
    
    @Test
    void deveRemoverUsuario() {
        // Arrange
        Usuario usuario = Usuario.criar(
            new Email("remover@exemplo.com"),
            SenhaHash.criarDeSenhaTexto("ValidPass123!"),
            new Nome("Usuario Para Remover")
        );
        Usuario usuarioSalvo = usuarioRepository.salvar(usuario);
        
        // Act
        usuarioRepository.remover(usuarioSalvo.getId());
        
        // Assert
        Optional<Usuario> usuarioRemovido = usuarioRepository.buscarPorEmail(usuarioSalvo.getEmail());
        assertThat(usuarioRemovido).isEmpty(); // Soft delete - não aparece na busca por email ativo
    }
    
    @Test
    void deveLancarExcecaoAoAtualizarUsuarioInexistente() {
        // Arrange
        Usuario usuario = Usuario.criar(
            new Email("inexistente@exemplo.com"),
            SenhaHash.criarDeSenhaTexto("ValidPass123!"),
            new Nome("Usuario Inexistente")
        );
        
        // Act & Assert
        assertThatThrownBy(() -> usuarioRepository.atualizar(usuario))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Usuário não encontrado para atualização");
    }
    
    @Test
    void naoDeveBuscarUsuarioInativo() {
        // Arrange
        Usuario usuario = Usuario.criar(
            new Email("inativo@exemplo.com"),
            SenhaHash.criarDeSenhaTexto("ValidPass123!"),
            new Nome("Usuario Inativo")
        );
        Usuario usuarioSalvo = usuarioRepository.salvar(usuario);
        
        // Desativa o usuário
        usuarioSalvo.desativar();
        usuarioRepository.atualizar(usuarioSalvo);
        
        // Act
        Optional<Usuario> usuarioEncontrado = usuarioRepository.buscarPorEmail(usuarioSalvo.getEmail());
        
        // Assert
        assertThat(usuarioEncontrado).isEmpty(); // Não deve encontrar usuário inativo
    }
}

