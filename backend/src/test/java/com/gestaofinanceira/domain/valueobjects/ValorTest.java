package com.gestaofinanceira.domain.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Valor Value Object Tests")
class ValorTest {

    @Test
    @DisplayName("Deve criar valor válido")
    void deveCriarValorValido() {
        // Arrange & Act
        Valor valor = new Valor(new BigDecimal("100.50"), Moeda.BRL);
        
        // Assert
        assertThat(valor.quantia()).isEqualTo(new BigDecimal("100.50"));
        assertThat(valor.moeda()).isEqualTo(Moeda.BRL);
    }

    @Test
    @DisplayName("Deve normalizar para 2 casas decimais")
    void deveNormalizarPara2CasasDecimais() {
        // Arrange & Act
        Valor valor = new Valor(new BigDecimal("100.5"), Moeda.BRL);
        
        // Assert
        assertThat(valor.quantia()).isEqualTo(new BigDecimal("100.50"));
    }

    @Test
    @DisplayName("Deve criar valor em reais")
    void deveCriarValorEmReais() {
        // Arrange & Act
        Valor valor = Valor.reais(new BigDecimal("100.50"));
        
        // Assert
        assertThat(valor.quantia()).isEqualTo(new BigDecimal("100.50"));
        assertThat(valor.moeda()).isEqualTo(Moeda.BRL);
    }

    @Test
    @DisplayName("Deve criar valor em reais a partir de double")
    void deveCriarValorEmReaisAPartirDeDouble() {
        // Arrange & Act
        Valor valor = Valor.reais(100.50);
        
        // Assert
        assertThat(valor.quantia()).isEqualTo(new BigDecimal("100.50"));
        assertThat(valor.moeda()).isEqualTo(Moeda.BRL);
    }

    @Test
    @DisplayName("Deve criar valor zero")
    void deveCriarValorZero() {
        // Arrange & Act
        Valor valor = Valor.zero(Moeda.BRL);
        
        // Assert
        assertThat(valor.quantia()).isEqualTo(new BigDecimal("0.00"));
        assertThat(valor.moeda()).isEqualTo(Moeda.BRL);
    }

    @Test
    @DisplayName("Deve criar valor zero em reais")
    void deveCriarValorZeroEmReais() {
        // Arrange & Act
        Valor valor = Valor.zeroReais();
        
        // Assert
        assertThat(valor.quantia()).isEqualTo(new BigDecimal("0.00"));
        assertThat(valor.moeda()).isEqualTo(Moeda.BRL);
    }

    @Test
    @DisplayName("Deve rejeitar quantia nula")
    void deveRejeitarQuantiaNula() {
        // Act & Assert
        assertThatThrownBy(() -> new Valor(null, Moeda.BRL))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Quantia não pode ser nula");
    }

