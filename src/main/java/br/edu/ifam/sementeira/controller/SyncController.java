package br.edu.ifam.sementeira.controller;

import br.edu.ifam.sementeira.dto.SyncDTO;
import br.edu.ifam.sementeira.service.SyncService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
@CrossOrigin(origins = "*")
public class SyncController {

    private final SyncService service;

    public SyncController(SyncService service) {
        this.service = service;
    }

    @PostMapping("/matrizes")
    public SyncDTO.Resposta receberMatrizes(@RequestBody SyncDTO.Requisicao requisicao) {
        return service.receberMatrizes(requisicao);
    }

    @GetMapping("/status")
    public SyncDTO.Status status() {
        return service.status();
    }
}
