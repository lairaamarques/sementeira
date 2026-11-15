package br.edu.ifam.sementeira.controller;

import br.edu.ifam.sementeira.dto.EntradaEstoqueDTO;
import br.edu.ifam.sementeira.dto.SaidaEstoqueDTO;
import br.edu.ifam.sementeira.model.Estoque;
import br.edu.ifam.sementeira.model.FamiliaProdutora;
import br.edu.ifam.sementeira.model.MovimentacaoEstoque;
import br.edu.ifam.sementeira.service.EstoqueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estoque")
@CrossOrigin(origins = "*")
public class EstoqueController {

    private final EstoqueService service;

    public EstoqueController(EstoqueService service) {
        this.service = service;
    }

    @GetMapping
    public List<Estoque> listar() {
        return service.listar();
    }
    
    @PostMapping
    public ResponseEntity<Estoque> lancarEntrada(@RequestBody EntradaEstoqueDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.lancarEntrada(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desfazerEntrada(@PathVariable Long id) {
        service.desfazerEntrada(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/saidas")
    public ResponseEntity<MovimentacaoEstoque> registrarSaida(@PathVariable Long id, @RequestBody SaidaEstoqueDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarSaida(id, dto));
    }

    @GetMapping("/saidas")
    public List<MovimentacaoEstoque> listarSaidas() {
        return service.listarMovimentacoes();
    }

    @DeleteMapping("/saidas/{movimentacaoId}")
    public ResponseEntity<Void> desfazerSaida(@PathVariable Long movimentacaoId) {
        service.desfazerSaida(movimentacaoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/familias")
    public List<FamiliaProdutora> listarFamilias() {
        return service.listarFamilias();
    }
}
