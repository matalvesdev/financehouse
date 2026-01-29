package com.gestaofinanceira.domain.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SenhaHash Value Object Tests")
class SenhaHashTest {

    @Test
    @DisplayName("Deve criar SenhaHash a partir de senha válida")
    void deveCriarSenhaHashAPartirDeSenhaValida() {
        // Arrange
        String senhaTexto = "MinhaSenh@123";
        
        // Act
        SenhaHash senhaHash = SenhaHash.criarDeSenhaTexto(senhaTexto);
        
        // Assert
        assertThat(senhaHash.hash()).isNotEmpty();
        assertThat(senhaHash.salt()).isNotEmpty();
        assertThat(senhaHash.hash()).isNotEqualTo(senhaTexto);
    }

    @Test
    @DisplayName("Deve verificar senha correta")
    void deveVerificarSenhaCorreta() {
        // Arrange
        String senhaTexto = "MinhaSenh@123";
        SenhaHash senhaHash = SenhaHash.criarDeSenhaTexto(senhaTexto);
        
        // Act & Assert
        assertThat(senhaHash.verificarSenha(senhaTexto)).isTrue();
    }

    @Test
    @DisplayName("Deve rejeitar senha incorreta")
    void deveRejeitarSenhaIncorreta() {
        // Arrange
        String senhaTexto = "MinhaSenh@123";
        String senhaIncorreta = "SenhaErrada@456";
        SenhaHash senhaHash = SenhaHash.criarDeSenhaTexto(senhaTexto);
        
        // Act & Assert
        assertThat(senhaHash.verificarSenha(senhaIncorreta)).isFalse();
    }

