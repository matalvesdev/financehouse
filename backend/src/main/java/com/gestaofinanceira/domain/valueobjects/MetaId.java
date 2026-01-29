package com.gestaofinanceira.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object para representar o identificador único de uma meta financeira.
 */
public record MetaId(UUID valor) {
    
    public MetaId {
        Objects.requireNonNull(valor, "ID da meta não pode ser nulo");
    }
    
    /**
     * Gera um novo ID único para meta financeira.
     */
    public static MetaId gerar() {
        return new MetaId(UUID.randomUUID());
    }
    
    /**
     * Cria um MetaId a partir de uma string UUID.
     */
    public static MetaId de(String uuid) {
        Objects.requireNonNull(uuid, "UUID não pode ser nulo");
        try {
            return new MetaId(UUID.fromString(uuid));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("UUID inválido: " + uuid, e);
        }
    }
    
    @Override
    public String toString() {
        return valor.toString();
    }
}