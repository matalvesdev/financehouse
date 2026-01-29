package com.gestaofinanceira.domain.valueobjects;

import net.jqwik.api.*;
import net.jqwik.api.constraints.BigRange;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for Valor Value Object.
 * Tests universal properties that should hold for all valid inputs.
 */
@Label("Feature: gestao-financeira-domestica, Valor Value Object Properties")
class ValorPropertyTest {

    /**
     * Property: Arithmetic operations consistency
     * For any two valid Valor objects with same currency, arithmetic operations should be consistent
     */
    @Property(tries = 20)
    @Label("Property: Arithmetic operations consistency")
    void arithmeticOperationsShouldBeConsistent(
            @ForAll("validValores") Valor valor1,
            @ForAll("validValores") Valor valor2) {
        
        Assume.that(valor1.moeda().equals(valor2.moeda()));
        
        // Act
        Valor soma = valor1.somar(valor2);
        Valor subtracao = valor1.subtrair(valor2);
        
        // Assert - Addition properties
        assertThat(soma.quantia()).isEqualTo(valor1.quantia().add(valor2.quantia()));
        assertThat(soma.moeda()).isEqualTo(valor1.moeda());
        
        // Assert - Subtraction properties
        assertThat(subtracao.quantia()).isEqualTo(valor1.quantia().subtract(valor2.quantia()));
        assertThat(subtracao.moeda()).isEqualTo(valor1.moeda());
        
        // Assert - Commutativity of addition
        assertThat(valor1.somar(valor2)).isEqualTo(valor2.somar(valor1));
    }

    /**
     * Property: Multiplication consistency
     * For any valid Valor and multiplier, multiplication should preserve currency and scale
     */
    @Property(tries = 20)
    @Label("Property: Multiplication consistency")
    void multiplicationShouldBeConsistent(
            @ForAll("validValores") Valor valor,
            @ForAll @BigRange(min = "0.01", max = "1000.00") BigDecimal multiplicador) {
        
        // Act
        Valor resultado = valor.multiplicar(multiplicador);
        
        // Assert
        assertThat(resultado.moeda()).isEqualTo(valor.moeda());
        assertThat(resultado.quantia().scale()).isLessThanOrEqualTo(2);
        
        BigDecimal expectedQuantia = valor.quantia().multiply(multiplicador)
            .setScale(2, RoundingMode.HALF_UP);
        assertThat(resultado.quantia()).isEqualTo(expectedQuantia);
    }

    /**
     * Property: Division consistency
     * For any valid Valor and non-zero divisor, division should preserve currency and scale
     */
    @Property(tries = 20)
    @Label("Property: Division consistency")
    void divisionShouldBeConsistent(
            @ForAll("validValores") Valor valor,
            @ForAll @BigRange(min = "0.01", max = "1000.00") BigDecimal divisor) {
        
        // Act
        Valor resultado = valor.dividir(divisor);
        
        // Assert
        assertThat(resultado.moeda()).isEqualTo(valor.moeda());
        assertThat(resultado.quantia().scale()).isLessThanOrEqualTo(2);
        
        BigDecimal expectedQuantia = valor.quantia().divide(divisor, 2, RoundingMode.HALF_UP);
        assertThat(resultado.quantia()).isEqualTo(expectedQuantia);
    }

    /**
     * Property: Comparison consistency
     * For any two valid Valor objects with same currency, comparison should be transitive
     */
    @Property(tries = 20)
    @Label("Property: Comparison consistency")
    void comparisonShouldBeConsistent(
            @ForAll("validValores") Valor valor1,
            @ForAll("validValores") Valor valor2,
            @ForAll("validValores") Valor valor3) {
        
        Assume.that(valor1.moeda().equals(valor2.moeda()) && 
                   valor2.moeda().equals(valor3.moeda()));
        
        // Test transitivity: if a > b and b > c, then a > c
        if (valor1.ehMaiorQue(valor2) && valor2.ehMaiorQue(valor3)) {
            assertThat(valor1.ehMaiorQue(valor3)).isTrue();
        }
        
        // Test reflexivity: a == a
        assertThat(valor1.equals(valor1)).isTrue();
        
        // Test symmetry: if a == b, then b == a
        if (valor1.equals(valor2)) {
            assertThat(valor2.equals(valor1)).isTrue();
        }
    }

