package br.edu.ifam.sementeira.controller;

import br.edu.ifam.sementeira.dto.IndicadoresDTO;
import br.edu.ifam.sementeira.service.IndicadoresService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/indicadores")
@CrossOrigin(origins = "*")
public class IndicadoresController {

    private final IndicadoresService service;

    public IndicadoresController(IndicadoresService service) {
        this.service = service;
    }

    @GetMapping
    public IndicadoresDTO calcular(@RequestParam(required = false) Long especieId) {
        return service.calcular(especieId);
    }
}
