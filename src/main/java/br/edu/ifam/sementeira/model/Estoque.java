package br.edu.ifam.sementeira.model;

import br.edu.ifam.sementeira.exception.RegraNegocioException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "estoque")
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "especie_id", nullable = false)
    private Especie especie;

    @ManyToOne
    @JoinColumn(name = "semente_id")
    private Semente semente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "familia_produtora_id")
    private FamiliaProdutora familiaProdutora;

    @Column(nullable = false)
    private String unidade;

    @Column(nullable = false)
    private BigDecimal quantidadeInicial;

    @Column(nullable = false)
    private BigDecimal saldo;

    @Column(nullable = false)
    private String status = "Disponível";

    @Column(nullable = false)
    private LocalDateTime dataEntrada = LocalDateTime.now();

    public boolean possuiSaldo(BigDecimal quantidade) {
        return saldo.compareTo(quantidade) >= 0;
    }

    public void incrementarSaldo(BigDecimal quantidade) {
        this.saldo = this.saldo.add(quantidade);
    }

    public void decrementarSaldo(BigDecimal quantidade) {
        if (!possuiSaldo(quantidade)) {
            throw new RegraNegocioException("Saldo indisponível para esta operação");
        }
        this.saldo = this.saldo.subtract(quantidade);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FamiliaProdutora getFamiliaProdutora() {
        return familiaProdutora;
    }

    public void setFamiliaProdutora(FamiliaProdutora familiaProdutora) {
        this.familiaProdutora = familiaProdutora;
    }

    public Especie getEspecie() {
        return especie;
    }

    public void setEspecie(Especie especie) {
        this.especie = especie;
    }

    public Semente getSemente() {
        return semente;
    }

    public void setSemente(Semente semente) {
        this.semente = semente;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public BigDecimal getQuantidadeInicial() {
        return quantidadeInicial;
    }

    public void setQuantidadeInicial(BigDecimal quantidadeInicial) {
        this.quantidadeInicial = quantidadeInicial;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(LocalDateTime dataEntrada) {
        this.dataEntrada = dataEntrada;
    }
}
