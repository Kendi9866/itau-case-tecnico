package com.example.demo.service;

import com.example.demo.DTO.PagamentoDTO;
import com.example.demo.DTO.PagamentosDTO;
import com.example.demo.DTO.PendenciasDTO;
import com.example.demo.client.PagamentoApiClient;
import com.example.demo.domain.StatusPagamento;
import com.example.demo.exception.InvalidClienteIdException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PendenteServiceTest {

    @Mock
    private PagamentoApiClient pagamentoApiClient;

    @InjectMocks
    private PendenteService pendenteService;

    @Test
    void deveSomarSomentePagamentosPendentes() {
        when(pagamentoApiClient.listarPagamentos("123")).thenReturn(new PagamentosDTO("123", List.of(
                new PagamentoDTO("001", new BigDecimal("1500.00"), StatusPagamento.PENDENTE),
                new PagamentoDTO("002", new BigDecimal("300.00"), StatusPagamento.AUTORIZADO),
                new PagamentoDTO("003", new BigDecimal("42.30"), StatusPagamento.PENDENTE)
        )));

        PendenciasDTO resultado = pendenteService.retornarPendencia("123");

        assertTrue(resultado.possuiPendencias());
        assertEquals(2, resultado.quantidade());
        assertEquals(0, new BigDecimal("1542.30").compareTo(resultado.valorTotal()));
        verify(pagamentoApiClient).listarPagamentos("123");
    }

    @Test
    void deveRetornarResumoZeradoQuandoNaoHaPendencias() {
        when(pagamentoApiClient.listarPagamentos("123")).thenReturn(
                new PagamentosDTO("123", List.of(
                        new PagamentoDTO("001", new BigDecimal("300.00"), StatusPagamento.AUTORIZADO)
                ))
        );

        PendenciasDTO resultado = pendenteService.retornarPendencia("123");

        assertFalse(resultado.possuiPendencias());
        assertEquals(0, resultado.quantidade());
        assertEquals(0, BigDecimal.ZERO.compareTo(resultado.valorTotal()));
    }

    @Test
    void deveRejeitarClienteIdInvalidoSemChamarApi() {
        assertThrows(InvalidClienteIdException.class, () -> pendenteService.retornarPendencia(" "));
        verify(pagamentoApiClient, never()).listarPagamentos(" ");
    }
}
