package com.example.demo.DTO;

import java.util.List;

public record PagamentosDTO(
        String clienteId,
        List<PagamentoDTO> pagamentos
) {
}
