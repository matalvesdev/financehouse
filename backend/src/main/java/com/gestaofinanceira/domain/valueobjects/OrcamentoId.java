package com.gestaofinanceira.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object para representar o identificador único de um orçamento.
 */
public record OrcamentoId(UUID valor) {
    
    public OrcamentoId {
        Objects.requireNonNull(valor, "ID do orçamento não pode ser nulo");
    }
    
    /**
     * Gera um novo ID único para orçamento.
     */
    public static OrcamentoId gerar() {
        return new OrcamentoId(UUID.randomUUID());
    }
    
    /**
     * Cria um OrcamentoId a partir de uma string UUID.
     */
    public static OrcamentoId de(String uuid) {
        Objects.requireNonNull(uuid, "UUID não pode ser nulo");
        try {
            return new OrcamentoId(UUID.fromString(uuid));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("UUID inválido: " + uuid, e);
        }
    }
    
    @Override
    public String toString() {
        return valor.toString();
    }
}