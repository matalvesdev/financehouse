package com.gestaofinanceira.application.dto.command;

/**
 * Comando para autenticação de usuário.
 * 
 * Representa a intenção de autenticar um usuário no sistema
 * usando suas credenciais.
 */
public record ComandoAutenticarUsuario(
    String email,
    String senha
) {}