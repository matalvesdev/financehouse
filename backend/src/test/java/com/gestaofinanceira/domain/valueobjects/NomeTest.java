package com.gestaofinanceira.domain.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Nome Value Object Tests")
class NomeTest {

    @Test
    @DisplayName("Deve criar nome válido")
    void deveCriarNomeValido() {
        // Arrange & Act
        Nome nome = new Nome("João Silva");
        
        // Assert
        assertThat(nome.valor()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Deve rejeitar nomes com espaços nas extremidades")
    void deveRejeitarNomesComEspacosNasExtremidades() {
        // Act & Assert
        assertThatThrownBy(() -> new Nome("  João Silva  "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Nome não pode começar ou terminar com espaço, hífen ou apostrofe");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "João",
        "Maria Silva",
        "José da Silva",
        "Ana-Paula",
        "Jean-Claude",
        "Mary O'Connor",
        "José María",
        "François",
        "Ângela",
        "João Pedro dos Santos"
    })
    @DisplayName("Deve aceitar nomes válidos")
    void deveAceitarNomesValidos(String nomeValido) {
        // Act & Assert
        assertThatCode(() -> new Nome(nomeValido))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve rejeitar nome nulo")
    void deveRejeitarNomeNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new Nome(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Nome não pode ser nulo");
    }

    @Test
    @DisplayName("Deve rejeitar nome vazio")
    void deveRejeitarNomeVazio() {
        // Act & Assert
        assertThatThrownBy(() -> new Nome(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Nome não pode estar vazio");
    }

    @Test
    @DisplayName("Deve rejeitar nome muito curto")
    void deveRejeitarNomeMuitoCurto() {
        // Act & Assert
        assertThatThrownBy(() -> new Nome("A"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Nome deve ter pelo menos 2 caracteres");
    }

    @Test
    @DisplayName("Deve rejeitar nome muito longo")
    void deveRejeitarNomeMuitoLongo() {
        // Arrange
        String nomeLongo = "A".repeat(101);
        
        // Act & Assert
        assertThatThrownBy(() -> new Nome(nomeLongo))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Nome não pode ter mais de 100 caracteres");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "João123",
        "Maria@Silva",
        "José#Santos",
        "Ana$Paula",
        "João%Silva",
        "Maria&José"
    })
    @DisplayName("Deve rejeitar nomes com caracteres inválidos")
    void deveRejeitarNomesComCaracteresInvalidos(String nomeInvalido) {
        // Act & Assert
        assertThatThrownBy(() -> new Nome(nomeInvalido))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Nome deve conter apenas letras, espaços, hífens e apostrofes");
    }

    @Test
    @DisplayName("Deve rejeitar nome com espaços consecutivos")
    void deveRejeitarNomeComEspacosConsecutivos() {
        // Act & Assert
        assertThatThrownBy(() -> new Nome("João  Silva"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Nome não pode conter espaços consecutivos");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        " João Silva",
        "João Silva ",
        "-João Silva",
        "João Silva-",
        "'João Silva",
        "João Silva'"
    })
    @DisplayName("Deve rejeitar nome que começa ou termina com caracteres inválidos")
    void deveRejeitarNomeQueComecaOuTerminaComCaracteresInvalidos(String nomeInvalido) {
        // Act & Assert
        assertThatThrownBy(() -> new Nome(nomeInvalido))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Nome não pode começar ou terminar com espaço, hífen ou apostrofe");
    }

    @Test
    @DisplayName("Deve extrair primeiro nome")
    void deveExtrairPrimeiroNome() {
        // Arrange
        Nome nome = new Nome("João Pedro Silva");
        
        // Act & Assert
        assertThat(nome.getPrimeiroNome()).isEqualTo("João");
    }

    @Test
    @DisplayName("Deve extrair último nome")
    void deveExtrairUltimoNome() {
        // Arrange
        Nome nomeComposto = new Nome("João Pedro Silva");
        Nome nomeSimples = new Nome("João");
        
        // Act & Assert
        assertThat(nomeComposto.getUltimoNome()).isEqualTo("Silva");
        assertThat(nomeSimples.getUltimoNome()).isEqualTo("João");
    }

    @Test
    @DisplayName("Deve extrair nomes do meio")
    void deveExtrairNomesDoMeio() {
        // Arrange
        Nome nomeComMeio = new Nome("João Pedro Santos Silva");
        Nome nomeSemMeio = new Nome("João Silva");
        Nome nomeSimples = new Nome("João");
        
        // Act & Assert
        assertThat(nomeComMeio.getNomesDoMeio()).isEqualTo("Pedro Santos");
        assertThat(nomeSemMeio.getNomesDoMeio()).isEmpty();
        assertThat(nomeSimples.getNomesDoMeio()).isEmpty();
    }

    @Test
    @DisplayName("Deve formatar nome corretamente")
    void deveFormatarNomeCorretamente() {
        // Arrange
        Nome nome = new Nome("joão pedro da silva");
        
        // Act
        String nomeFormatado = nome.getFormatado();
        
        // Assert
        assertThat(nomeFormatado).isEqualTo("João Pedro da Silva");
    }

    @Test
    @DisplayName("Deve tratar preposições e artigos na formatação")
    void deveTratarPreposicoesEArtigosNaFormatacao() {
        // Arrange
        Nome nome = new Nome("JOSÉ DA SILVA DOS SANTOS");
        
        // Act
        String nomeFormatado = nome.getFormatado();
        
        // Assert
        assertThat(nomeFormatado).isEqualTo("José da Silva dos Santos");
    }

    @Test
    @DisplayName("Deve extrair iniciais corretamente")
    void deveExtrairIniciaisCorretamente() {
        // Arrange
        Nome nome = new Nome("João Pedro da Silva");
        
        // Act
        String iniciais = nome.getIniciais();
        
        // Assert
        assertThat(iniciais).isEqualTo("JPS"); // Não inclui preposições
    }

    @Test
    @DisplayName("Deve verificar se nome é composto")
    void deveVerificarSeNomeEhComposto() {
        // Arrange
        Nome nomeComposto = new Nome("João Silva");
        Nome nomeSimples = new Nome("João");
        
        // Act & Assert
        assertThat(nomeComposto.ehComposto()).isTrue();
        assertThat(nomeSimples.ehComposto()).isFalse();
    }

    @Test
    @DisplayName("Deve contar quantidade de palavras")
    void deveContarQuantidadeDePalavras() {
        // Arrange
        Nome nomeSimples = new Nome("João");
        Nome nomeComposto = new Nome("João Pedro Silva");
        Nome nomeCompleto = new Nome("João Pedro dos Santos Silva");
        
        // Act & Assert
        assertThat(nomeSimples.getQuantidadePalavras()).isEqualTo(1);
        assertThat(nomeComposto.getQuantidadePalavras()).isEqualTo(3);
        assertThat(nomeCompleto.getQuantidadePalavras()).isEqualTo(5);
    }

    @Test
    @DisplayName("Deve implementar toString com formatação")
    void deveImplementarToStringComFormatacao() {
        // Arrange
        Nome nome = new Nome("joão silva");
        
        // Act & Assert
        assertThat(nome.toString()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Deve implementar equals e hashCode corretamente")
    void deveImplementarEqualsEHashCodeCorretamente() {
        // Arrange
        Nome nome1 = new Nome("João Silva");
        Nome nome2 = new Nome("  João Silva  ");
        Nome nome3 = new Nome("Maria Silva");
        
        // Act & Assert
        assertThat(nome1).isEqualTo(nome2);
        assertThat(nome1).isNotEqualTo(nome3);
        assertThat(nome1.hashCode()).isEqualTo(nome2.hashCode());
    }

    @Test
    @DisplayName("Deve aceitar nomes com acentos e caracteres especiais válidos")
    void deveAceitarNomesComAcentosECaracteresEspeciaisValidos() {
        // Arrange & Act & Assert
        assertThatCode(() -> new Nome("José María"))
            .doesNotThrowAnyException();
        
        assertThatCode(() -> new Nome("François"))
            .doesNotThrowAnyException();
        
        assertThatCode(() -> new Nome("Ângela"))
            .doesNotThrowAnyException();
        
        assertThatCode(() -> new Nome("Müller"))
            .doesNotThrowAnyException();
    }
}