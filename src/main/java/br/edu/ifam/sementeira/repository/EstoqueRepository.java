package br.edu.ifam.sementeira.repository;

import br.edu.ifam.sementeira.model.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    List<Estoque> findAllByOrderByDataEntradaDesc();

    boolean existsBySementeId(Long sementeId);
}
