package com.example.demo.exception;

import com.example.demo.client.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class HandlerException {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerException.class);

    @ExceptionHandler(InvalidClienteIdException.class)
    public ResponseEntity<ApiError> handleInvalidRequest(InvalidClienteIdException exception) {
        return error(HttpStatus.BAD_REQUEST, "clienteId inválido", "invalid_request");
    }

    @ExceptionHandler(PagamentoApiTimeoutException.class)
    public ResponseEntity<ApiError> handleTimeout(PagamentoApiTimeoutException exception) {
        return error(HttpStatus.GATEWAY_TIMEOUT, "Serviço de pagamentos indisponível", "payment_api_timeout");
    }

    @ExceptionHandler(PagamentoApiException.class)
    public ResponseEntity<ApiError> handlePaymentApiFailure(PagamentoApiException exception) {
        return error(HttpStatus.BAD_GATEWAY, "Não foi possível obter os pagamentos", "payment_api_failure");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NoResourceFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "Recurso não encontrado", "resource_not_found");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedError(Exception exception) {
        LOGGER.error("event=unexpected_error correlationId={} exceptionType={}",
                correlationId(), exception.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(500, "Erro interno", correlationId()));
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message, String event) {
        LOGGER.warn("event={} correlationId={}", event, correlationId());
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), message, correlationId()));
    }

    private String correlationId() {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        return correlationId == null ? "indisponível" : correlationId;
    }
}
