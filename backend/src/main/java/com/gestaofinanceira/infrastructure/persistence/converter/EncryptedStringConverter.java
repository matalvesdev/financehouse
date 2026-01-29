package com.gestaofinanceira.infrastructure.persistence.converter;

import com.gestaofinanceira.application.ports.service.CriptografiaPort;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Conversor JPA para criptografia automática de strings sensíveis.
 * 
 * Automaticamente criptografa dados ao salvar no banco e descriptografa
 * ao carregar do banco. Usado para campos sensíveis como descrições
 * de transações, nomes, etc.
 * 
 * Requirements: 10.1
 */
@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    
    private static CriptografiaPort criptografiaPort;
    
    @Autowired
    public void setCriptografiaPort(CriptografiaPort criptografiaPort) {
        EncryptedStringConverter.criptografiaPort = criptografiaPort;
    }
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return attribute;
        }
        
        try {
            return criptografiaPort.criptografarDados(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar dados para o banco", e);
        }
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return dbData;
        }
        
        try {
            return criptografiaPort.descriptografarDados(dbData);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar dados do banco", e);
        }
    }
}