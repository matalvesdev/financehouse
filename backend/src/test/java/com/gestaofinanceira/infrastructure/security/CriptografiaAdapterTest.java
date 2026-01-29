package com.gestaofinanceira.infrastructure.security;

import com.gestaofinanceira.application.ports.service.CriptografiaPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para CriptografiaAdapter.
 * 
 * Valida:
 * - Hashing e verificação de senhas
 * - Criptografia e descriptografia de dados
 * - Geração de tokens seguros
 * - Validação de força de senhas
 * - Tratamento de erros
 */
class CriptografiaAdapterTest {
    
    private CriptografiaAdapter criptografiaAdapter;
    
    @BeforeEach
    void setUp() {
        // Usar chave de teste válida (32 bytes = 256 bits)
        // "abcdefghijklmnopqrstuvwxyz123456" em Base64
        String testKey = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY=";
        criptografiaAdapter = new CriptografiaAdapter(testKey);
    }
    
    @Test
    void deveHashearSenhaCorretamente() {
        // Arrange
        String senha = "MinhaSenh@123";
        
        // Act
        String hash = criptografiaAdapter.hashearSenha(senha);
        
        // Assert
        assertThat(hash).isNotNull().isNotEmpty();
        assertThat(hash).isNotEqualTo(senha);
        assertThat(hash).startsWith("$2a$"); // BCrypt prefix
    }
    
    @Test
    void deveVerificarSenhaCorreta() {
        // Arrange
        String senha = "MinhaSenh@123";
        String hash = criptografiaAdapter.hashearSenha(senha);
        
        // Act
        boolean valida = criptografiaAdapter.verificarSenha(senha, hash);
        
        // Assert
        assertThat(valida).isTrue();
    }
    
    @Test
    void deveRejeitarSenhaIncorreta() {
        // Arrange
        String senhaCorreta = "MinhaSenh@123";
        String senhaIncorreta = "SenhaErrada456";
        String hash = criptografiaAdapter.hashearSenha(senhaCorreta);
        
        // Act
        boolean valida = criptografiaAdapter.verificarSenha(senhaIncorreta, hash);
        
        // Assert
        assertThat(valida).isFalse();
    }
    
    @Test
    void deveGerarHashesDiferentesParaMesmaSenha() {
        // Arrange
        String senha = "MinhaSenh@123";
        
        // Act
        String hash1 = criptografiaAdapter.hashearSenha(senha);
        String hash2 = criptografiaAdapter.hashearSenha(senha);
        
        // Assert
        assertThat(hash1).isNotEqualTo(hash2); // BCrypt usa salt aleatório
        assertThat(criptografiaAdapter.verificarSenha(senha, hash1)).isTrue();
        assertThat(criptografiaAdapter.verificarSenha(senha, hash2)).isTrue();
    }
    
    @Test
    void deveCriptografarEDescriptografarDados() {
        // Arrange
        String dadosOriginais = "Dados sensíveis para criptografar";
        
        // Act
        String dadosCriptografados = criptografiaAdapter.criptografarDados(dadosOriginais);
        String dadosDescriptografados = criptografiaAdapter.descriptografarDados(dadosCriptografados);
        
        // Assert
        assertThat(dadosCriptografados).isNotNull().isNotEmpty();
        assertThat(dadosCriptografados).isNotEqualTo(dadosOriginais);
        assertThat(dadosDescriptografados).isEqualTo(dadosOriginais);
    }
    
    @Test
    void deveGerarCriptografiasDiferentesParaMesmosDados() {
        // Arrange
        String dados = "Dados para criptografar";
        
        // Act
        String criptografia1 = criptografiaAdapter.criptografarDados(dados);
        String criptografia2 = criptografiaAdapter.criptografarDados(dados);
        
        // Assert
        assertThat(criptografia1).isNotEqualTo(criptografia2); // IV aleatório
        assertThat(criptografiaAdapter.descriptografarDados(criptografia1)).isEqualTo(dados);
        assertThat(criptografiaAdapter.descriptografarDados(criptografia2)).isEqualTo(dados);
    }
    
    @Test
    void deveGerarTokenSeguroComTamanhoCorreto() {
        // Arrange
        int tamanho = 32;
        
        // Act
        String token = criptografiaAdapter.gerarTokenSeguro(tamanho);
        
        // Assert
        assertThat(token).isNotNull().isNotEmpty();
        // Token Base64 URL-safe sem padding
        assertThat(token).matches("^[A-Za-z0-9_-]+$");
    }
    
    @Test
    void deveGerarTokensUnicos() {
        // Arrange
        int tamanho = 16;
        
        // Act
        String token1 = criptografiaAdapter.gerarTokenSeguro(tamanho);
        String token2 = criptografiaAdapter.gerarTokenSeguro(tamanho);
        
        // Assert
        assertThat(token1).isNotEqualTo(token2);
    }
    
    @Test
    void deveGerarSaltsUnicos() {
        // Act
        String salt1 = criptografiaAdapter.gerarSalt();
        String salt2 = criptografiaAdapter.gerarSalt();
        
        // Assert
        assertThat(salt1).isNotNull().isNotEmpty();
        assertThat(salt2).isNotNull().isNotEmpty();
        assertThat(salt1).isNotEqualTo(salt2);
    }
    
