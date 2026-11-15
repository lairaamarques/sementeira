package br.edu.ifam.sementeira.controller;

import br.edu.ifam.sementeira.exception.RegraNegocioException;
import br.edu.ifam.sementeira.model.Semente;
import br.edu.ifam.sementeira.repository.EstoqueRepository;
import br.edu.ifam.sementeira.repository.NomeCientificoRepository;
import br.edu.ifam.sementeira.repository.SementeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/semente")
@CrossOrigin(origins = "*")
public class SementeController {

    @Autowired
    private SementeRepository repository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private NomeCientificoRepository nomeCientificoRepository;

    private void validarNomeCientifico(Semente semente) {
        Long ncId = semente.getNomeCientificoId();
        if (ncId != null && !nomeCientificoRepository.existsById(ncId)) {
            throw new RegraNegocioException("Nome científico " + ncId + " não existe no servidor");
        }
    }

    @GetMapping
    public List<Semente> listarTodas() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Semente> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Semente> cadastrarSemente(@RequestBody Semente sementeDTO) {
        sementeDTO.setId(null);
        validarNomeCientifico(sementeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(sementeDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Semente> atualizarSemente(@PathVariable Long id, @RequestBody Semente sementeAtualizada) {
        Semente existente = repository.findById(id).orElse(null);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }
        sementeAtualizada.setId(id);
        validarNomeCientifico(sementeAtualizada);

        if (existente.getUuid() != null) {
            sementeAtualizada.setUuid(existente.getUuid());
        }
        sementeAtualizada.setSincronizadoEm(existente.getSincronizadoEm());
        if (sementeAtualizada.getAtualizadoEm() == null) {
            sementeAtualizada.setAtualizadoEm(existente.getAtualizadoEm());
        }
        return ResponseEntity.ok(repository.save(sementeAtualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarSemente(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (estoqueRepository.existsBySementeId(id)) {
            throw new RegraNegocioException("Esta matriz é origem de lotes no estoque e não pode ser apagada");
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
