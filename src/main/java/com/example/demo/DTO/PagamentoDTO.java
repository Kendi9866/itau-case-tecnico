package com.example.demo.DTO;

import com.example.demo.domain.StatusPagamento;

import java.math.BigDecimal;

public record PagamentoDTO(
        String id,
        BigDecimal valor,
        StatusPagamento status
) {
}
