package com.gestaofinanceira.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestaofinanceira.application.dto.request.AutenticarUsuarioRequest;
import com.gestaofinanceira.application.dto.request.CriarUsuarioRequest;
import com.gestaofinanceira.application.dto.response.AutenticacaoResponse;
import com.gestaofinanceira.application.dto.response.UsuarioResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para AuthController.
 * 
 * Valida contratos de API e responses para operações de autenticação.
 * Requirements: 1.1, 1.2, 1.3, 1.4
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void deveRegistrarUsuarioComSucesso() throws Exception {
        // Arrange
        CriarUsuarioRequest request = new CriarUsuarioRequest(
            "João Silva",
            "joao@exemplo.com",
            "MinhaSenh@123"
        );
        
        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao@exemplo.com"))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.dadosIniciaisCarregados").value(false))
                .andReturn();
        
        // Verificar resposta detalhada
        String responseContent = result.getResponse().getContentAsString();
        UsuarioResponse response = objectMapper.readValue(responseContent, UsuarioResponse.class);
        
        assertThat(response.id()).isNotNull();
        assertThat(response.nome()).isEqualTo("João Silva");
        assertThat(response.email()).isEqualTo("joao@exemplo.com");
        assertThat(response.criadoEm()).isNotNull();
    }
    
    @Test
    void deveRejeitarRegistroComEmailInvalido() throws Exception {
        // Arrange
        CriarUsuarioRequest request = new CriarUsuarioRequest(
            "João Silva",
            "email-invalido",
            "MinhaSenh@123"
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }
    
    @Test
    void deveRejeitarRegistroComSenhaFraca() throws Exception {
        // Arrange
        CriarUsuarioRequest request = new CriarUsuarioRequest(
            "João Silva",
            "joao@exemplo.com",
            "123" // Senha muito fraca
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.senha").exists());
    }
    
    @Test
    void deveRejeitarRegistroComEmailDuplicado() throws Exception {
        // Arrange - Primeiro registro
        CriarUsuarioRequest primeiroRequest = new CriarUsuarioRequest(
            "João Silva",
            "duplicado@exemplo.com",
            "MinhaSenh@123"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(primeiroRequest)))
                .andExpect(status().isCreated());
        
        // Arrange - Segundo registro com mesmo email
        CriarUsuarioRequest segundoRequest = new CriarUsuarioRequest(
            "Maria Santos",
            "duplicado@exemplo.com",
            "OutraSenh@456"
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(segundoRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
    }
    
    @Test
    void deveAutenticarUsuarioComCredenciaisValidas() throws Exception {
        // Arrange - Registrar usuário primeiro
        CriarUsuarioRequest registroRequest = new CriarUsuarioRequest(
            "Maria Santos",
            "maria@exemplo.com",
            "MinhaSenh@123"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroRequest)))
                .andExpect(status().isCreated());
        
        // Arrange - Credenciais de login
        AutenticarUsuarioRequest loginRequest = new AutenticarUsuarioRequest(
            "maria@exemplo.com",
            "MinhaSenh@123"
        );
        
        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").exists())
                .andExpect(jsonPath("$.usuario.email").value("maria@exemplo.com"))
                .andReturn();
        
        // Verificar resposta detalhada
        String responseContent = result.getResponse().getContentAsString();
        AutenticacaoResponse response = objectMapper.readValue(responseContent, AutenticacaoResponse.class);
        
        assertThat(response.accessToken()).isNotNull().isNotEmpty();
        assertThat(response.refreshToken()).isNotNull().isNotEmpty();
        assertThat(response.usuario().email()).isEqualTo("maria@exemplo.com");
    }
    
    @Test
    void deveRejeitarAutenticacaoComCredenciaisInvalidas() throws Exception {
        // Arrange
        AutenticarUsuarioRequest request = new AutenticarUsuarioRequest(
            "inexistente@exemplo.com",
            "senhaErrada"
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_ERROR"));
    }
    
    @Test
    void deveRejeitarAutenticacaoComDadosInvalidos() throws Exception {
        // Arrange
        AutenticarUsuarioRequest request = new AutenticarUsuarioRequest(
            "", // Email vazio
            ""  // Senha vazia
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.senha").exists());
    }
    
    @Test
    void deveRenovarTokenComRefreshTokenValido() throws Exception {
        // Arrange - Registrar e autenticar usuário
        CriarUsuarioRequest registroRequest = new CriarUsuarioRequest(
            "Pedro Costa",
            "pedro@exemplo.com",
            "MinhaSenh@123"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroRequest)))
                .andExpect(status().isCreated());
        
        AutenticarUsuarioRequest loginRequest = new AutenticarUsuarioRequest(
            "pedro@exemplo.com",
            "MinhaSenh@123"
        );
        
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        String loginResponseContent = loginResult.getResponse().getContentAsString();
        AutenticacaoResponse loginResponse = objectMapper.readValue(loginResponseContent, AutenticacaoResponse.class);
        
        // Act & Assert - Renovar token
        mockMvc.perform(post("/api/auth/refresh")
                .header("X-Refresh-Token", loginResponse.refreshToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").value(loginResponse.refreshToken())) // Mesmo refresh token
                .andExpect(jsonPath("$.usuario.email").value("pedro@exemplo.com"));
    }
    
    @Test
    void deveRejeitarRenovacaoSemRefreshToken() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }
    
    @Test
    void deveFazerLogoutComSucesso() throws Exception {
        // Arrange - Registrar e autenticar usuário
        CriarUsuarioRequest registroRequest = new CriarUsuarioRequest(
            "Ana Silva",
            "ana@exemplo.com",
            "MinhaSenh@123"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroRequest)))
                .andExpect(status().isCreated());
        
        AutenticarUsuarioRequest loginRequest = new AutenticarUsuarioRequest(
            "ana@exemplo.com",
            "MinhaSenh@123"
        );
        
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        String loginResponseContent = loginResult.getResponse().getContentAsString();
        AutenticacaoResponse loginResponse = objectMapper.readValue(loginResponseContent, AutenticacaoResponse.class);
        
        // Act & Assert - Logout
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + loginResponse.accessToken())
                .header("X-Refresh-Token", loginResponse.refreshToken()))
                .andExpect(status().isNoContent());
    }
    
    @Test
    void deveRejeitarLogoutSemTokens() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }
}

