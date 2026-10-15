package com.example.demo.DTO;

import java.math.BigDecimal;

public record PendenciasDTO(
       boolean possuiPendencias,
       int quantidade,
       BigDecimal valorTotal
) {
}
