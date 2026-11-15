package br.edu.ifam.sementeira.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "especie")
public class Especie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomePopular;

    @Column(nullable = false)
    private String nomeCientifico;

    @Column(nullable = false)
    private String tipoRecurso;

    private String tempoMaturacao;

    private BigDecimal valorMercado;

    @Column(nullable = false)
    private boolean ativo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomePopular() { return nomePopular; }
    public void setNomePopular(String nomePopular) { this.nomePopular = nomePopular; }

    public String getNomeCientifico() { return nomeCientifico; }
    public void setNomeCientifico(String nomeCientifico) { this.nomeCientifico = nomeCientifico; }

    public String getTipoRecurso() { return tipoRecurso; }
    public void setTipoRecurso(String tipoRecurso) { this.tipoRecurso = tipoRecurso; }

    public String getTempoMaturacao() { return tempoMaturacao; }
    public void setTempoMaturacao(String tempoMaturacao) { this.tempoMaturacao = tempoMaturacao; }

    public BigDecimal getValorMercado() { return valorMercado; }
    public void setValorMercado(BigDecimal valorMercado) { this.valorMercado = valorMercado; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
