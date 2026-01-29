package com.gestaofinanceira.domain.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Email Value Object Tests")
class EmailTest {

    @Test
    @DisplayName("Deve criar email válido")
    void deveCriarEmailValido() {
        // Arrange & Act
        Email email = new Email("usuario@exemplo.com");
        
        // Assert
        assertThat(email.valor()).isEqualTo("usuario@exemplo.com");
    }

    @Test
    @DisplayName("Deve normalizar email para lowercase")
    void deveNormalizarEmailParaLowercase() {
        // Arrange & Act
        Email email = new Email("USUARIO@EXEMPLO.COM");
        
        // Assert
        assertThat(email.valor()).isEqualTo("usuario@exemplo.com");
    }

    @Test
    @DisplayName("Deve remover espaços em branco")
    void deveRemoverEspacosEmBranco() {
        // Arrange & Act
        Email email = new Email("  usuario@exemplo.com  ");
        
        // Assert
        assertThat(email.valor()).isEqualTo("usuario@exemplo.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "usuario@exemplo.com",
        "test.email@domain.co.uk",
        "user+tag@example.org",
        "user_name@example-domain.com",
        "123@example.com",
        "a@b.co"
    })
    @DisplayName("Deve aceitar emails válidos")
    void deveAceitarEmailsValidos(String emailValido) {
        // Act & Assert
        assertThatCode(() -> new Email(emailValido))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "email-sem-arroba.com",
        "@exemplo.com",
        "usuario@",
        "usuario@.com",
        "usuario@exemplo.",
        "usuario..duplo@exemplo.com",
        "usuario@exemplo..com",
        "usuario@",
        "email com espaço@exemplo.com",
        "email@exemplo com espaço.com"
    })
    @DisplayName("Deve rejeitar emails inválidos")
    void deveRejeitarEmailsInvalidos(String emailInvalido) {
        // Act & Assert
        assertThatThrownBy(() -> new Email(emailInvalido))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Formato de email inválido");
    }

    @Test
    @DisplayName("Deve rejeitar string vazia")
    void deveRejeitarStringVazia() {
        // Act & Assert
        assertThatThrownBy(() -> new Email(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email não pode estar vazio");
    }

    @Test
    @DisplayName("Deve rejeitar string apenas com espaços")
    void deveRejeitarStringApenasComEspacos() {
        // Act & Assert
        assertThatThrownBy(() -> new Email(" "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email não pode estar vazio");
    }

    @Test
    @DisplayName("Deve rejeitar email nulo")
    void deveRejeitarEmailNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new Email(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Email não pode ser nulo");
    }

    @Test
    @DisplayName("Deve rejeitar email vazio")
    void deveRejeitarEmailVazio() {
        // This test is now covered by deveRejeitarStringVazia
        // Keeping for backward compatibility
        assertThatThrownBy(() -> new Email(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email não pode estar vazio");
    }

    @Test
    @DisplayName("Deve rejeitar email muito longo")
    void deveRejeitarEmailMuitoLongo() {
        // Arrange
        String emailLongo = "a".repeat(250) + "@exemplo.com";
        
        // Act & Assert
        assertThatThrownBy(() -> new Email(emailLongo))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email não pode ter mais de 255 caracteres");
    }

    @Test
    @DisplayName("Deve extrair domínio corretamente")
    void deveExtrairDominioCorretamente() {
        // Arrange
        Email email = new Email("usuario@exemplo.com");
        
        // Act & Assert
        assertThat(email.getDominio()).isEqualTo("exemplo.com");
    }

    @Test
    @DisplayName("Deve extrair parte local corretamente")
    void deveExtrairParteLocalCorretamente() {
        // Arrange
        Email email = new Email("usuario@exemplo.com");
        
        // Act & Assert
        assertThat(email.getParteLocal()).isEqualTo("usuario");
    }

    @Test
    @DisplayName("Deve verificar se pertence ao domínio")
    void deveVerificarSePertenceAoDominio() {
        // Arrange
        Email email = new Email("usuario@exemplo.com");
        
        // Act & Assert
        assertThat(email.pertenceAoDominio("exemplo.com")).isTrue();
        assertThat(email.pertenceAoDominio("EXEMPLO.COM")).isTrue();
        assertThat(email.pertenceAoDominio("outro.com")).isFalse();
    }

    @Test
    @DisplayName("Deve implementar toString corretamente")
    void deveImplementarToStringCorretamente() {
        // Arrange
        Email email = new Email("usuario@exemplo.com");
        
        // Act & Assert
        assertThat(email.toString()).isEqualTo("usuario@exemplo.com");
    }

    @Test
    @DisplayName("Deve implementar equals e hashCode corretamente")
    void deveImplementarEqualsEHashCodeCorretamente() {
        // Arrange
        Email email1 = new Email("usuario@exemplo.com");
        Email email2 = new Email("USUARIO@EXEMPLO.COM");
        Email email3 = new Email("outro@exemplo.com");
        
        // Act & Assert
        assertThat(email1).isEqualTo(email2);
        assertThat(email1).isNotEqualTo(email3);
        assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
    }
}