package br.edu.ifam.sementeira.service;

import br.edu.ifam.sementeira.dto.SyncDTO;
import br.edu.ifam.sementeira.dto.SyncDTO.Matriz;
import br.edu.ifam.sementeira.dto.SyncDTO.Resultado;
import br.edu.ifam.sementeira.exception.RegraNegocioException;
import br.edu.ifam.sementeira.model.NomeCientifico;
import br.edu.ifam.sementeira.model.Semente;
import br.edu.ifam.sementeira.model.Sincronizacao;
import br.edu.ifam.sementeira.repository.NomeCientificoRepository;
import br.edu.ifam.sementeira.repository.SementeRepository;
import br.edu.ifam.sementeira.repository.SincronizacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SyncService {

    public static final int MAX_POR_ENVIO = 500;

    private final SementeRepository sementeRepository;
    private final NomeCientificoRepository nomeCientificoRepository;
    private final SincronizacaoRepository sincronizacaoRepository;

    public SyncService(SementeRepository sementeRepository,
                       NomeCientificoRepository nomeCientificoRepository,
                       SincronizacaoRepository sincronizacaoRepository) {
        this.sementeRepository = sementeRepository;
        this.nomeCientificoRepository = nomeCientificoRepository;
        this.sincronizacaoRepository = sincronizacaoRepository;
    }

    @Transactional
    public SyncDTO.Resposta receberMatrizes(SyncDTO.Requisicao req) {
        List<Matriz> matrizes = req == null || req.matrizes() == null ? List.of() : req.matrizes();
        if (matrizes.size() > MAX_POR_ENVIO) {
            throw new RegraNegocioException("Envie no máximo " + MAX_POR_ENVIO + " matrizes por vez");
        }

        LocalDateTime agora = LocalDateTime.now();
        List<Resultado> resultados = new ArrayList<>();
        int criadas = 0, atualizadas = 0, ignoradas = 0, erros = 0;

        for (Matriz m : matrizes) {
            String problema = validar(m);
            if (problema != null) {
                resultados.add(new Resultado(m == null ? null : m.uuid(), null, "ERRO", problema));
                erros++;
                continue;
            }

            Semente existente = sementeRepository.findByUuid(m.uuid().trim()).orElse(null);
            if (existente != null && versaoNaoEMaisNova(m, existente)) {
                resultados.add(new Resultado(m.uuid(), existente.getId(), "IGNORADA",
                        "O servidor já tem esta versão ou uma mais nova"));
                ignoradas++;
                continue;
            }

            Semente s = existente != null ? existente : new Semente();
            copiar(m, s);
            s.setSincronizadoEm(agora);
            s = sementeRepository.save(s);

            if (existente == null) {
                resultados.add(new Resultado(m.uuid(), s.getId(), "CRIADA", null));
                criadas++;
            } else {
                resultados.add(new Resultado(m.uuid(), s.getId(), "ATUALIZADA", null));
                atualizadas++;
            }
        }

        Sincronizacao log = new Sincronizacao();
        log.setDispositivo(req == null ? null : req.dispositivo());
        log.setDataHora(agora);
        log.setRecebidas(matrizes.size());
        log.setCriadas(criadas);
        log.setAtualizadas(atualizadas);
        log.setIgnoradas(ignoradas);
        log.setErros(erros);
        sincronizacaoRepository.save(log);

        return new SyncDTO.Resposta(matrizes.size(), criadas, atualizadas, ignoradas, erros, agora, resultados);
    }

    public SyncDTO.Status status() {
        var ultima = sincronizacaoRepository.findFirstByOrderByDataHoraDesc();
        return new SyncDTO.Status(
                ultima.map(Sincronizacao::getDataHora).orElse(null),
                ultima.map(Sincronizacao::getDispositivo).orElse(null),
                sementeRepository.count());
    }

    private static String validar(Matriz m) {
        if (m == null) return "Matriz vazia";
        if (m.uuid() == null || m.uuid().isBlank()) return "uuid é obrigatório";
        if (m.uuid().trim().length() > 36) return "uuid deve ter no máximo 36 caracteres";
        if (m.nome() == null || m.nome().isBlank()) return "nome é obrigatório";
        if (m.latitude() != null && (m.latitude() < -90 || m.latitude() > 90)) return "latitude inválida";
        if (m.longitude() != null && (m.longitude() < -180 || m.longitude() > 180)) return "longitude inválida";
        return null;
    }

    private static boolean versaoNaoEMaisNova(Matriz m, Semente existente) {
        return m.atualizadoEm() != null && existente.getAtualizadoEm() != null
                && !m.atualizadoEm().isAfter(existente.getAtualizadoEm());
    }

    private void copiar(Matriz m, Semente s) {
        s.setUuid(m.uuid().trim());
        s.setNome(m.nome().trim());
        s.setNomeCientificoId(resolverNomeCientifico(m));
        s.setTempoMedioColheita(m.tempoMedioColheita());
        s.setEpocaInicio(m.epocaInicio());
        s.setEpocaFim(m.epocaFim());
        s.setTipoCultivo(m.tipoCultivo());
        s.setTamanhoPorte(m.tamanhoPorte());
        s.setLatitude(m.latitude());
        s.setLongitude(m.longitude());
        s.setAtualizadoEm(m.atualizadoEm());
    }

    private Long resolverNomeCientifico(Matriz m) {
        if (m.nomeCientifico() == null || m.nomeCientifico().isBlank()) {
            return null;
        }
        String nome = m.nomeCientifico().trim();
        NomeCientifico nc = nomeCientificoRepository.findFirstByNomeIgnoreCase(nome).orElseGet(() -> {
            NomeCientifico novo = new NomeCientifico();
            novo.setNome(nome);
            novo.setNomePopular(m.nome().trim());
            return nomeCientificoRepository.save(novo);
        });
        return nc.getId();
    }
}
