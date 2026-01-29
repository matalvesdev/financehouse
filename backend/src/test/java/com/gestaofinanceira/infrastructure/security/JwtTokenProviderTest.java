package com.gestaofinanceira.infrastructure.security;

import com.gestaofinanceira.application.ports.service.TokenJwtPort;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para JwtTokenProvider.
 * 
 * Valida:
 * - Geração de tokens JWT
 * - Validação de tokens
 * - Extração de claims
 * - Gerenciamento de blacklist
 * - Tratamento de erros
 */
class JwtTokenProviderTest {
    
    private JwtTokenProvider jwtTokenProvider;
    private UsuarioId usuarioId;
    
    @BeforeEach
    void setUp() {
        // Usar chave de teste
        String testSecret = "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdGVzdGluZy0xMjM0NTY3ODkwMTIzNDU2Nzg5MA==";
        jwtTokenProvider = new JwtTokenProvider(testSecret, 900, 604800, "test-issuer");
        usuarioId = new UsuarioId(UUID.randomUUID());
    }
    
    @Test
    void deveGerarTokenDeAcessoValido() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "test@email.com");
        claims.put("nome", "Test User");
        
        // Act
        String token = jwtTokenProvider.gerarTokenAcesso(usuarioId, claims);
        
        // Assert
        assertThat(token).isNotNull().isNotEmpty();
        
        TokenJwtPort.ResultadoValidacaoToken resultado = jwtTokenProvider.validarToken(token);
        assertThat(resultado.valido()).isTrue();
        assertThat(resultado.usuarioId()).isEqualTo(usuarioId);
        assertThat(resultado.tipo()).isEqualTo(TokenJwtPort.TipoToken.ACESSO);
    }
    
    @Test
    void deveGerarTokenDeRefreshValido() {
        // Act
        String token = jwtTokenProvider.gerarTokenRefresh(usuarioId);
        
        // Assert
        assertThat(token).isNotNull().isNotEmpty();
        
        TokenJwtPort.ResultadoValidacaoToken resultado = jwtTokenProvider.validarToken(token);
        assertThat(resultado.valido()).isTrue();
        assertThat(resultado.usuarioId()).isEqualTo(usuarioId);
        assertThat(resultado.tipo()).isEqualTo(TokenJwtPort.TipoToken.REFRESH);
    }
    
    @Test
    void deveExtrairClaimsCorretamente() {
        // Arrange
        Map<String, Object> claimsOriginais = new HashMap<>();
        claimsOriginais.put("email", "test@email.com");
        claimsOriginais.put("nome", "Test User");
        claimsOriginais.put("ativo", true);
        
        String token = jwtTokenProvider.gerarTokenAcesso(usuarioId, claimsOriginais);
        
        // Act
        Map<String, Object> claimsExtraidos = jwtTokenProvider.extrairClaims(token);
        
        // Assert
        assertThat(claimsExtraidos.get("email")).isEqualTo("test@email.com");
        assertThat(claimsExtraidos.get("nome")).isEqualTo("Test User");
        assertThat(claimsExtraidos.get("ativo")).isEqualTo(true);
        assertThat(claimsExtraidos.get("sub")).isEqualTo(usuarioId.valor().toString());
    }
    
    @Test
    void deveExtrairUsuarioIdCorretamente() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        String token = jwtTokenProvider.gerarTokenAcesso(usuarioId, claims);
        
        // Act
        UsuarioId usuarioIdExtraido = jwtTokenProvider.extrairUsuarioId(token);
        
        // Assert
        assertThat(usuarioIdExtraido).isEqualTo(usuarioId);
    }
    
    @Test
    void deveRejeitarTokenInvalido() {
        // Arrange
        String tokenInvalido = "token.invalido.aqui";
        
        // Act
        TokenJwtPort.ResultadoValidacaoToken resultado = jwtTokenProvider.validarToken(tokenInvalido);
        
        // Assert
        assertThat(resultado.valido()).isFalse();
        assertThat(resultado.motivo()).contains("Token malformado");
    }
    
    @Test
    void deveRejeitarTokenVazio() {
        // Act
        TokenJwtPort.ResultadoValidacaoToken resultado = jwtTokenProvider.validarToken("");
        
        // Assert
        assertThat(resultado.valido()).isFalse();
        assertThat(resultado.motivo()).contains("Token não pode ser vazio");
    }
    
    @Test
    void deveRejeitarTokenNulo() {
        // Act
        TokenJwtPort.ResultadoValidacaoToken resultado = jwtTokenProvider.validarToken(null);
        
        // Assert
        assertThat(resultado.valido()).isFalse();
        assertThat(resultado.motivo()).contains("Token não pode ser vazio");
    }
    
    @Test
    void deveInvalidarTokenNaBlacklist() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        String token = jwtTokenProvider.gerarTokenAcesso(usuarioId, claims);
        
        // Verificar que o token é válido inicialmente
        assertThat(jwtTokenProvider.validarToken(token).valido()).isTrue();
        
        // Act
        jwtTokenProvider.invalidarToken(token);
        
        // Assert
        assertThat(jwtTokenProvider.tokenInvalidado(token)).isTrue();
        
        TokenJwtPort.ResultadoValidacaoToken resultado = jwtTokenProvider.validarToken(token);
        assertThat(resultado.valido()).isFalse();
        assertThat(resultado.motivo()).contains("Token foi invalidado");
    }
    
    @Test
    void deveRenovarTokenAcessoComTokenRefresh() {
        // Arrange
        String refreshToken = jwtTokenProvider.gerarTokenRefresh(usuarioId);
        
        // Act
        String novoAccessToken = jwtTokenProvider.renovarTokenAcesso(refreshToken);
        
        // Assert
        assertThat(novoAccessToken).isNotNull().isNotEmpty();
        assertThat(novoAccessToken).isNotEqualTo(refreshToken);
        
        TokenJwtPort.ResultadoValidacaoToken resultado = jwtTokenProvider.validarToken(novoAccessToken);
        assertThat(resultado.valido()).isTrue();
        assertThat(resultado.tipo()).isEqualTo(TokenJwtPort.TipoToken.ACESSO);
        assertThat(resultado.usuarioId()).isEqualTo(usuarioId);
    }
    
    @Test
    void deveRejeitarRenovacaoComTokenAcesso() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        String accessToken = jwtTokenProvider.gerarTokenAcesso(usuarioId, claims);
        
        // Act & Assert
        assertThatThrownBy(() -> jwtTokenProvider.renovarTokenAcesso(accessToken))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("não é um token de refresh");
    }
    
    @Test
    void deveRejeitarRenovacaoComTokenInvalido() {
        // Arrange
        String tokenInvalido = "token.invalido.aqui";
        
        // Act & Assert
        assertThatThrownBy(() -> jwtTokenProvider.renovarTokenAcesso(tokenInvalido))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Token de refresh inválido");
    }
    
    @Test
    void deveDetectarTokenExpirado() {
        // Arrange - criar provider com expiração muito curta
        JwtTokenProvider providerComExpiracaoCurta = new JwtTokenProvider(
            "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdGVzdGluZy0xMjM0NTY3ODkwMTIzNDU2Nzg5MA==",
            -1, // Token já nasce expirado
            604800,
            "test-issuer"
        );
        
        Map<String, Object> claims = new HashMap<>();
        String token = providerComExpiracaoCurta.gerarTokenAcesso(usuarioId, claims);
        
        // Act
        boolean expirado = providerComExpiracaoCurta.tokenExpirado(token);
        TokenJwtPort.ResultadoValidacaoToken resultado = providerComExpiracaoCurta.validarToken(token);
        
        // Assert
        assertThat(expirado).isTrue();
        assertThat(resultado.valido()).isFalse();
        assertThat(resultado.motivo()).contains("Token expirado");
    }
    
    @Test
    void deveObterDataExpiracaoCorreta() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        String token = jwtTokenProvider.gerarTokenAcesso(usuarioId, claims);
        
        // Act
        LocalDateTime dataExpiracao = jwtTokenProvider.obterDataExpiracao(token);
        
        // Assert
        assertThat(dataExpiracao).isAfter(LocalDateTime.now());
        assertThat(dataExpiracao).isBefore(LocalDateTime.now().plusSeconds(1000)); // Dentro do prazo esperado
    }
    
    @Test
    void deveLancarExcecaoParaUsuarioIdNulo() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        
        // Act & Assert
        assertThatThrownBy(() -> jwtTokenProvider.gerarTokenAcesso(null, claims))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("UsuarioId não pode ser nulo");
    }
    
    @Test
    void deveLancarExcecaoParaClaimsNulos() {
        // Act & Assert
        assertThatThrownBy(() -> jwtTokenProvider.gerarTokenAcesso(usuarioId, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Claims não podem ser nulos");
    }
}