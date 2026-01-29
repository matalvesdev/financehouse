package com.gestaofinanceira.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object para representar o identificador único de uma transação.
 */
public record TransacaoId(UUID valor) {
    
    public TransacaoId {
        Objects.requireNonNull(valor, "ID da transação não pode ser nulo");
    }
    
    /**
     * Gera um novo ID único para transação.
     */
    public static TransacaoId gerar() {
        return new TransacaoId(UUID.randomUUID());
    }
    
    /**
     * Cria um TransacaoId a partir de uma string UUID.
     */
    public static TransacaoId de(String uuid) {
        Objects.requireNonNull(uuid, "UUID não pode ser nulo");
        try {
            return new TransacaoId(UUID.fromString(uuid));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("UUID inválido: " + uuid, e);
        }
    }
    
    @Override
    public String toString() {
        return valor.toString();
    }
}