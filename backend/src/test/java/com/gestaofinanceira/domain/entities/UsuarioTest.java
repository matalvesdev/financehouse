package com.gestaofinanceira.domain.entities;

import com.gestaofinanceira.domain.valueobjects.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Usuario Entity Tests")
class UsuarioTest {
    
    private Email email;
    private SenhaHash senha;
    private Nome nome;
    
    @BeforeEach
    void setUp() {
        email = new Email("usuario@teste.com");
        senha = SenhaHash.criarDeSenhaTexto("MinhaSenh@123");
        nome = new Nome("João Silva");
    }
    
    @Test
    @DisplayName("Deve criar usuário com dados válidos")
    void deveCriarUsuarioComDadosValidos() {
        Usuario usuario = Usuario.criar(email, senha, nome);
        
        assertThat(usuario.getId()).isNotNull();
        assertThat(usuario.getEmail()).isEqualTo(email);
        assertThat(usuario.getSenha()).isEqualTo(senha);
        assertThat(usuario.getNome()).isEqualTo(nome);
        assertThat(usuario.isAtivo()).isTrue();
        assertThat(usuario.isDadosIniciaisCarregados()).isFalse();
        assertThat(usuario.getCriadoEm()).isNotNull();
    }
    
    @Test
    @DisplayName("Deve marcar dados iniciais carregados")
    void deveMarcarDadosIniciaisCarregados() {
        Usuario usuario = Usuario.criar(email, senha, nome);
        
        assertThat(usuario.podeImportarPlanilha()).isTrue();
        
        usuario.marcarDadosIniciaisCarregados();
        
        assertThat(usuario.isDadosIniciaisCarregados()).isTrue();
        assertThat(usuario.podeImportarPlanilha()).isFalse();
    }
    
    @Test
    @DisplayName("Não deve permitir marcar dados iniciais carregados duas vezes")
    void naoDevePermitirMarcarDadosIniciaisCarregadosDuasVezes() {
        Usuario usuario = Usuario.criar(email, senha, nome);
        
        usuario.marcarDadosIniciaisCarregados();
        
        assertThatThrownBy(() -> usuario.marcarDadosIniciaisCarregados())
            .isInstanceOf(IllegalStateException.class);
    }
    
    @Test
    @DisplayName("Deve atualizar senha")
    void deveAtualizarSenha() {
        Usuario usuario = Usuario.criar(email, senha, nome);
        SenhaHash novaSenha = SenhaHash.criarDeSenhaTexto("NovaSenha@456");
        
        usuario.atualizarSenha(novaSenha);
        
        assertThat(usuario.getSenha()).isEqualTo(novaSenha);
    }
    
    @Test
    @DisplayName("Deve atualizar nome")
    void deveAtualizarNome() {
        Usuario usuario = Usuario.criar(email, senha, nome);
        Nome novoNome = new Nome("Maria Santos");
        
        usuario.atualizarNome(novoNome);
        
        assertThat(usuario.getNome()).isEqualTo(novoNome);
    }
    
    @Test
    @DisplayName("Deve desativar usuário")
    void deveDesativarUsuario() {
        Usuario usuario = Usuario.criar(email, senha, nome);
        
        assertThat(usuario.isAtivo()).isTrue();
        
        usuario.desativar();
        
        assertThat(usuario.isAtivo()).isFalse();
        assertThat(usuario.podeRealizarOperacoes()).isFalse();
    }
    
    @Test
    @DisplayName("Deve reativar usuário")
    void deveReativarUsuario() {
        Usuario usuario = Usuario.criar(email, senha, nome);
        usuario.desativar();
        
        assertThat(usuario.isAtivo()).isFalse();
        
        usuario.reativar();
        
        assertThat(usuario.isAtivo()).isTrue();
        assertThat(usuario.podeRealizarOperacoes()).isTrue();
    }
    
    @Test
    @DisplayName("Não deve permitir operações em usuário inativo")
    void naoDevePermitirOperacoesEmUsuarioInativo() {
        Usuario usuario = Usuario.criar(email, senha, nome);
        usuario.desativar();
        
        SenhaHash novaSenha = SenhaHash.criarDeSenhaTexto("NovaSenha@456");
        Nome novoNome = new Nome("Maria Santos");
        
        assertThatThrownBy(() -> usuario.atualizarSenha(novaSenha))
            .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> usuario.atualizarNome(novoNome))
            .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> usuario.marcarDadosIniciaisCarregados())
            .isInstanceOf(IllegalStateException.class);
    }
    
    @Test
    @DisplayName("Deve verificar senha corretamente")
    void deveVerificarSenhaCorretamente() {
        String senhaTexto = "MinhaSenh@123";
        SenhaHash senhaHash = SenhaHash.criarDeSenhaTexto(senhaTexto);
        Usuario usuario = Usuario.criar(email, senhaHash, nome);
        
        assertThat(usuario.verificarSenha(senhaTexto)).isTrue();
        assertThat(usuario.verificarSenha("SenhaErrada")).isFalse();
    }
    
    @Test
    @DisplayName("Deve identificar novo usuário")
    void deveIdentificarNovoUsuario() {
        Usuario usuario = Usuario.criar(email, senha, nome);
        
        assertThat(usuario.ehNovoUsuario()).isTrue();
        
        usuario.marcarDadosIniciaisCarregados();
        
        assertThat(usuario.ehNovoUsuario()).isFalse();
    }
    
    @Test
    @DisplayName("Deve implementar equals e hashCode corretamente")
    void deveImplementarEqualsEHashCodeCorretamente() {
        UsuarioId id = UsuarioId.gerar();
        Usuario usuario1 = new Usuario(id, email, senha, nome);
        Usuario usuario2 = new Usuario(id, email, senha, nome);
        Usuario usuario3 = new Usuario(UsuarioId.gerar(), email, senha, nome);
        
        assertThat(usuario1).isEqualTo(usuario2);
        assertThat(usuario1).isNotEqualTo(usuario3);
        assertThat(usuario1.hashCode()).isEqualTo(usuario2.hashCode());
    }
    
    @Test
    @DisplayName("Deve rejeitar parâmetros nulos")
    void deveRejeitarParametrosNulos() {
        assertThatThrownBy(() -> Usuario.criar(null, senha, nome))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Usuario.criar(email, null, nome))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Usuario.criar(email, senha, null))
            .isInstanceOf(NullPointerException.class);
    }
}