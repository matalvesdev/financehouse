package com.gestaofinanceira.application.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO para requisição de importação de planilha.
 * 
 * Contém o arquivo de planilha (Excel ou CSV) com dados financeiros
 * históricos do usuário para importação inicial.
 */
public record ImportarPlanilhaRequest(
    @NotNull(message = "Arquivo é obrigatório")
    MultipartFile arquivo
) {}