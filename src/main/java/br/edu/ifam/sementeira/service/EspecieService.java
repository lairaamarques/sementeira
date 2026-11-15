package br.edu.ifam.sementeira.service;

import br.edu.ifam.sementeira.exception.RegraNegocioException;
import br.edu.ifam.sementeira.model.Especie;
import br.edu.ifam.sementeira.repository.EspecieRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EspecieService {

    private final EspecieRepository repository;

    public EspecieService(EspecieRepository repository) {
        this.repository = repository;
    }

    public List<Especie> listarAtivas() {
        return repository.findByAtivoTrueOrderByNomePopularAsc();
    }

    public Especie buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> RegraNegocioException.naoEncontrado("Espécie"));
    }

    @Transactional
    public Especie cadastrar(Especie dados) {
        validar(null, dados);
        Especie nova = new Especie();
        copiar(dados, nova);
        return repository.save(nova);
    }

    @Transactional
    public Especie atualizar(Long id, Especie dados) {
        Especie existente = buscar(id);
        validar(id, dados);
        copiar(dados, existente);
        return repository.save(existente);
    }

    @Transactional
    public void inativar(Long id) {
        Especie especie = buscar(id);
        especie.setAtivo(false);
        repository.save(especie);
    }

    private void validar(Long id, Especie dados) {
        if (vazio(dados.getNomePopular()) || vazio(dados.getNomeCientifico())
                || vazio(dados.getTipoRecurso()) || vazio(dados.getTempoMaturacao())) {
            throw new RegraNegocioException(
                    "Preencha Nome Popular, Nome Científico, Tipo de Recurso e Tempo de Maturação");
        }
        if (dados.getValorMercado() != null && dados.getValorMercado().compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("O valor de mercado não pode ser negativo");
        }
        if (repository.existeDuplicadaAtiva(id, dados.getNomePopular().trim(), dados.getNomeCientifico().trim())) {
            throw new RegraNegocioException(HttpStatus.CONFLICT, "Espécie já catalogada no diretório");
        }
    }

    private void copiar(Especie origem, Especie destino) {
        destino.setNomePopular(origem.getNomePopular().trim());
        destino.setNomeCientifico(origem.getNomeCientifico().trim());
        destino.setTipoRecurso(origem.getTipoRecurso().trim());
        destino.setTempoMaturacao(origem.getTempoMaturacao().trim());
        destino.setValorMercado(origem.getValorMercado());
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}
