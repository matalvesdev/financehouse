package com.gestaofinanceira.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestaofinanceira.application.dto.request.AutenticarUsuarioRequest;
import com.gestaofinanceira.application.dto.request.CriarOrcamentoRequest;
import com.gestaofinanceira.application.dto.request.CriarUsuarioRequest;
import com.gestaofinanceira.application.dto.response.AutenticacaoResponse;
import com.gestaofinanceira.domain.valueobjects.PeriodoOrcamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para OrcamentoController.
 * 
 * Valida contratos de API e responses para operações de orçamentos.
 * Requirements: 5.1, 5.2
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrcamentoControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String accessToken;
    
    @BeforeEach
    void setUp() throws Exception {
        // Registrar e autenticar usuário para os testes
        CriarUsuarioRequest registroRequest = new CriarUsuarioRequest(
            "Usuario Orcamento",
            "orcamento@teste.com",
            "MinhaSenh@123"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroRequest)))
                .andExpect(status().isCreated());
        
        AutenticarUsuarioRequest loginRequest = new AutenticarUsuarioRequest(
            "orcamento@teste.com",
            "MinhaSenh@123"
        );
        
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        String loginResponseContent = loginResult.getResponse().getContentAsString();
        AutenticacaoResponse loginResponse = objectMapper.readValue(loginResponseContent, AutenticacaoResponse.class);
        
        this.accessToken = loginResponse.accessToken();
    }
    
    @Test
    void deveCriarOrcamentoComSucesso() throws Exception {
        // Arrange
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
            "ALIMENTACAO",
            new BigDecimal("500.00"),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/orcamentos")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categoria").value("ALIMENTACAO"))
                .andExpect(jsonPath("$.limite").value(500.00))
                .andExpect(jsonPath("$.periodo").value("MENSAL"))
                .andExpect(jsonPath("$.gastoAtual").value(0.00))
                .andExpect(jsonPath("$.percentualUtilizado").value(0.00))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.criadoEm").exists());
    }
    
    @Test
    void deveRejeitarOrcamentoComLimiteInvalido() throws Exception {
        // Arrange
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
            "ALIMENTACAO",
            new BigDecimal("-100.00"), // Limite negativo
            PeriodoOrcamento.MENSAL,
            LocalDate.now()
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/orcamentos")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.limite").exists());
    }
    
    @Test
    void deveRejeitarOrcamentoDuplicadoParaCategoria() throws Exception {
        // Arrange - Criar primeiro orçamento
        CriarOrcamentoRequest primeiroRequest = new CriarOrcamentoRequest(
            "TRANSPORTE",
            new BigDecimal("300.00"),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        
        mockMvc.perform(post("/api/orcamentos")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(primeiroRequest)))
                .andExpect(status().isCreated());
        
        // Arrange - Tentar criar segundo orçamento para mesma categoria
        CriarOrcamentoRequest segundoRequest = new CriarOrcamentoRequest(
            "TRANSPORTE",
            new BigDecimal("400.00"),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/orcamentos")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(segundoRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("BUDGET_BUSINESS_RULE_ERROR"));
    }
    
    @Test
    void deveListarOrcamentosAtivos() throws Exception {
        // Arrange - Criar alguns orçamentos
        CriarOrcamentoRequest orcamento1 = new CriarOrcamentoRequest(
            "ALIMENTACAO",
            new BigDecimal("500.00"),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        
        CriarOrcamentoRequest orcamento2 = new CriarOrcamentoRequest(
            "LAZER",
            new BigDecimal("200.00"),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        
        mockMvc.perform(post("/api/orcamentos")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orcamento1)))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/orcamentos")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orcamento2)))
                .andExpect(status().isCreated());
        
        // Act & Assert
        mockMvc.perform(get("/api/orcamentos")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].categoria").exists())
                .andExpect(jsonPath("$[1].categoria").exists());
    }
    
    @Test
    void deveBuscarOrcamentoPorCategoria() throws Exception {
        // Arrange - Criar orçamento
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
            "SAUDE",
            new BigDecimal("150.00"),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        
        mockMvc.perform(post("/api/orcamentos")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        // Act & Assert
        mockMvc.perform(get("/api/orcamentos/categoria/SAUDE")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categoria").value("SAUDE"))
                .andExpect(jsonPath("$.limite").value(150.00));
    }
    
    @Test
    void deveRetornar404ParaCategoriaInexistente() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orcamentos/categoria/CATEGORIA_INEXISTENTE")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void deveObterResumoDeOrcamentos() throws Exception {
        // Arrange - Criar orçamentos
        CriarOrcamentoRequest orcamento1 = new CriarOrcamentoRequest(
            "ALIMENTACAO",
            new BigDecimal("500.00"),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        
        CriarOrcamentoRequest orcamento2 = new CriarOrcamentoRequest(
            "TRANSPORTE",
            new BigDecimal("300.00"),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        
        mockMvc.perform(post("/api/orcamentos")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orcamento1)))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/orcamentos")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orcamento2)))
                .andExpect(status().isCreated());
        
        // Act & Assert
        mockMvc.perform(get("/api/orcamentos/resumo")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalOrcamentos").value(2))
                .andExpect(jsonPath("$.orcamentosExcedidos").value(0))
                .andExpect(jsonPath("$.orcamentosProximosLimite").value(0))
                .andExpect(jsonPath("$.totalLimites").value(800.00))
                .andExpect(jsonPath("$.totalGastos").value(0.00))
                .andExpect(jsonPath("$.percentualGeralUtilizado").value(0.00));
    }
    
    @Test
    void deveRejeitarOperacaoSemAutenticacao() throws Exception {
        // Arrange
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
            "ALIMENTACAO",
            new BigDecimal("500.00"),
            PeriodoOrcamento.MENSAL,
            LocalDate.now()
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/orcamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void deveListarApenasOrcamentosDoUsuarioAutenticado() throws Exception {
        // Arrange - Criar orçamento para o usuário atual
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
            "ALIMENTACAO",
            new BigDecimal("500.00"),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        
        mockMvc.perform(post("/api/orcamentos")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        // Arrange - Criar outro usuário e orçamento
        CriarUsuarioRequest outroUsuarioRequest = new CriarUsuarioRequest(
            "Outro Usuario",
            "outro@teste.com",
            "MinhaSenh@123"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(outroUsuarioRequest)))
                .andExpect(status().isCreated());
        
        AutenticarUsuarioRequest outroLoginRequest = new AutenticarUsuarioRequest(
            "outro@teste.com",
            "MinhaSenh@123"
        );
        
        MvcResult outroLoginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(outroLoginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        String outroLoginResponseContent = outroLoginResult.getResponse().getContentAsString();
        AutenticacaoResponse outroLoginResponse = objectMapper.readValue(outroLoginResponseContent, AutenticacaoResponse.class);
        
        CriarOrcamentoRequest outroOrcamentoRequest = new CriarOrcamentoRequest(
            "TRANSPORTE",
            new BigDecimal("300.00"),
            PeriodoOrcamento.MENSAL,
            LocalDate.now().withDayOfMonth(1)
        );
        
        mockMvc.perform(post("/api/orcamentos")
                .header("Authorization", "Bearer " + outroLoginResponse.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(outroOrcamentoRequest)))
                .andExpect(status().isCreated());
        
        // Act & Assert - Usuário original deve ver apenas seu orçamento
        mockMvc.perform(get("/api/orcamentos")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categoria").value("ALIMENTACAO"));
    }
}

