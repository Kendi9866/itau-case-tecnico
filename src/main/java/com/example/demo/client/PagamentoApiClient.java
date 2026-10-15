package com.example.demo.client;

import com.example.demo.DTO.PagamentoDTO;
import com.example.demo.DTO.PagamentosDTO;
import com.example.demo.exception.PagamentoApiException;
import com.example.demo.exception.PagamentoApiTimeoutException;
import com.example.demo.exception.PayloadPagamentoInvalidoException;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpTimeoutException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

@Component
public class PagamentoApiClient {

    private final RestClient restClient;

    public PagamentoApiClient(RestClient pagamentosRestClient) {
        this.restClient = pagamentosRestClient;
    }

    public PagamentosDTO listarPagamentos(String clienteId) {
        try {
            PagamentosDTO resposta = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/pagamentos")
                            .queryParam("clienteId", clienteId)
                            .build())
                    .headers(headers -> {
                        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
                        if (correlationId != null) {
                            headers.set(CorrelationIdFilter.HEADER_NAME, correlationId);
                        }
                    })
                    .retrieve()
                    .body(PagamentosDTO.class);

            validarResposta(resposta);
            return resposta;
        } catch (ResourceAccessException exception) {
            if (isTimeout(exception)) {
                throw new PagamentoApiTimeoutException();
            }
            throw new PagamentoApiException();
        } catch (RestClientException exception) {
            throw new PagamentoApiException();
        }
    }

    private void validarResposta(PagamentosDTO resposta) {
        if (resposta == null || resposta.pagamentos() == null
                || resposta.pagamentos().stream().anyMatch(this::pagamentoInvalido)) {
            throw new PayloadPagamentoInvalidoException();
        }
    }

    private boolean pagamentoInvalido(PagamentoDTO pagamento) {
        return pagamento == null
                || pagamento.id() == null
                || pagamento.id().isBlank()
                || pagamento.valor() == null
                || pagamento.valor().signum() < 0
                || pagamento.status() == null;
    }

    private boolean isTimeout(Throwable exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof SocketTimeoutException
                    || cause instanceof HttpTimeoutException
                    || cause instanceof TimeoutException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
