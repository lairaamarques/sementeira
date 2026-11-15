package br.edu.ifam.sementeira.repository;

import br.edu.ifam.sementeira.model.Sincronizacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SincronizacaoRepository extends JpaRepository<Sincronizacao, Long> {

    Optional<Sincronizacao> findFirstByOrderByDataHoraDesc();
}
