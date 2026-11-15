package br.edu.ifam.sementeira.controller;

import br.edu.ifam.sementeira.model.Especie;
import br.edu.ifam.sementeira.service.EspecieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/especies")
@CrossOrigin(origins = "*")
public class EspecieController {

    private final EspecieService service;

    public EspecieController(EspecieService service) {
        this.service = service;
    }

    @GetMapping
    public List<Especie> listar() {
        return service.listarAtivas();
    }

    @GetMapping("/{id}")
    public Especie buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Especie> cadastrar(@RequestBody Especie especie) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(especie));
    }

    @PutMapping("/{id}")
    public Especie atualizar(@PathVariable Long id, @RequestBody Especie especie) {
        return service.atualizar(id, especie);
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
