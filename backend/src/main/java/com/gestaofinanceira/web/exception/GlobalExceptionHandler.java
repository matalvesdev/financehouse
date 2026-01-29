package com.gestaofinanceira.web.exception;

import com.gestaofinanceira.web.controller.AuthController;
import com.gestaofinanceira.web.controller.TransacaoController;
import com.gestaofinanceira.web.controller.OrcamentoController;
import com.gestaofinanceira.web.controller.MetaFinanceiraController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manipulador global de exceções para a camada web.
 * 
 * Centraliza o tratamento de erros e padroniza as respostas de erro
 * da API REST, garantindo que os clientes recebam informações
 * consistentes sobre problemas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Trata erros de validação de entrada (Bean Validation).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "Dados de entrada inválidos",
            fieldErrors,
            LocalDateTime.now()
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Trata erros de autenticação (credenciais inválidas).
     */
    @ExceptionHandler(AuthController.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationError(AuthController.AuthenticationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "AUTHENTICATION_ERROR",
            "Credenciais inválidas",
            null,
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Trata erros de conta inativa.
     */
    @ExceptionHandler(AuthController.AccountInactiveException.class)
    public ResponseEntity<ErrorResponse> handleAccountInactiveError(AuthController.AccountInactiveException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "ACCOUNT_INACTIVE",
            "Conta de usuário está inativa",
            null,
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    /**
     * Trata erros de email já existente.
     */
    @ExceptionHandler(AuthController.EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsError(AuthController.EmailAlreadyExistsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "EMAIL_ALREADY_EXISTS",
            "Email já está em uso",
            null,
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    /**
     * Trata erros de validação de dados.
     */
    @ExceptionHandler(AuthController.ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(AuthController.ValidationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            ex.getMessage(),
            null,
            LocalDateTime.now()
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Trata erros de token inválido.
     */
    @ExceptionHandler(AuthController.InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenError(AuthController.InvalidTokenException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_TOKEN",
            "Token inválido ou expirado",
            null,
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Trata erros de validação de dados de transações.
     */
    @ExceptionHandler(TransacaoController.ValidationException.class)
    public ResponseEntity<ErrorResponse> handleTransacaoValidationError(TransacaoController.ValidationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "TRANSACTION_VALIDATION_ERROR",
            ex.getMessage(),
            null,
            LocalDateTime.now()
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Trata erros de regra de negócio de transações.
     */
    @ExceptionHandler(TransacaoController.BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleTransacaoBusinessRuleError(TransacaoController.BusinessRuleException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "TRANSACTION_BUSINESS_RULE_ERROR",
            ex.getMessage(),
            null,
            LocalDateTime.now()
        );
        
        return ResponseEntity.unprocessableEntity().body(errorResponse);
    }
    
    /**
     * Trata erros de validação de dados de orçamentos.
     */
    @ExceptionHandler(OrcamentoController.ValidationException.class)
    public ResponseEntity<ErrorResponse> handleOrcamentoValidationError(OrcamentoController.ValidationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "BUDGET_VALIDATION_ERROR",
            ex.getMessage(),
            null,
            LocalDateTime.now()
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Trata erros de regra de negócio de orçamentos.
     */
    @ExceptionHandler(OrcamentoController.BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleOrcamentoBusinessRuleError(OrcamentoController.BusinessRuleException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "BUDGET_BUSINESS_RULE_ERROR",
            ex.getMessage(),
            null,
            LocalDateTime.now()
        );
        
        return ResponseEntity.unprocessableEntity().body(errorResponse);
    }
    
    /**
     * Trata erros de validação de dados de metas financeiras.
     */
    @ExceptionHandler(MetaFinanceiraController.ValidationException.class)
    public ResponseEntity<ErrorResponse> handleMetaValidationError(MetaFinanceiraController.ValidationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "GOAL_VALIDATION_ERROR",
            ex.getMessage(),
            null,
            LocalDateTime.now()
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Trata erros de regra de negócio de metas financeiras.
     */
    @ExceptionHandler(MetaFinanceiraController.BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleMetaBusinessRuleError(MetaFinanceiraController.BusinessRuleException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "GOAL_BUSINESS_RULE_ERROR",
            ex.getMessage(),
            null,
            LocalDateTime.now()
        );
        
        return ResponseEntity.unprocessableEntity().body(errorResponse);
    }
    
    /**
     * Trata erros gerais não capturados por outros handlers.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_ERROR",
            "Erro interno do servidor",
            null,
            LocalDateTime.now()
        );
        
        // Log do erro para debugging (em produção, usar um logger apropriado)
        ex.printStackTrace();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * DTO para respostas de erro padronizadas.
     */
    public record ErrorResponse(
        String code,
        String message,
        Map<String, String> fieldErrors,
        LocalDateTime timestamp
    ) {}
}