    @Test
    @DisplayName("Deve rejeitar moeda nula")
    void deveRejeitarMoedaNula() {
        // Act & Assert
        assertThatThrownBy(() -> new Valor(BigDecimal.TEN, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Moeda não pode ser nula");
    }

    @Test
    @DisplayName("Deve rejeitar mais de 2 casas decimais")
    void deveRejeitarMaisDe2CasasDecimais() {
        // Act & Assert
        assertThatThrownBy(() -> new Valor(new BigDecimal("100.123"), Moeda.BRL))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Valor não pode ter mais de 2 casas decimais");
    }

    @Test
    @DisplayName("Deve somar valores da mesma moeda")
    void deveSomarValoresDaMesmaMoeda() {
        // Arrange
        Valor valor1 = Valor.reais(100.50);
        Valor valor2 = Valor.reais(50.25);
        
        // Act
        Valor resultado = valor1.somar(valor2);
        
        // Assert
        assertThat(resultado.quantia()).isEqualTo(new BigDecimal("150.75"));
        assertThat(resultado.moeda()).isEqualTo(Moeda.BRL);
    }

    @Test
    @DisplayName("Deve subtrair valores da mesma moeda")
    void deveSubtrairValoresDaMesmaMoeda() {
        // Arrange
        Valor valor1 = Valor.reais(100.50);
        Valor valor2 = Valor.reais(50.25);
        
        // Act
        Valor resultado = valor1.subtrair(valor2);
        
        // Assert
        assertThat(resultado.quantia()).isEqualTo(new BigDecimal("50.25"));
        assertThat(resultado.moeda()).isEqualTo(Moeda.BRL);
    }

    @Test
    @DisplayName("Deve multiplicar valor por fator")
    void deveMultiplicarValorPorFator() {
        // Arrange
        Valor valor = Valor.reais(100.00);
        
        // Act
        Valor resultado = valor.multiplicar(new BigDecimal("1.5"));
        
        // Assert
        assertThat(resultado.quantia()).isEqualTo(new BigDecimal("150.00"));
        assertThat(resultado.moeda()).isEqualTo(Moeda.BRL);
    }

    @Test
    @DisplayName("Deve dividir valor por divisor")
    void deveDividirValorPorDivisor() {
        // Arrange
        Valor valor = Valor.reais(100.00);
        
        // Act
        Valor resultado = valor.dividir(new BigDecimal("2"));
        
        // Assert
        assertThat(resultado.quantia()).isEqualTo(new BigDecimal("50.00"));
        assertThat(resultado.moeda()).isEqualTo(Moeda.BRL);
    }

    @Test
    @DisplayName("Deve rejeitar divisão por zero")
    void deveRejeitarDivisaoPorZero() {
        // Arrange
        Valor valor = Valor.reais(100.00);
        
        // Act & Assert
        assertThatThrownBy(() -> valor.dividir(BigDecimal.ZERO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Não é possível dividir por zero");
    }

    @Test
    @DisplayName("Deve calcular valor absoluto")
    void deveCalcularValorAbsoluto() {
        // Arrange
        Valor valor = Valor.reais(-100.50);
        
        // Act
        Valor resultado = valor.absoluto();
        
        // Assert
        assertThat(resultado.quantia()).isEqualTo(new BigDecimal("100.50"));
        assertThat(resultado.moeda()).isEqualTo(Moeda.BRL);
    }

    @Test
    @DisplayName("Deve negar valor")
    void deveNegarValor() {
        // Arrange
        Valor valor = Valor.reais(100.50);
        
        // Act
        Valor resultado = valor.negar();
        
        // Assert
        assertThat(resultado.quantia()).isEqualTo(new BigDecimal("-100.50"));
        assertThat(resultado.moeda()).isEqualTo(Moeda.BRL);
    }

    @Test
    @DisplayName("Deve verificar se é positivo")
    void deveVerificarSeEhPositivo() {
        // Arrange
        Valor positivo = Valor.reais(100.50);
        Valor negativo = Valor.reais(-100.50);
        Valor zero = Valor.zeroReais();
        
        // Act & Assert
        assertThat(positivo.ehPositivo()).isTrue();
        assertThat(negativo.ehPositivo()).isFalse();
        assertThat(zero.ehPositivo()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se é negativo")
    void deveVerificarSeEhNegativo() {
        // Arrange
        Valor positivo = Valor.reais(100.50);
        Valor negativo = Valor.reais(-100.50);
        Valor zero = Valor.zeroReais();
        
        // Act & Assert
        assertThat(positivo.ehNegativo()).isFalse();
        assertThat(negativo.ehNegativo()).isTrue();
        assertThat(zero.ehNegativo()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se é zero")
    void deveVerificarSeEhZero() {
        // Arrange
        Valor positivo = Valor.reais(100.50);
        Valor zero = Valor.zeroReais();
        
        // Act & Assert
        assertThat(positivo.ehZero()).isFalse();
        assertThat(zero.ehZero()).isTrue();
    }

    @Test
    @DisplayName("Deve comparar valores da mesma moeda")
    void deveCompararValoresDaMesmaMoeda() {
        // Arrange
        Valor menor = Valor.reais(50.00);
        Valor maior = Valor.reais(100.00);
        Valor igual = Valor.reais(50.00);
        
        // Act & Assert
        assertThat(menor.comparar(maior)).isNegative();
        assertThat(maior.comparar(menor)).isPositive();
        assertThat(menor.comparar(igual)).isZero();
        
        assertThat(menor.ehMenorQue(maior)).isTrue();
        assertThat(maior.ehMaiorQue(menor)).isTrue();
        assertThat(menor.ehMenorOuIgualA(igual)).isTrue();
        assertThat(maior.ehMaiorOuIgualA(menor)).isTrue();
    }

    @Test
    @DisplayName("Deve rejeitar operações com moedas diferentes")
    void deveRejeitarOperacoesComMoedasDiferentes() {
        // Arrange
        Valor valorBRL = new Valor(BigDecimal.TEN, Moeda.BRL);
        Valor valorUSD = new Valor(BigDecimal.TEN, Moeda.USD);
        
        // Act & Assert
        assertThatThrownBy(() -> valorBRL.somar(valorUSD))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Não é possível operar valores de moedas diferentes");
        
        assertThatThrownBy(() -> valorBRL.subtrair(valorUSD))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Não é possível operar valores de moedas diferentes");
        
        assertThatThrownBy(() -> valorBRL.comparar(valorUSD))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Não é possível operar valores de moedas diferentes");
    }

    @Test
    @DisplayName("Deve formatar valor corretamente")
    void deveFormatarValorCorretamente() {
        // Arrange
        Valor valor = Valor.reais(1234.56);
        
        // Act & Assert
        assertThat(valor.formatado()).isEqualTo("R$ 1234.56");
        assertThat(valor.toString()).isEqualTo("R$ 1234.56");
    }

    @Test
    @DisplayName("Deve implementar equals e hashCode corretamente")
    void deveImplementarEqualsEHashCodeCorretamente() {
        // Arrange
        Valor valor1 = Valor.reais(100.50);
        Valor valor2 = Valor.reais(100.50);
        Valor valor3 = Valor.reais(200.00);
        Valor valor4 = new Valor(new BigDecimal("100.50"), Moeda.USD);
        
        // Act & Assert
        assertThat(valor1).isEqualTo(valor2);
        assertThat(valor1).isNotEqualTo(valor3);
        assertThat(valor1).isNotEqualTo(valor4);
        assertThat(valor1.hashCode()).isEqualTo(valor2.hashCode());
    }
}