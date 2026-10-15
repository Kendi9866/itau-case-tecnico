package com.example.demo.controller;

import com.example.demo.DTO.PendenciasDTO;
import com.example.demo.service.PendenteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clientes")
public class PendenteController {

    private final PendenteService pendenteService;

    public PendenteController(PendenteService pendenteService) {
        this.pendenteService = pendenteService;
    }

    @GetMapping({"/{clienteId}/pendencias", "/pendencias"})
    public PendenciasDTO retornarPendencia(@PathVariable(required = false) String clienteId) {
        return pendenteService.retornarPendencia(clienteId);
    }
}
