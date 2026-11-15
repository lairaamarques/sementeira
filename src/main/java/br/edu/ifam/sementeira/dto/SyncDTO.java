package br.edu.ifam.sementeira.dto;

import java.time.LocalDateTime;
import java.util.List;

public final class SyncDTO {

    private SyncDTO() {}

    public record Requisicao(String dispositivo, List<Matriz> matrizes) {}

    public record Matriz(
            String uuid,
            String nome,
            String nomeCientifico,
            Integer tempoMedioColheita,
            String epocaInicio,
            String epocaFim,
            String tipoCultivo,
            String tamanhoPorte,
            Double latitude,
            Double longitude,
            LocalDateTime atualizadoEm
    ) {}

    public record Resposta(
            int recebidas,
            int criadas,
            int atualizadas,
            int ignoradas,
            int erros,
            LocalDateTime sincronizadoEm,
            List<Resultado> resultados
    ) {}

    public record Resultado(String uuid, Long id, String situacao, String mensagem) {}

    public record Status(LocalDateTime ultimaSincronizacao, String dispositivo, long totalMatrizes) {}
}
