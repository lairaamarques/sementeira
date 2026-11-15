package br.edu.ifam.sementeira.repository;

import br.edu.ifam.sementeira.model.FamiliaProdutora;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FamiliaProdutoraRepository extends JpaRepository<FamiliaProdutora, Long> {

    Optional<FamiliaProdutora> findByNomeIgnoreCase(String nome);

    List<FamiliaProdutora> findAllByOrderByNomeAsc();
}
