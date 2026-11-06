package br.edu.ifam.sementeira.controller;

import br.edu.ifam.sementeira.model.Semente;
import br.edu.ifam.sementeira.repository.SementeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/semente") // A rota base para a API da Fani
@CrossOrigin(origins = "*")
public class SementeController {

    @Autowired
    private SementeRepository repository;

    // Igual a: @GET("semente")
    @GetMapping
    public List<Semente> listarTodas() {
        return repository.findAll();
    }

    // Igual a: @GET("semente/{id}")
    @GetMapping("/{id}")
    public Semente buscarPorId(@PathVariable Long id) {
        return repository.findById(id).orElse(null);
    }

    // Igual a: @POST("semente")
    @PostMapping
    public Semente cadastrarSemente(@RequestBody Semente sementeDTO) {
        return repository.save(sementeDTO);
    }

    // Igual a: @PUT("semente/{id}")
    @PutMapping("/{id}")
    public Semente atualizarSemente(@PathVariable Long id, @RequestBody Semente sementeAtualizada) {
        sementeAtualizada.setId(id); // Garante que o ID correto será atualizado
        return repository.save(sementeAtualizada);
    }

    // Igual a: @DELETE("semente/{id}")
    @DeleteMapping("/{id}")
    public void deletarSemente(@PathVariable Long id) {
        repository.deleteById(id);
    }
}