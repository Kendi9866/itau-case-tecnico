package com.example.demo.client;

import com.example.demo.DTO.PagamentosDTO;
import com.example.demo.exception.PagamentoApiException;
import com.example.demo.exception.PagamentoApiTimeoutException;
import com.example.demo.exception.PayloadPagamentoInvalidoException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PagamentoApiClientTest {

    private MockRestServiceServer server;

    @AfterEach
    void verificarRequisicoes() {
        if (server != null) {
            server.verify();
        }
    }

    @Test
    void deveConsumirEndpointDePagamentosEPropagarCorrelationId() {
        PagamentoApiClient client = criarClient();
        server.expect(requestTo("http://pagamentos.test/api/pagamentos?clienteId=123"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(CorrelationIdFilter.HEADER_NAME, "correlation-123"))
                .andRespond(withSuccess("""
                        {"clienteId":"123","pagamentos":[
                          {"id":"001","valor":1500.00,"status":"PENDENTE"}
                        ]}
                        """, org.springframework.http.MediaType.APPLICATION_JSON));
        org.slf4j.MDC.put(CorrelationIdFilter.MDC_KEY, "correlation-123");

        try {
            PagamentosDTO result = client.listarPagamentos("123");
            assertEquals("123", result.clienteId());
            assertEquals("001", result.pagamentos().getFirst().id());
        } finally {
            org.slf4j.MDC.remove(CorrelationIdFilter.MDC_KEY);
        }
    }

    @Test
    void deveRejeitarPayloadSemListaDePagamentos() {
        PagamentoApiClient client = criarClient();
        server.expect(requestTo("http://pagamentos.test/api/pagamentos?clienteId=123"))
                .andRespond(withSuccess(
                        "{\"clienteId\":\"123\",\"pagamentos\":null}",
                        org.springframework.http.MediaType.APPLICATION_JSON
                ));

        assertThrows(PayloadPagamentoInvalidoException.class, () -> client.listarPagamentos("123"));
    }

    @Test
    void deveTraduzirErroHttpDaApiDePagamentos() {
        PagamentoApiClient client = criarClient();
        server.expect(requestTo("http://pagamentos.test/api/pagamentos?clienteId=123"))
                .andRespond(withServerError());

        assertThrows(PagamentoApiException.class, () -> client.listarPagamentos("123"));
    }

    @Test
    void deveTraduzirTimeoutDaApiDePagamentos() {
        PagamentoApiClient client = criarClient();
        server.expect(requestTo("http://pagamentos.test/api/pagamentos?clienteId=123"))
                .andRespond(request -> {
                    throw new ResourceAccessException("timeout", new SocketTimeoutException());
                });

        assertThrows(PagamentoApiTimeoutException.class, () -> client.listarPagamentos("123"));
    }

    private PagamentoApiClient criarClient() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        return new PagamentoApiClient(builder.baseUrl("http://pagamentos.test").build());
    }
}
