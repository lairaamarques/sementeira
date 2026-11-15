package br.edu.ifam.sementeira.dto;

import java.math.BigDecimal;

public record SaidaEstoqueDTO(
        String tipo,
        BigDecimal quantidade,
        String observacao
) {}
