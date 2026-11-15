package br.edu.ifam.sementeira.controller;

import br.edu.ifam.sementeira.dto.VendaDTO;
import br.edu.ifam.sementeira.model.Venda;
import br.edu.ifam.sementeira.service.VendaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendas")
@CrossOrigin(origins = "*")
public class VendaController {

    private final VendaService service;

    public VendaController(VendaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Venda> listar() {
        return service.listar();
    }

    @PostMapping
    public ResponseEntity<Venda> registrar(@RequestBody VendaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(dto));
    }

    @PatchMapping("/{id}/cancelar")
    public Venda cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }
}