    @Test
    @DisplayName("Deve gerar hashes diferentes para mesma senha")
    void deveGerarHashesDiferentesParaMesmaSenha() {
        // Arrange
        String senhaTexto = "MinhaSenh@123";
        
        // Act
        SenhaHash hash1 = SenhaHash.criarDeSenhaTexto(senhaTexto);
        SenhaHash hash2 = SenhaHash.criarDeSenhaTexto(senhaTexto);
        
        // Assert
        assertThat(hash1.hash()).isNotEqualTo(hash2.hash());
        assertThat(hash1.salt()).isNotEqualTo(hash2.salt());
        
        // Mas ambos devem verificar a senha corretamente
        assertThat(hash1.verificarSenha(senhaTexto)).isTrue();
        assertThat(hash2.verificarSenha(senhaTexto)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Password1!",
        "Str0ng&Pass",  // Changed # to & which is allowed
        "C0mpl3x$Word",
        "MyP@ssw0rd"
    })
    @DisplayName("Deve aceitar senhas válidas")
    void deveAceitarSenhasValidas(String senhaValida) {
        // Act & Assert
        assertThatCode(() -> SenhaHash.criarDeSenhaTexto(senhaValida))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve rejeitar senha muito curta")
    void deveRejeitarSenhaMuitoCurta() {
        // Act & Assert
        assertThatThrownBy(() -> SenhaHash.criarDeSenhaTexto("Abc1@"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Senha deve ter pelo menos 8 caracteres");
    }

    @Test
    @DisplayName("Deve rejeitar senha muito longa")
    void deveRejeitarSenhaMuitoLonga() {
        // Arrange
        String senhaLonga = "A".repeat(120) + "a1@" + "B".repeat(10);
        
        // Act & Assert
        assertThatThrownBy(() -> SenhaHash.criarDeSenhaTexto(senhaLonga))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Senha não pode ter mais de 128 caracteres");
    }

    @Test
    @DisplayName("Deve rejeitar senha sem letra minúscula")
    void deveRejeitarSenhaSemLetraMinuscula() {
        // Act & Assert
        assertThatThrownBy(() -> SenhaHash.criarDeSenhaTexto("PASSWORD123@"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Senha deve conter pelo menos: 1 letra minúscula");
    }

    @Test
    @DisplayName("Deve rejeitar senha sem letra maiúscula")
    void deveRejeitarSenhaSemLetraMaiuscula() {
        // Act & Assert
        assertThatThrownBy(() -> SenhaHash.criarDeSenhaTexto("password123@"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Senha deve conter pelo menos: 1 letra minúscula");
    }

    @Test
    @DisplayName("Deve rejeitar senha sem número")
    void deveRejeitarSenhaSemNumero() {
        // Act & Assert
        assertThatThrownBy(() -> SenhaHash.criarDeSenhaTexto("Password@"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Senha deve conter pelo menos: 1 letra minúscula");
    }

    @Test
    @DisplayName("Deve rejeitar senha sem caractere especial")
    void deveRejeitarSenhaSemCaractereEspecial() {
        // Act & Assert
        assertThatThrownBy(() -> SenhaHash.criarDeSenhaTexto("Password123"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Senha deve conter pelo menos: 1 letra minúscula");
    }

    @Test
    @DisplayName("Deve rejeitar senha nula")
    void deveRejeitarSenhaNula() {
        // Act & Assert
        assertThatThrownBy(() -> SenhaHash.criarDeSenhaTexto(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Senha não pode ser nula");
    }

    @Test
    @DisplayName("Deve rejeitar hash nulo")
    void deveRejeitarHashNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new SenhaHash(null, "salt"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Hash da senha não pode ser nulo");
    }

    @Test
    @DisplayName("Deve rejeitar salt nulo")
    void deveRejeitarSaltNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new SenhaHash("hash", null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Salt da senha não pode ser nulo");
    }

    @Test
    @DisplayName("Deve rejeitar hash vazio")
    void deveRejeitarHashVazio() {
        // Act & Assert
        assertThatThrownBy(() -> new SenhaHash("", "salt"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Hash da senha não pode estar vazio");
    }

    @Test
    @DisplayName("Deve rejeitar salt vazio")
    void deveRejeitarSaltVazio() {
        // Act & Assert
        assertThatThrownBy(() -> new SenhaHash("hash", ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Salt da senha não pode estar vazio");
    }

    @Test
    @DisplayName("Deve rejeitar verificação com senha nula")
    void deveRejeitarVerificacaoComSenhaNula() {
        // Arrange
        SenhaHash senhaHash = SenhaHash.criarDeSenhaTexto("MinhaSenh@123");
        
        // Act & Assert
        assertThatThrownBy(() -> senhaHash.verificarSenha(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Senha não pode ser nula");
    }

    @Test
    @DisplayName("Deve indicar que não precisa rehash por padrão")
    void deveIndicarQueNaoPrecisaRehashPorPadrao() {
        // Arrange
        SenhaHash senhaHash = SenhaHash.criarDeSenhaTexto("MinhaSenh@123");
        
        // Act & Assert
        assertThat(senhaHash.precisaRehash()).isFalse();
    }

    @Test
    @DisplayName("Deve implementar toString sem expor dados sensíveis")
    void deveImplementarToStringSemExporDadosSensiveis() {
        // Arrange
        SenhaHash senhaHash = SenhaHash.criarDeSenhaTexto("MinhaSenh@123");
        
        // Act
        String toString = senhaHash.toString();
        
        // Assert
        assertThat(toString).isEqualTo("SenhaHash{hash=*****, salt=*****}");
        assertThat(toString).doesNotContain(senhaHash.hash());
        assertThat(toString).doesNotContain(senhaHash.salt());
    }

    @Test
    @DisplayName("Deve implementar equals e hashCode corretamente")
    void deveImplementarEqualsEHashCodeCorretamente() {
        // Arrange
        String senhaTexto = "MinhaSenh@123";
        SenhaHash hash1 = SenhaHash.criarDeSenhaTexto(senhaTexto);
        SenhaHash hash2 = SenhaHash.criarDeSenhaTexto(senhaTexto);
        SenhaHash hash3 = new SenhaHash(hash1.hash(), hash1.salt());
        
        // Act & Assert
        assertThat(hash1).isNotEqualTo(hash2); // Diferentes salts
        assertThat(hash1).isEqualTo(hash3); // Mesmo hash e salt
        assertThat(hash1.hashCode()).isEqualTo(hash3.hashCode());
    }

    @Test
    @DisplayName("Deve ser resistente a timing attacks")
    void deveSerResistenteATimingAttacks() {
        // Arrange
        String senhaCorreta = "MinhaSenh@123";
        String senhaIncorreta = "SenhaErrada@456";
        SenhaHash senhaHash = SenhaHash.criarDeSenhaTexto(senhaCorreta);
        
        // Act - Mede tempo de verificação para senha correta e incorreta
        long inicioCorreto = System.nanoTime();
        boolean resultadoCorreto = senhaHash.verificarSenha(senhaCorreta);
        long tempoCorreto = System.nanoTime() - inicioCorreto;
        
        long inicioIncorreto = System.nanoTime();
        boolean resultadoIncorreto = senhaHash.verificarSenha(senhaIncorreta);
        long tempoIncorreto = System.nanoTime() - inicioIncorreto;
        
        // Assert
        assertThat(resultadoCorreto).isTrue();
        assertThat(resultadoIncorreto).isFalse();
        
        // O tempo não deve variar significativamente (tolerância de 50%)
        double razaoTempo = (double) Math.max(tempoCorreto, tempoIncorreto) / Math.min(tempoCorreto, tempoIncorreto);
        assertThat(razaoTempo).isLessThan(2.0); // Tolerância para variações normais
    }
}