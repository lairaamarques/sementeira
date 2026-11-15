package br.edu.ifam.sementeira.dto;

import java.math.BigDecimal;
import java.util.List;

public record IndicadoresDTO(
        long matrizesMapeadas,
        BigDecimal sacasEmEstoque,
        BigDecimal mudasNoViveiro,
        BigDecimal faturamentoEstimado,
        BigDecimal faturamentoRealizado,
        BigDecimal faturamentoMes,
        long vendasMes,
        List<EstoqueEspecie> estoquePorEspecie,
        List<Alerta> alertas
) {

    public record EstoqueEspecie(String especie, String tipoRecurso, String unidade, BigDecimal saldo) {}

    public record Alerta(String tipo, String titulo, String mensagem) {}
}
