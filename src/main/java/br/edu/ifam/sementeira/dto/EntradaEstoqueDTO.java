package br.edu.ifam.sementeira.dto;

import java.math.BigDecimal;

public record EntradaEstoqueDTO(
        Long especieId,
        BigDecimal quantidade,
        String unidade,
        String familiaProdutora,
        Long sementeId
) {

    public EntradaEstoqueDTO(Long especieId, BigDecimal quantidade, String unidade, String familiaProdutora) {
        this(especieId, quantidade, unidade, familiaProdutora, null);
    }
}
