package com.gestaofinanceira.application.dto.command;

import com.gestaofinanceira.domain.valueobjects.UsuarioId;
import org.springframework.web.multipart.MultipartFile;

/**
 * Comando para importação de planilha.
 * 
 * Representa a intenção de importar dados financeiros
 * históricos de uma planilha Excel ou CSV.
 */
public record ComandoImportarPlanilha(
    UsuarioId usuarioId,
    MultipartFile arquivo
) {}