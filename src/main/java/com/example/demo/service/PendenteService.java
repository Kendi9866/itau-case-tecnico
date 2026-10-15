package com.example.demo.service;

import com.example.demo.DTO.PagamentoDTO;
import com.example.demo.DTO.PagamentosDTO;
import com.example.demo.DTO.PendenciasDTO;
import com.example.demo.client.PagamentoApiClient;
import com.example.demo.domain.StatusPagamento;
import com.example.demo.exception.InvalidClienteIdException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PendenteService {

    private static final Pattern CLIENTE_ID_VALIDO = Pattern.compile("[A-Za-z0-9_-]{1,50}");

    private final PagamentoApiClient pagamentoApiClient;

    public PendenteService(PagamentoApiClient pagamentoApiClient) {
        this.pagamentoApiClient = pagamentoApiClient;
    }

    public PendenciasDTO retornarPendencia(String clienteId) {
        if (clienteId == null || !CLIENTE_ID_VALIDO.matcher(clienteId).matches()) {
            throw new InvalidClienteIdException();
        }

        PagamentosDTO respostaPagamentos = pagamentoApiClient.listarPagamentos(clienteId);
        List<PagamentoDTO> pendentes = respostaPagamentos.pagamentos().stream()
                .filter(pagamento -> pagamento.status() == StatusPagamento.PENDENTE)
                .toList();

        BigDecimal valorTotal = pendentes.stream()
                .map(PagamentoDTO::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PendenciasDTO(!pendentes.isEmpty(), pendentes.size(), valorTotal);
    }
}