    /**
     * Property: Currency validation
     * For any two Valor objects with different currencies, operations should fail
     */
    @Property(tries = 20)
    @Label("Property: Currency validation")
    void differentCurrencyOperationsShouldFail(
            @ForAll("validValores") Valor valorBRL,
            @ForAll("validValores") Valor valorUSD) {
        
        Assume.that(!valorBRL.moeda().equals(valorUSD.moeda()));
        
        // Act & Assert
        assertThatThrownBy(() -> valorBRL.somar(valorUSD))
            .isInstanceOf(IllegalArgumentException.class);
            
        assertThatThrownBy(() -> valorBRL.subtrair(valorUSD))
            .isInstanceOf(IllegalArgumentException.class);
            
        assertThatThrownBy(() -> valorBRL.ehMaiorQue(valorUSD))
            .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Property: Zero value behavior
     * For any valid Valor, operations with zero should behave correctly
     */
    @Property(tries = 20)
    @Label("Property: Zero value behavior")
    void zeroValueBehaviorShouldBeCorrect(@ForAll("validValores") Valor valor) {
        // Arrange
        Valor zero = new Valor(BigDecimal.ZERO, valor.moeda());
        
        // Act & Assert
        assertThat(valor.somar(zero)).isEqualTo(valor);
        assertThat(valor.subtrair(zero)).isEqualTo(valor);
        assertThat(zero.somar(valor)).isEqualTo(valor);
        
        if (valor.quantia().compareTo(BigDecimal.ZERO) > 0) {
            assertThat(valor.ehMaiorQue(zero)).isTrue();
            assertThat(zero.ehMenorQue(valor)).isTrue();
        }
    }

    /**
     * Property: Absolute value consistency
     * For any valid Valor, absolute value should always be non-negative
     */
    @Property(tries = 20)
    @Label("Property: Absolute value consistency")
    void absoluteValueShouldBeNonNegative(@ForAll("validValoresIncludingNegative") Valor valor) {
        // Act
        Valor absoluto = valor.absoluto();
        
        // Assert
        assertThat(absoluto.quantia().compareTo(BigDecimal.ZERO)).isGreaterThanOrEqualTo(0);
        assertThat(absoluto.moeda()).isEqualTo(valor.moeda());
        
        if (valor.quantia().compareTo(BigDecimal.ZERO) >= 0) {
            assertThat(absoluto).isEqualTo(valor);
        } else {
            assertThat(absoluto.quantia()).isEqualTo(valor.quantia().negate());
        }
    }

    /**
     * Property: Scale preservation
     * For any valid Valor, the scale should never exceed 2 decimal places
     */
    @Property(tries = 20)
    @Label("Property: Scale preservation")
    void scaleShouldNeverExceedTwoDecimalPlaces(@ForAll("validValores") Valor valor) {
        // Assert
        assertThat(valor.quantia().scale()).isLessThanOrEqualTo(2);
    }

    /**
     * Property: Formatting consistency
     * For any valid Valor, formatting should be consistent and readable
     */
    @Property(tries = 20)
    @Label("Property: Formatting consistency")
    void formattingShouldBeConsistent(@ForAll("validValores") Valor valor) {
        // Act
        String formatted = valor.toString();
        
        // Assert
        assertThat(formatted).isNotEmpty();
        assertThat(formatted).contains(valor.moeda().getSimbolo()); // Use symbol, not name
        assertThat(formatted).contains(valor.quantia().toString());
    }

    // Generators for test data

    @Provide
    Arbitrary<Valor> validValores() {
        Arbitrary<BigDecimal> quantias = Arbitraries.bigDecimals()
            .between(BigDecimal.valueOf(0.01), BigDecimal.valueOf(999999.99))
            .ofScale(2);
            
        Arbitrary<Moeda> moedas = Arbitraries.of(Moeda.class);
        
        return Combinators.combine(quantias, moedas)
            .as(Valor::new);
    }

    @Provide
    Arbitrary<Valor> validValoresIncludingNegative() {
        Arbitrary<BigDecimal> quantias = Arbitraries.bigDecimals()
            .between(BigDecimal.valueOf(-999999.99), BigDecimal.valueOf(999999.99))
            .ofScale(2);
            
        Arbitrary<Moeda> moedas = Arbitraries.of(Moeda.class);
        
        return Combinators.combine(quantias, moedas)
            .as(Valor::new);
    }
}
