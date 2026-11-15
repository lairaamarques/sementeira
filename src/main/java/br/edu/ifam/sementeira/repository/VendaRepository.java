package br.edu.ifam.sementeira.repository;

import br.edu.ifam.sementeira.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    List<Venda> findAllByOrderByDataVendaDesc();

    boolean existsByEstoqueId(Long estoqueId);
}
