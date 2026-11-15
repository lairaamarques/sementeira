package br.edu.ifam.sementeira.repository;

import br.edu.ifam.sementeira.model.Semente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SementeRepository extends JpaRepository<Semente, Long> {

    Optional<Semente> findByUuid(String uuid);

    long countByNomeCientificoId(Long nomeCientificoId);
}