    @Test
    void deveValidarSenhaForte() {
        // Arrange
        String senhaForte = "MinhaSenh@Forte123!";
        
        // Act
        CriptografiaPort.ResultadoValidacaoSenha resultado = criptografiaAdapter.validarForcaSenha(senhaForte);
        
        // Assert
        assertThat(resultado.valida()).isTrue();
        assertThat(resultado.nivel()).isIn(
            CriptografiaPort.NivelForca.FORTE,
            CriptografiaPort.NivelForca.MUITO_FORTE
        );
        assertThat(resultado.pontuacao()).isGreaterThan(70);
    }
    
    @Test
    void deveRejeitarSenhaFraca() {
        // Arrange
        String senhaFraca = "123";
        
        // Act
        CriptografiaPort.ResultadoValidacaoSenha resultado = criptografiaAdapter.validarForcaSenha(senhaFraca);
        
        // Assert
        assertThat(resultado.valida()).isFalse();
        assertThat(resultado.nivel()).isIn(
            CriptografiaPort.NivelForca.MUITO_FRACA,
            CriptografiaPort.NivelForca.FRACA
        );
        assertThat(resultado.sugestoesMelhoria()).isNotEmpty();
    }
    
    @Test
    void deveDetectarPadroesComuns() {
        // Arrange
        String senhaComPadrao = "password123";
        
        // Act
        CriptografiaPort.ResultadoValidacaoSenha resultado = criptografiaAdapter.validarForcaSenha(senhaComPadrao);
        
        // Assert
        assertThat(resultado.sugestoesMelhoria())
            .anyMatch(sugestao -> sugestao.contains("padrões comuns"));
    }
    
    @Test
    void deveDetectarRepeticoesExcessivas() {
        // Arrange
        String senhaComRepeticoes = "aaaaBBBB1111!";
        
        // Act
        CriptografiaPort.ResultadoValidacaoSenha resultado = criptografiaAdapter.validarForcaSenha(senhaComRepeticoes);
        
        // Assert
        assertThat(resultado.sugestoesMelhoria())
            .anyMatch(sugestao -> sugestao.contains("repetir o mesmo caractere"));
    }
    
    @Test
    void deveFornecerSugestoesParaSenhaSemMaiusculas() {
        // Arrange
        String senha = "minhasenha123!";
        
        // Act
        CriptografiaPort.ResultadoValidacaoSenha resultado = criptografiaAdapter.validarForcaSenha(senha);
        
        // Assert
        assertThat(resultado.sugestoesMelhoria())
            .anyMatch(sugestao -> sugestao.contains("maiúscula"));
    }
    
    @Test
    void deveFornecerSugestoesParaSenhaSemMinusculas() {
        // Arrange
        String senha = "MINHASENHA123!";
        
        // Act
        CriptografiaPort.ResultadoValidacaoSenha resultado = criptografiaAdapter.validarForcaSenha(senha);
        
        // Assert
        assertThat(resultado.sugestoesMelhoria())
            .anyMatch(sugestao -> sugestao.contains("minúscula"));
    }
    
    @Test
    void deveFornecerSugestoesParaSenhaSemNumeros() {
        // Arrange
        String senha = "MinhaSenh@Forte!";
        
        // Act
        CriptografiaPort.ResultadoValidacaoSenha resultado = criptografiaAdapter.validarForcaSenha(senha);
        
        // Assert
        assertThat(resultado.sugestoesMelhoria())
            .anyMatch(sugestao -> sugestao.contains("número"));
    }
    
    @Test
    void deveFornecerSugestoesParaSenhaSemCaracteresEspeciais() {
        // Arrange
        String senha = "MinhaSenh4Forte";
        
        // Act
        CriptografiaPort.ResultadoValidacaoSenha resultado = criptografiaAdapter.validarForcaSenha(senha);
        
        // Assert
        assertThat(resultado.sugestoesMelhoria())
            .anyMatch(sugestao -> sugestao.contains("caractere especial"));
    }
    
    @Test
    void deveLancarExcecaoParaSenhaNula() {
        // Act & Assert
        assertThatThrownBy(() -> criptografiaAdapter.hashearSenha(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Senha não pode ser nula");
    }
    
    @Test
    void deveLancarExcecaoParaSenhaVazia() {
        // Act & Assert
        assertThatThrownBy(() -> criptografiaAdapter.hashearSenha(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Senha não pode ser nula ou vazia");
    }
    
    @Test
    void deveLancarExcecaoParaDadosNulos() {
        // Act & Assert
        assertThatThrownBy(() -> criptografiaAdapter.criptografarDados(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Dados não podem ser nulos");
    }
    
    @Test
    void deveLancarExcecaoParaDadosVazios() {
        // Act & Assert
        assertThatThrownBy(() -> criptografiaAdapter.criptografarDados(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Dados não podem ser nulos ou vazios");
    }
    
    @Test
    void deveLancarExcecaoParaTokenTamanhoInvalido() {
        // Act & Assert
        assertThatThrownBy(() -> criptografiaAdapter.gerarTokenSeguro(0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Tamanho do token deve estar entre 1 e 1024");
        
        assertThatThrownBy(() -> criptografiaAdapter.gerarTokenSeguro(1025))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Tamanho do token deve estar entre 1 e 1024");
    }
    
    @Test
    void deveRetornarFalsoParaVerificacaoComParametrosNulos() {
        // Act & Assert
        assertThat(criptografiaAdapter.verificarSenha(null, "hash")).isFalse();
        assertThat(criptografiaAdapter.verificarSenha("senha", null)).isFalse();
        assertThat(criptografiaAdapter.verificarSenha(null, null)).isFalse();
    }
}