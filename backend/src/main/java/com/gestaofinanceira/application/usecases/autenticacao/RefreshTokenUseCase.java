package com.gestaofinanceira.application.usecases.autenticacao;

import com.gestaofinanceira.application.dto.response.AutenticacaoResponse;
import com.gestaofinanceira.application.dto.response.UsuarioResponse;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.application.ports.service.TokenJwtPort;
import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Caso de uso para renovação de tokens de acesso usando refresh token.
 * 
 * Implementa o processo de renovação de sessão:
 * - Validação do refresh token
 * - Verificação de status do usuário
 * - Geração de novo access token
 * - Manutenção do refresh token existente
 * 
 * Requirements: 1.3, 10.2
 */
@Service
@Transactional(readOnly = true)
public class RefreshTokenUseCase {
    
    private final UsuarioRepository usuarioRepository;
    private final TokenJwtPort tokenJwtPort;
    
    // Duração do token de acesso em segundos (15 minutos)
    private static final long ACCESS_TOKEN_DURATION_SECONDS = 15 * 60;
    
    public RefreshTokenUseCase(UsuarioRepository usuarioRepository,
                               TokenJwtPort tokenJwtPort) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, 
            "UsuarioRepository não pode ser nulo");
        this.tokenJwtPort = Objects.requireNonNull(tokenJwtPort, 
            "TokenJwtPort não pode ser nulo");
    }
    
    /**
     * Executa a renovação do token de acesso.
     * 
     * @param refreshToken token de refresh válido
     * @return nova resposta de autenticação com access token renovado
     * @throws IllegalArgumentException se o refresh token é inválido
     * @throws IllegalStateException se o usuário está inativo
     */
    public AutenticacaoResponse executar(String refreshToken) {
        Objects.requireNonNull(refreshToken, "Refresh token não pode ser nulo");
        
        if (refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Refresh token não pode estar vazio");
        }
        
        // 1. Validar refresh token
        var resultadoValidacao = tokenJwtPort.validarToken(refreshToken);
        if (!resultadoValidacao.valido()) {
            throw new IllegalArgumentException("Refresh token inválido: " + resultadoValidacao.motivo());
        }
        
        // 2. Verificar se é um token de refresh
        if (resultadoValidacao.tipo() != TokenJwtPort.TipoToken.REFRESH) {
            throw new IllegalArgumentException("Token fornecido não é um refresh token");
        }
        
        // 3. Verificar se o token não está na blacklist
        if (tokenJwtPort.tokenInvalidado(refreshToken)) {
            throw new IllegalArgumentException("Refresh token foi invalidado");
        }
        
        // 4. Extrair ID do usuário
        UsuarioId usuarioId = resultadoValidacao.usuarioId();
        
        // 5. Buscar usuário no repositório
        Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
            .orElseThrow(() -> new IllegalStateException("Usuário não encontrado"));
        
        // 6. Verificar se o usuário está ativo
        if (!usuario.isAtivo()) {
            throw new IllegalStateException("Conta de usuário está inativa");
        }
        
        // 7. Gerar novo access token
        Map<String, Object> claims = criarClaims(usuario);
        String novoAccessToken = tokenJwtPort.gerarTokenAcesso(usuario.getId(), claims);
        
        // 8. Criar resposta de usuário
        UsuarioResponse usuarioResponse = new UsuarioResponse(
            usuario.getId().valor().toString(),
            usuario.getEmail().valor(),
            usuario.getNome().valor(),
            usuario.getCriadoEm(),
            usuario.isAtivo(),
            usuario.isDadosIniciaisCarregados()
        );
        
        // 9. Retornar resposta com novo access token (mantém o mesmo refresh token)
        return new AutenticacaoResponse(
            novoAccessToken,
            refreshToken, // Mantém o mesmo refresh token
            "Bearer",
            ACCESS_TOKEN_DURATION_SECONDS,
            usuarioResponse
        );
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