package com.gestaofinanceira.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestaofinanceira.application.dto.request.AutenticarUsuarioRequest;
import com.gestaofinanceira.application.dto.request.CriarTransacaoRequest;
import com.gestaofinanceira.application.dto.request.CriarUsuarioRequest;
import com.gestaofinanceira.application.dto.response.AutenticacaoResponse;
import com.gestaofinanceira.domain.valueobjects.TipoTransacao;
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
 * Testes de integração para TransacaoController.
 * 
 * Valida contratos de API e responses para operações de transações.
 * Requirements: 3.1, 3.2, 3.3, 3.5
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TransacaoControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String accessToken;
    private String refreshToken;
    
    @BeforeEach
    void setUp() throws Exception {
        // Registrar e autenticar usuário para os testes
        CriarUsuarioRequest registroRequest = new CriarUsuarioRequest(
            "Usuario Teste",
            "usuario@teste.com",
            "MinhaSenh@123"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroRequest)))
                .andExpect(status().isCreated());
        
        AutenticarUsuarioRequest loginRequest = new AutenticarUsuarioRequest(
            "usuario@teste.com",
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
        this.refreshToken = loginResponse.refreshToken();
    }
    
    @Test
    void deveCriarTransacaoComSucesso() throws Exception {
        // Arrange
        CriarTransacaoRequest request = new CriarTransacaoRequest(
            new BigDecimal("150.50"),
            "Compra no supermercado",
            "ALIMENTACAO",
            TipoTransacao.DESPESA,
            LocalDate.now()
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/transacoes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.valor").value(150.50))
                .andExpect(jsonPath("$.descricao").value("Compra no supermercado"))
                .andExpect(jsonPath("$.categoria").value("ALIMENTACAO"))
                .andExpect(jsonPath("$.tipo").value("DESPESA"))
                .andExpect(jsonPath("$.ativa").value(true))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.criadoEm").exists());
    }
    
    @Test
    void deveRejeitarTransacaoComValorInvalido() throws Exception {
        // Arrange
        CriarTransacaoRequest request = new CriarTransacaoRequest(
            new BigDecimal("-50.00"), // Valor negativo
            "Transação inválida",
            "ALIMENTACAO",
            TipoTransacao.DESPESA,
            LocalDate.now()
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/transacoes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.valor").exists());
    }
    
    @Test
    void deveRejeitarTransacaoSemAutenticacao() throws Exception {
        // Arrange
        CriarTransacaoRequest request = new CriarTransacaoRequest(
            new BigDecimal("100.00"),
            "Transação sem auth",
            "ALIMENTACAO",
            TipoTransacao.DESPESA,
            LocalDate.now()
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/transacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void deveListarTransacoesDoUsuario() throws Exception {
        // Arrange - Criar algumas transações
        CriarTransacaoRequest transacao1 = new CriarTransacaoRequest(
            new BigDecimal("100.00"),
            "Receita 1",
            "SALARIO",
            TipoTransacao.RECEITA,
            LocalDate.now()
        );
        
        CriarTransacaoRequest transacao2 = new CriarTransacaoRequest(
            new BigDecimal("50.00"),
            "Despesa 1",
            "ALIMENTACAO",
            TipoTransacao.DESPESA,
            LocalDate.now()
        );
        
        mockMvc.perform(post("/api/transacoes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacao1)))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/transacoes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacao2)))
                .andExpect(status().isCreated());
        
        // Act & Assert
        mockMvc.perform(get("/api/transacoes")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].descricao").exists())
                .andExpect(jsonPath("$.content[1].descricao").exists());
    }
    
    @Test
    void deveListarTransacoesComFiltros() throws Exception {
        // Arrange - Criar transações de diferentes categorias
        CriarTransacaoRequest transacaoAlimentacao = new CriarTransacaoRequest(
            new BigDecimal("30.00"),
            "Lanche",
            "ALIMENTACAO",
            TipoTransacao.DESPESA,
            LocalDate.now()
        );
        
        CriarTransacaoRequest transacaoTransporte = new CriarTransacaoRequest(
            new BigDecimal("20.00"),
            "Uber",
            "TRANSPORTE",
            TipoTransacao.DESPESA,
            LocalDate.now()
        );
        
        mockMvc.perform(post("/api/transacoes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoAlimentacao)))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/transacoes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoTransporte)))
                .andExpect(status().isCreated());
        
        // Act & Assert - Filtrar por categoria
        mockMvc.perform(get("/api/transacoes")
                .header("Authorization", "Bearer " + accessToken)
                .param("categoria", "ALIMENTACAO"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].categoria").value("ALIMENTACAO"));
    }
    
    @Test
    void deveListarTransacoesRecentes() throws Exception {
        // Arrange - Criar transação
        CriarTransacaoRequest request = new CriarTransacaoRequest(
            new BigDecimal("75.00"),
            "Transação recente",
            "LAZER",
            TipoTransacao.DESPESA,
            LocalDate.now()
        );
        
        mockMvc.perform(post("/api/transacoes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        // Act & Assert
        mockMvc.perform(get("/api/transacoes/recentes")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].descricao").value("Transação recente"));
    }
    
    @Test
    void deveAtualizarTransacaoExistente() throws Exception {
        // Arrange - Criar transação primeiro
        CriarTransacaoRequest criarRequest = new CriarTransacaoRequest(
            new BigDecimal("100.00"),
            "Descrição original",
            "ALIMENTACAO",
            TipoTransacao.DESPESA,
            LocalDate.now()
        );
        
        MvcResult criarResult = mockMvc.perform(post("/api/transacoes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criarRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String criarResponseContent = criarResult.getResponse().getContentAsString();
        var criarResponse = objectMapper.readTree(criarResponseContent);
        String transacaoId = criarResponse.get("id").asText();
        
        // Arrange - Dados para atualização
        var atualizarRequest = new com.gestaofinanceira.application.dto.request.AtualizarTransacaoRequest(
            new BigDecimal("150.00"),
            "Descrição atualizada",
            "LAZER",
            TipoTransacao.DESPESA,
            LocalDate.now()
        );
        
        // Act & Assert
        mockMvc.perform(put("/api/transacoes/" + transacaoId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(atualizarRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(transacaoId))
                .andExpect(jsonPath("$.valor").value(150.00))
                .andExpect(jsonPath("$.descricao").value("Descrição atualizada"))
                .andExpect(jsonPath("$.categoria").value("LAZER"));
    }
    
    @Test
    void deveExcluirTransacaoExistente() throws Exception {
        // Arrange - Criar transação primeiro
        CriarTransacaoRequest criarRequest = new CriarTransacaoRequest(
            new BigDecimal("80.00"),
            "Transação para excluir",
            "ALIMENTACAO",
            TipoTransacao.DESPESA,
            LocalDate.now()
        );
        
        MvcResult criarResult = mockMvc.perform(post("/api/transacoes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criarRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String criarResponseContent = criarResult.getResponse().getContentAsString();
        var criarResponse = objectMapper.readTree(criarResponseContent);
        String transacaoId = criarResponse.get("id").asText();
        
        // Act & Assert
        mockMvc.perform(delete("/api/transacoes/" + transacaoId)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }
    
    @Test
    void deveReativarTransacaoExcluida() throws Exception {
        // Arrange - Criar e excluir transação
        CriarTransacaoRequest criarRequest = new CriarTransacaoRequest(
            new BigDecimal("60.00"),
            "Transação para reativar",
            "ALIMENTACAO",
            TipoTransacao.DESPESA,
            LocalDate.now()
        );
        
        MvcResult criarResult = mockMvc.perform(post("/api/transacoes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criarRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String criarResponseContent = criarResult.getResponse().getContentAsString();
        var criarResponse = objectMapper.readTree(criarResponseContent);
        String transacaoId = criarResponse.get("id").asText();
        
        // Excluir transação
        mockMvc.perform(delete("/api/transacoes/" + transacaoId)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
        
        // Act & Assert - Reativar
        mockMvc.perform(patch("/api/transacoes/" + transacaoId + "/reativar")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(transacaoId))
                .andExpect(jsonPath("$.ativa").value(true))
                .andExpect(jsonPath("$.descricao").value("Transação para reativar"));
    }
    
    @Test
    void deveRejeitarOperacaoComTransacaoInexistente() throws Exception {
        // Arrange
        String transacaoIdInexistente = "550e8400-e29b-41d4-a716-446655440000";
        
        // Act & Assert
        mockMvc.perform(delete("/api/transacoes/" + transacaoIdInexistente)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("TRANSACTION_BUSINESS_RULE_ERROR"));
    }
}

