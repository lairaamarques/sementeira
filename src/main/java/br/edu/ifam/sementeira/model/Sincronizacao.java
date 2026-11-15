package br.edu.ifam.sementeira.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sincronizacao")
public class Sincronizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dispositivo;

    @Column(nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();

    private int recebidas;
    private int criadas;
    private int atualizadas;
    private int ignoradas;
    private int erros;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDispositivo() { return dispositivo; }
    public void setDispositivo(String dispositivo) { this.dispositivo = dispositivo; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public int getRecebidas() { return recebidas; }
    public void setRecebidas(int recebidas) { this.recebidas = recebidas; }

    public int getCriadas() { return criadas; }
    public void setCriadas(int criadas) { this.criadas = criadas; }

    public int getAtualizadas() { return atualizadas; }
    public void setAtualizadas(int atualizadas) { this.atualizadas = atualizadas; }

    public int getIgnoradas() { return ignoradas; }
    public void setIgnoradas(int ignoradas) { this.ignoradas = ignoradas; }

    public int getErros() { return erros; }
    public void setErros(int erros) { this.erros = erros; }
}
