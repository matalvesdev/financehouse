package com.gestaofinanceira.web.controller;

import com.gestaofinanceira.application.dto.command.ComandoAutenticarUsuario;
import com.gestaofinanceira.application.dto.command.ComandoCriarUsuario;
import com.gestaofinanceira.application.dto.request.AutenticarUsuarioRequest;
import com.gestaofinanceira.application.dto.request.CriarUsuarioRequest;
import com.gestaofinanceira.application.dto.response.AutenticacaoResponse;
import com.gestaofinanceira.application.dto.response.UsuarioResponse;
import com.gestaofinanceira.application.usecases.autenticacao.AutenticarUsuarioUseCase;
import com.gestaofinanceira.application.usecases.autenticacao.LogoutUsuarioUseCase;
import com.gestaofinanceira.application.usecases.autenticacao.RefreshTokenUseCase;
import com.gestaofinanceira.application.usecases.autenticacao.RegistrarUsuarioUseCase;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Objects;

/**
 * Controller REST para operações de autenticação.
 * 
 * Fornece endpoints para:
 * - Login de usuários
 * - Registro de novos usuários  
 * - Renovação de tokens (refresh)
 * - Logout de usuários
 * 
 * Requirements: 1.1, 1.2, 1.3, 1.4
 */
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    
    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUsuarioUseCase logoutUsuarioUseCase;
    
    public AuthController(AutenticarUsuarioUseCase autenticarUsuarioUseCase,
                         RegistrarUsuarioUseCase registrarUsuarioUseCase,
                         RefreshTokenUseCase refreshTokenUseCase,
                         LogoutUsuarioUseCase logoutUsuarioUseCase) {
        this.autenticarUsuarioUseCase = Objects.requireNonNull(autenticarUsuarioUseCase);
        this.registrarUsuarioUseCase = Objects.requireNonNull(registrarUsuarioUseCase);
        this.refreshTokenUseCase = Objects.requireNonNull(refreshTokenUseCase);
        this.logoutUsuarioUseCase = Objects.requireNonNull(logoutUsuarioUseCase);
    }
    
    /**
     * Endpoint para autenticação de usuários.
     * 
     * @param request dados de login (email e senha)
     * @return tokens JWT e dados do usuário
     */
    @PostMapping("/login")
    public ResponseEntity<AutenticacaoResponse> login(@Valid @RequestBody AutenticarUsuarioRequest request) {
        try {
            ComandoAutenticarUsuario comando = new ComandoAutenticarUsuario(
                request.email(),
                request.senha()
            );
            
            AutenticacaoResponse response = autenticarUsuarioUseCase.executar(comando);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            // Credenciais inválidas - retorna 401
            throw new AuthenticationException("Credenciais inválidas", e);
        } catch (IllegalStateException e) {
            // Conta inativa - retorna 403
            throw new AccountInactiveException("Conta de usuário inativa", e);
        }
    }
    
    /**
     * Endpoint para registro de novos usuários.
     * 
     * @param request dados do novo usuário (nome, email, senha)
     * @return dados do usuário criado
     */
    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody CriarUsuarioRequest request) {
        try {
            ComandoCriarUsuario comando = new ComandoCriarUsuario(
                request.nome(),
                request.email(),
                request.senha()
            );
            
            UsuarioResponse response = registrarUsuarioUseCase.executar(comando);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalStateException e) {
            // Email já em uso - retorna 409
            throw new EmailAlreadyExistsException("Email já está em uso", e);
        } catch (IllegalArgumentException e) {
            // Dados inválidos - retorna 400
            throw new ValidationException("Dados de entrada inválidos", e);
        }
    }
    
    /**
     * Endpoint para renovação de tokens de acesso.
     * 
     * @param request requisição HTTP para extrair o refresh token do header
     * @return novo access token e dados atualizados do usuário
     */
    @PostMapping("/refresh")
    public ResponseEntity<AutenticacaoResponse> refresh(HttpServletRequest request) {
        try {
            String refreshToken = extractRefreshTokenFromRequest(request);
            
            AutenticacaoResponse response = refreshTokenUseCase.executar(refreshToken);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            // Token inválido - retorna 401
            throw new InvalidTokenException("Refresh token inválido", e);
        } catch (IllegalStateException e) {
            // Usuário inativo - retorna 403
            throw new AccountInactiveException("Conta de usuário inativa", e);
        }
    }
    
    /**
     * Endpoint para logout de usuários.
     * 
     * @param request requisição HTTP para extrair tokens dos headers
     * @return confirmação de logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        try {
            String accessToken = extractAccessTokenFromRequest(request);
            String refreshToken = extractRefreshTokenFromRequest(request);
            
            if (refreshToken != null && !refreshToken.trim().isEmpty()) {
                if (accessToken != null && !accessToken.trim().isEmpty()) {
                    // Logout com ambos os tokens
                    logoutUsuarioUseCase.executar(accessToken, refreshToken);
                } else {
                    // Logout apenas com refresh token (access token pode ter expirado)
                    logoutUsuarioUseCase.executarComRefreshToken(refreshToken);
                }
            } else {
                throw new IllegalArgumentException("Refresh token é obrigatório para logout");
            }
            
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            // Token inválido - retorna 401
            throw new InvalidTokenException("Token inválido para logout", e);
        }
    }
    
    /**
     * Extrai o access token do header Authorization.
     */
    private String extractAccessTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    /**
     * Extrai o refresh token do header X-Refresh-Token.
     */
    private String extractRefreshTokenFromRequest(HttpServletRequest request) {
        return request.getHeader("X-Refresh-Token");
    }
    
    // Exception classes para tratamento específico de erros
    
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class AccountInactiveException extends RuntimeException {
        public AccountInactiveException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}