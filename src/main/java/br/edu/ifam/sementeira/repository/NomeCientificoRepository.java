package br.edu.ifam.sementeira.repository;

import br.edu.ifam.sementeira.model.NomeCientifico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NomeCientificoRepository extends JpaRepository<NomeCientifico, Long> {

    Optional<NomeCientifico> findFirstByNomeIgnoreCase(String nome);
}
