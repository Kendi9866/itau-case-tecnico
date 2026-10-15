package com.example.demo.controller;

import com.example.demo.DTO.PagamentoDTO;
import com.example.demo.DTO.PagamentosDTO;
import com.example.demo.client.PagamentoApiClient;
import com.example.demo.domain.StatusPagamento;
import com.example.demo.exception.PagamentoApiException;
import com.example.demo.exception.PagamentoApiTimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PendenteControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagamentoApiClient pagamentoApiClient;

    @Test
    void deveRetornarResumoDePendenciasEPropagarCorrelationId() throws Exception {
        when(pagamentoApiClient.listarPagamentos("123")).thenReturn(new PagamentosDTO("123", List.of(
                new PagamentoDTO("001", new BigDecimal("1500.00"), StatusPagamento.PENDENTE),
                new PagamentoDTO("002", new BigDecimal("300.00"), StatusPagamento.AUTORIZADO)
        )));

        mockMvc.perform(get("/clientes/123/pendencias")
                        .header("X-Correlation-Id", "request-123"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "request-123"))
                .andExpect(jsonPath("$.possuiPendencias").value(true))
                .andExpect(jsonPath("$.quantidade").value(1))
                .andExpect(jsonPath("$.valorTotal").value(1500.00));

        verify(pagamentoApiClient).listarPagamentos("123");
    }

    @Test
    void deveRejeitarClienteIdAusenteOuInvalidoAntesDeChamarApi() throws Exception {
        mockMvc.perform(get("/clientes/pendencias"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("clienteId inválido"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty());

        mockMvc.perform(get("/clientes/abc%20def/pendencias"))
                .andExpect(status().isBadRequest());

        verify(pagamentoApiClient, never()).listarPagamentos("abc def");
    }

    @Test
    void deveConverterFalhaDaApiDePagamentosEmBadGatewaySemDetalhesInternos() throws Exception {
        when(pagamentoApiClient.listarPagamentos("123")).thenThrow(new PagamentoApiException());

        mockMvc.perform(get("/clientes/123/pendencias"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("Não foi possível obter os pagamentos"))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void deveConverterTimeoutEmGatewayTimeout() throws Exception {
        when(pagamentoApiClient.listarPagamentos("123")).thenThrow(new PagamentoApiTimeoutException());

        mockMvc.perform(get("/clientes/123/pendencias"))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.message").value("Serviço de pagamentos indisponível"));
    }
}
