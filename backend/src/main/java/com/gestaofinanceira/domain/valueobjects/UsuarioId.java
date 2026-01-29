package com.gestaofinanceira.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object para representar o identificador único de um usuário.
 */
public record UsuarioId(UUID valor) {
    
    public UsuarioId {
        Objects.requireNonNull(valor, "ID do usuário não pode ser nulo");
    }
    
    /**
     * Gera um novo ID único para usuário.
     */
    public static UsuarioId gerar() {
        return new UsuarioId(UUID.randomUUID());
    }
    
    /**
     * Cria um UsuarioId a partir de uma string UUID.
     */
    public static UsuarioId de(String uuid) {
        Objects.requireNonNull(uuid, "UUID não pode ser nulo");
        try {
            return new UsuarioId(UUID.fromString(uuid));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("UUID inválido: " + uuid, e);
        }
    }
    
    @Override
    public String toString() {
        return valor.toString();
    }
}