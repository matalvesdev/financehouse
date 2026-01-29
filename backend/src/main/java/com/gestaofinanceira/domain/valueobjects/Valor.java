package com.gestaofinanceira.domain.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object para representar valores monetários com moeda.
 * Garante precisão decimal e operações matemáticas seguras.
 */
public record Valor(BigDecimal quantia, Moeda moeda) {
    
    public Valor {
        Objects.requireNonNull(quantia, "Quantia não pode ser nula");
        Objects.requireNonNull(moeda, "Moeda não pode ser nula");
        
        if (quantia.scale() > 2) {
            throw new IllegalArgumentException("Valor não pode ter mais de 2 casas decimais");
        }
        
        // Normaliza para sempre ter 2 casas decimais
        quantia = quantia.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Cria um valor em Real brasileiro.
     */
    public static Valor reais(BigDecimal quantia) {
        return new Valor(quantia, Moeda.BRL);
    }
    
    /**
     * Cria um valor em Real brasileiro a partir de double.
     */
    public static Valor reais(double quantia) {
        return new Valor(BigDecimal.valueOf(quantia), Moeda.BRL);
    }
    
    /**
     * Cria um valor zero na moeda especificada.
     */
    public static Valor zero(Moeda moeda) {
        return new Valor(BigDecimal.ZERO, moeda);
    }
    
    /**
     * Cria um valor zero em Real brasileiro.
     */
    public static Valor zeroReais() {
        return zero(Moeda.BRL);
    }
    
    /**
     * Soma este valor com outro valor da mesma moeda.
     */
    public Valor somar(Valor outro) {
        validarMoeda(outro);
        return new Valor(this.quantia.add(outro.quantia), this.moeda);
    }
    
    /**
     * Subtrai outro valor deste valor (mesma moeda).
     */
    public Valor subtrair(Valor outro) {
        validarMoeda(outro);
        return new Valor(this.quantia.subtract(outro.quantia), this.moeda);
    }
    
    /**
     * Multiplica este valor por um fator.
     */
    public Valor multiplicar(BigDecimal fator) {
        Objects.requireNonNull(fator, "Fator não pode ser nulo");
        BigDecimal resultado = this.quantia.multiply(fator).setScale(2, RoundingMode.HALF_UP);
        return new Valor(resultado, this.moeda);
    }
    
    /**
     * Divide este valor por um divisor.
     */
    public Valor dividir(BigDecimal divisor) {
        Objects.requireNonNull(divisor, "Divisor não pode ser nulo");
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Não é possível dividir por zero");
        }
        return new Valor(this.quantia.divide(divisor, 2, RoundingMode.HALF_UP), this.moeda);
    }
    
    /**
     * Retorna o valor absoluto.
     */
    public Valor absoluto() {
        return new Valor(this.quantia.abs(), this.moeda);
    }
    
    /**
     * Retorna o valor negativo.
     */
    public Valor negar() {
        return new Valor(this.quantia.negate(), this.moeda);
    }
    
    /**
     * Verifica se este valor é positivo.
     */
    public boolean ehPositivo() {
        return quantia.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Verifica se este valor é negativo.
     */
    public boolean ehNegativo() {
        return quantia.compareTo(BigDecimal.ZERO) < 0;
    }
    
    /**
     * Verifica se este valor é zero.
     */
    public boolean ehZero() {
        return quantia.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Compara este valor com outro valor da mesma moeda.
     * @return valor negativo se menor, zero se igual, valor positivo se maior
     */
    public int comparar(Valor outro) {
        validarMoeda(outro);
        return this.quantia.compareTo(outro.quantia);
    }
    
    /**
     * Verifica se este valor é maior que outro.
     */
    public boolean ehMaiorQue(Valor outro) {
        return comparar(outro) > 0;
    }
    
    /**
     * Verifica se este valor é menor que outro.
     */
    public boolean ehMenorQue(Valor outro) {
        return comparar(outro) < 0;
    }
    
    /**
     * Verifica se este valor é maior ou igual a outro.
     */
    public boolean ehMaiorOuIgualA(Valor outro) {
        return comparar(outro) >= 0;
    }
    
    /**
     * Verifica se este valor é menor ou igual a outro.
     */
    public boolean ehMenorOuIgualA(Valor outro) {
        return comparar(outro) <= 0;
    }
    
    /**
     * Valida se outro valor tem a mesma moeda.
     */
    private void validarMoeda(Valor outro) {
        if (!this.moeda.equals(outro.moeda)) {
            throw new IllegalArgumentException(
                String.format("Não é possível operar valores de moedas diferentes: %s e %s", 
                    this.moeda, outro.moeda)
            );
        }
    }
    
    /**
     * Retorna uma representação formatada do valor.
     */
    public String formatado() {
        return String.format("%s %.2f", moeda.getSimbolo(), quantia).replace(',', '.');
    }
    
    @Override
    public String toString() {
        return formatado();
    }
}