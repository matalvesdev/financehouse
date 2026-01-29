package com.gestaofinanceira.application.dto.command;

/**
 * Comando para criação de usuário.
 * 
 * Representa a intenção de criar um novo usuário no sistema,
 * contendo todos os dados necessários validados.
 */
public record ComandoCriarUsuario(
    String nome,
    String email,
    String senha
) {}