package com.gestaofinanceira.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisição de autenticação de usuário.
 * 
 * Contém as credenciais necessárias para autenticação:
 * - Email do usuário
 * - Senha em texto plano (será hasheada durante o processo)
 */
public record AutenticarUsuarioRequest(
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    String email,
    
    @NotBlank(message = "Senha é obrigatória")
    String senha
) {}