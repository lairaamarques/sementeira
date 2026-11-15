package br.edu.ifam.sementeira.dto;

import java.math.BigDecimal;

public record VendaDTO(
        Long estoqueId,
        BigDecimal quantidade,
        BigDecimal valorTotal,
        String comprador
) {}
