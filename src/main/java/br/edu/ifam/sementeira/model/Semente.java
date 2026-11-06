package br.edu.ifam.sementeira.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sementes")
public class Semente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Campos Antigos do DTO do App
    private String nome;
    private String descricao;
    private String especie;
    private Integer tempoMedioColheita;
    private Integer quantidade;
    private String cuidado;
    private Float preco;

    // Novos campos adicionados (Banco v7 do App)
    @Column(name = "nome_cientifico_id")
    private Long nomeCientificoId; // Mapeado diretamente como ID para facilitar o tráfego do JSON

    private String epocaInicio;
    private String epocaFim;
    private String tipoCultivo;
    private String tamanhoPorte;
    private Double latitude;
    private Double longitude;
    private String caminhoImagem;

    public Semente() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getTempoMedioColheita() {
        return tempoMedioColheita;
    }

    public void setTempoMedioColheita(Integer tempoMedioColheita) {
        this.tempoMedioColheita = tempoMedioColheita;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public String getCuidado() {
        return cuidado;
    }

    public void setCuidado(String cuidado) {
        this.cuidado = cuidado;
    }

    public Float getPreco() {
        return preco;
    }

    public void setPreco(Float preco) {
        this.preco = preco;
    }

    public Long getNomeCientificoId() {
        return nomeCientificoId;
    }

    public void setNomeCientificoId(Long nomeCientificoId) {
        this.nomeCientificoId = nomeCientificoId;
    }

    public String getEpocaInicio() {
        return epocaInicio;
    }

    public void setEpocaInicio(String epocaInicio) {
        this.epocaInicio = epocaInicio;
    }

    public String getEpocaFim() {
        return epocaFim;
    }

    public void setEpocaFim(String epocaFim) {
        this.epocaFim = epocaFim;
    }

    public String getTipoCultivo() {
        return tipoCultivo;
    }

    public void setTipoCultivo(String tipoCultivo) {
        this.tipoCultivo = tipoCultivo;
    }

    public String getTamanhoPorte() {
        return tamanhoPorte;
    }

    public void setTamanhoPorte(String tamanhoPorte) {
        this.tamanhoPorte = tamanhoPorte;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getCaminhoImagem() {
        return caminhoImagem;
    }

    public void setCaminhoImagem(String caminhoImagem) {
        this.caminhoImagem = caminhoImagem;
    }
}
