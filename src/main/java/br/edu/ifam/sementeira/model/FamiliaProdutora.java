package br.edu.ifam.sementeira.model;

import jakarta.persistence.*;

@Entity
@Table(name = "familia_produtora")
public class FamiliaProdutora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    public FamiliaProdutora() {}

    public FamiliaProdutora(String nome) { this.nome = nome; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
