package com.gestaofinanceira.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestaofinanceira.application.dto.request.AutenticarUsuarioRequest;
import com.gestaofinanceira.application.dto.request.CriarMetaFinanceiraRequest;
import com.gestaofinanceira.application.dto.request.CriarUsuarioRequest;
import com.gestaofinanceira.application.dto.response.AutenticacaoResponse;
import com.gestaofinanceira.domain.valueobjects.TipoMeta;
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
 * Testes de integração para MetaFinanceiraController.
 * 
 * Valida contratos de API e responses para operações de metas financeiras.
 * Requirements: 6.1, 6.2
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MetaFinanceiraControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String accessToken;
    
    @BeforeEach
    void setUp() throws Exception {
        // Registrar e autenticar usuário para os testes
        CriarUsuarioRequest registroRequest = new CriarUsuarioRequest(
            "Usuario Meta",
            "meta@teste.com",
            "MinhaSenh@123"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroRequest)))
                .andExpect(status().isCreated());
        
        AutenticarUsuarioRequest loginRequest = new AutenticarUsuarioRequest(
            "meta@teste.com",
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
    void deveCriarMetaFinanceiraComSucesso() throws Exception {
        // Arrange
        CriarMetaFinanceiraRequest request = new CriarMetaFinanceiraRequest(
            "Reserva de Emergência",
            new BigDecimal("10000.00"),
            LocalDate.now().plusMonths(12),
            TipoMeta.EMERGENCIA
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/metas")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nome").value("Reserva de Emergência"))
                .andExpect(jsonPath("$.valorAlvo").value(10000.00))
                .andExpect(jsonPath("$.valorAtual").value(0.00))
                .andExpect(jsonPath("$.tipo").value("EMERGENCIA"))
                .andExpect(jsonPath("$.percentualConclusao").value(0.00))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.criadoEm").exists());
    }
    
    @Test
    void deveRejeitarMetaComValorAlvoInvalido() throws Exception {
        // Arrange
        CriarMetaFinanceiraRequest request = new CriarMetaFinanceiraRequest(
            "Meta Inválida",
            new BigDecimal("-1000.00"), // Valor negativo
            LocalDate.now().plusMonths(6),
            TipoMeta.COMPRA
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/metas")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.valorAlvo").exists());
    }
    
    @Test
    void deveRejeitarMetaComPrazoPassado() throws Exception {
        // Arrange
        CriarMetaFinanceiraRequest request = new CriarMetaFinanceiraRequest(
            "Meta com prazo passado",
            new BigDecimal("5000.00"),
            LocalDate.now().minusDays(1), // Data no passado
            TipoMeta.VIAGEM
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/metas")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.prazo").exists());
    }
    
    @Test
    void deveListarMetasAtivas() throws Exception {
        // Arrange - Criar algumas metas
        CriarMetaFinanceiraRequest meta1 = new CriarMetaFinanceiraRequest(
            "Viagem para Europa",
            new BigDecimal("8000.00"),
            LocalDate.now().plusMonths(18),
            TipoMeta.VIAGEM
        );
        
        CriarMetaFinanceiraRequest meta2 = new CriarMetaFinanceiraRequest(
            "Novo Notebook",
            new BigDecimal("3000.00"),
            LocalDate.now().plusMonths(6),
            TipoMeta.COMPRA
        );
        
        mockMvc.perform(post("/api/metas")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(meta1)))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/metas")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(meta2)))
                .andExpect(status().isCreated());
        
        // Act & Assert
        mockMvc.perform(get("/api/metas")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").exists())
                .andExpect(jsonPath("$[1].nome").exists());
    }
    
    @Test
    void deveListarMetasPorTipo() throws Exception {
        // Arrange - Criar metas de diferentes tipos
        CriarMetaFinanceiraRequest metaViagem = new CriarMetaFinanceiraRequest(
            "Viagem",
            new BigDecimal("5000.00"),
            LocalDate.now().plusMonths(12),
            TipoMeta.VIAGEM
        );
        
        CriarMetaFinanceiraRequest metaCompra = new CriarMetaFinanceiraRequest(
            "Compra",
            new BigDecimal("2000.00"),
            LocalDate.now().plusMonths(6),
            TipoMeta.COMPRA
        );
        
        mockMvc.perform(post("/api/metas")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metaViagem)))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/metas")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metaCompra)))
                .andExpect(status().isCreated());
        
        // Act & Assert - Filtrar por tipo VIAGEM
        mockMvc.perform(get("/api/metas")
                .header("Authorization", "Bearer " + accessToken)
                .param("tipo", "VIAGEM"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipo").value("VIAGEM"))
                .andExpect(jsonPath("$[0].nome").value("Viagem"));
    }
    
    @Test
    void deveObterResumoDeMetasFinanceiras() throws Exception {
        // Arrange - Criar metas
        CriarMetaFinanceiraRequest meta1 = new CriarMetaFinanceiraRequest(
            "Meta 1",
            new BigDecimal("5000.00"),
            LocalDate.now().plusMonths(12),
            TipoMeta.EMERGENCIA
        );
        
        CriarMetaFinanceiraRequest meta2 = new CriarMetaFinanceiraRequest(
            "Meta 2",
            new BigDecimal("3000.00"),
            LocalDate.now().plusMonths(6),
            TipoMeta.COMPRA
        );
        
        mockMvc.perform(post("/api/metas")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(meta1)))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/metas")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(meta2)))
                .andExpect(status().isCreated());
        
        // Act & Assert
        mockMvc.perform(get("/api/metas/resumo")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalMetas").value(2))
                .andExpect(jsonPath("$.metasAlcancadas").value(0))
                .andExpect(jsonPath("$.metasEmAndamento").value(2))
                .andExpect(jsonPath("$.metasAtrasadas").value(0))
                .andExpect(jsonPath("$.totalValorAlvo").value(8000.00))
                .andExpect(jsonPath("$.totalValorAtual").value(0.00))
                .andExpect(jsonPath("$.percentualGeralConclusao").value(0.00));
    }
    
    @Test
    void deveListarMetasProximasDoVencimento() throws Exception {
        // Arrange - Criar metas com diferentes prazos
        CriarMetaFinanceiraRequest metaProxima = new CriarMetaFinanceiraRequest(
            "Meta Próxima",
            new BigDecimal("1000.00"),
            LocalDate.now().plusDays(15), // Dentro de 30 dias
            TipoMeta.COMPRA
        );
        
        CriarMetaFinanceiraRequest metaDistante = new CriarMetaFinanceiraRequest(
            "Meta Distante",
            new BigDecimal("5000.00"),
            LocalDate.now().plusMonths(6), // Mais de 30 dias
            TipoMeta.VIAGEM
        );
        
        mockMvc.perform(post("/api/metas")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metaProxima)))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/metas")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metaDistante)))
                .andExpect(status().isCreated());
        
        // Act & Assert
        mockMvc.perform(get("/api/metas/proximas-vencimento")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Meta Próxima"));
    }
    
    @Test
    void deveListarMetasProximasComParametroPersonalizado() throws Exception {
        // Arrange - Criar meta
        CriarMetaFinanceiraRequest meta = new CriarMetaFinanceiraRequest(
            "Meta em 45 dias",
            new BigDecimal("2000.00"),
            LocalDate.now().plusDays(45),
            TipoMeta.COMPRA
        );
        
        mockMvc.perform(post("/api/metas")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(meta)))
                .andExpect(status().isCreated());
        
        // Act & Assert - Buscar metas próximas em 60 dias
        mockMvc.perform(get("/api/metas/proximas-vencimento")
                .header("Authorization", "Bearer " + accessToken)
                .param("dias", "60"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Meta em 45 dias"));
    }
    
    @Test
    void deveRejeitarOperacaoSemAutenticacao() throws Exception {
        // Arrange
        CriarMetaFinanceiraRequest request = new CriarMetaFinanceiraRequest(
            "Meta sem auth",
            new BigDecimal("1000.00"),
            LocalDate.now().plusMonths(6),
            TipoMeta.COMPRA
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/metas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void deveListarApenasMetasDoUsuarioAutenticado() throws Exception {
        // Arrange - Criar meta para o usuário atual
        CriarMetaFinanceiraRequest request = new CriarMetaFinanceiraRequest(
            "Minha Meta",
            new BigDecimal("2000.00"),
            LocalDate.now().plusMonths(6),
            TipoMeta.COMPRA
        );
        
        mockMvc.perform(post("/api/metas")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        // Arrange - Criar outro usuário e meta
        CriarUsuarioRequest outroUsuarioRequest = new CriarUsuarioRequest(
            "Outro Usuario Meta",
            "outrometa@teste.com",
            "MinhaSenh@123"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(outroUsuarioRequest)))
                .andExpect(status().isCreated());
        
        AutenticarUsuarioRequest outroLoginRequest = new AutenticarUsuarioRequest(
            "outrometa@teste.com",
            "MinhaSenh@123"
        );
        
        MvcResult outroLoginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(outroLoginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        String outroLoginResponseContent = outroLoginResult.getResponse().getContentAsString();
        AutenticacaoResponse outroLoginResponse = objectMapper.readValue(outroLoginResponseContent, AutenticacaoResponse.class);
        
        CriarMetaFinanceiraRequest outraMetaRequest = new CriarMetaFinanceiraRequest(
            "Meta do Outro",
            new BigDecimal("3000.00"),
            LocalDate.now().plusMonths(12),
            TipoMeta.VIAGEM
        );
        
        mockMvc.perform(post("/api/metas")
                .header("Authorization", "Bearer " + outroLoginResponse.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(outraMetaRequest)))
                .andExpect(status().isCreated());
        
        // Act & Assert - Usuário original deve ver apenas sua meta
        mockMvc.perform(get("/api/metas")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Minha Meta"));
    }
}

