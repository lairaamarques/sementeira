package br.edu.ifam.sementeira.repository;

import br.edu.ifam.sementeira.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findAllByOrderByDataDesc();

    boolean existsByEstoqueId(Long estoqueId);
}
