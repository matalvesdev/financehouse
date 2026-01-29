package com.gestaofinanceira.application.usecases.autenticacao;

import com.gestaofinanceira.application.dto.command.ComandoAutenticarUsuario;
import com.gestaofinanceira.application.dto.response.AutenticacaoResponse;
import com.gestaofinanceira.application.dto.response.UsuarioResponse;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.application.ports.service.CriptografiaPort;
import com.gestaofinanceira.application.ports.service.TokenJwtPort;
import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.Email;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Caso de uso para autenticação de usuários no sistema.
 * 
 * Implementa o processo completo de autenticação:
 * - Validação de credenciais
 * - Verificação de status do usuário
 * - Geração de tokens JWT (access + refresh)
 * - Retorno de dados de sessão
 * 
 * Requirements: 1.1, 1.2, 10.2
 */
@Service
@Transactional(readOnly = true)
public class AutenticarUsuarioUseCase {
    
    private final UsuarioRepository usuarioRepository;
    private final CriptografiaPort criptografiaPort;
    private final TokenJwtPort tokenJwtPort;
    
    // Duração do token de acesso em segundos (15 minutos)
    private static final long ACCESS_TOKEN_DURATION_SECONDS = 15 * 60;
    
    public AutenticarUsuarioUseCase(UsuarioRepository usuarioRepository,
                                    CriptografiaPort criptografiaPort,
                                    TokenJwtPort tokenJwtPort) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, 
            "UsuarioRepository não pode ser nulo");
        this.criptografiaPort = Objects.requireNonNull(criptografiaPort, 
            "CriptografiaPort não pode ser nulo");
        this.tokenJwtPort = Objects.requireNonNull(tokenJwtPort, 
            "TokenJwtPort não pode ser nulo");
    }
    
    /**
     * Executa a autenticação de um usuário.
     * 
     * @param comando credenciais do usuário
     * @return dados de autenticação com tokens JWT
     * @throws IllegalArgumentException se as credenciais são inválidas
     * @throws IllegalStateException se o usuário está inativo
     */
    public AutenticacaoResponse executar(ComandoAutenticarUsuario comando) {
        Objects.requireNonNull(comando, "Comando não pode ser nulo");
        
        // 1. Validar dados de entrada
        validarDadosEntrada(comando);
        
        // 2. Buscar usuário por email
        Email email = new Email(comando.email());
        Usuario usuario = usuarioRepository.buscarPorEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));
        
        // 3. Verificar se o usuário está ativo
        if (!usuario.isAtivo()) {
            throw new IllegalStateException("Conta de usuário está inativa");
        }
        
        // 4. Verificar senha
        if (!usuario.verificarSenha(comando.senha())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }
        
        // 5. Gerar tokens JWT
        Map<String, Object> claims = criarClaims(usuario);
        String accessToken = tokenJwtPort.gerarTokenAcesso(usuario.getId(), claims);
        String refreshToken = tokenJwtPort.gerarTokenRefresh(usuario.getId());
        
        // 6. Criar resposta de usuário
        UsuarioResponse usuarioResponse = new UsuarioResponse(
            usuario.getId().valor().toString(),
            usuario.getEmail().valor(),
            usuario.getNome().valor(),
            usuario.getCriadoEm(),
            usuario.isAtivo(),
            usuario.isDadosIniciaisCarregados()
        );
        
        // 7. Retornar resposta de autenticação
        return new AutenticacaoResponse(
            accessToken,
            refreshToken,
            "Bearer",
            ACCESS_TOKEN_DURATION_SECONDS,
            usuarioResponse
        );
    }
    
    /**
     * Valida os dados de entrada do comando.
     */
    private void validarDadosEntrada(ComandoAutenticarUsuario comando) {
        if (comando.email() == null || comando.email().trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        
        if (comando.senha() == null || comando.senha().isEmpty()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
    }
    
    /**
     * Cria claims adicionais para o token JWT.
     */
    private Map<String, Object> criarClaims(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", usuario.getEmail().valor());
        claims.put("nome", usuario.getNome().valor());
        claims.put("ativo", usuario.isAtivo());
        claims.put("dadosIniciaisCarregados", usuario.isDadosIniciaisCarregados());
        claims.put("criadoEm", usuario.getCriadoEm().toEpochSecond(ZoneOffset.UTC));
        return claims;
    }
}