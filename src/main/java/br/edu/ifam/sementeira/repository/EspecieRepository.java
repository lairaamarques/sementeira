package br.edu.ifam.sementeira.repository;

import br.edu.ifam.sementeira.model.Especie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EspecieRepository extends JpaRepository<Especie, Long> {

    List<Especie> findByAtivoTrueOrderByNomePopularAsc();

    @Query("""
            select count(e) > 0 from Especie e
            where e.ativo = true
              and (:id is null or e.id <> :id)
              and (lower(e.nomePopular) = lower(:nomePopular)
                   or lower(e.nomeCientifico) = lower(:nomeCientifico))
            """)
    boolean existeDuplicadaAtiva(@Param("id") Long id,
                                 @Param("nomePopular") String nomePopular,
                                 @Param("nomeCientifico") String nomeCientifico);
}
