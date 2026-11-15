package br.edu.ifam.sementeira.controller;

import br.edu.ifam.sementeira.model.NomeCientifico;
import br.edu.ifam.sementeira.repository.NomeCientificoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nome-cientifico")
@CrossOrigin(origins = "*")
public class NomeCientificoController {

    @Autowired
    private NomeCientificoRepository repository;

    @GetMapping
    public List<NomeCientifico> listarTodos() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NomeCientifico> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<NomeCientifico> cadastrar(@RequestBody NomeCientifico nomeCientifico) {
        nomeCientifico.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(nomeCientifico));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NomeCientifico> atualizar(@PathVariable Long id, @RequestBody NomeCientifico atualizado) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        atualizado.setId(id);
        return ResponseEntity.ok(repository.save(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
