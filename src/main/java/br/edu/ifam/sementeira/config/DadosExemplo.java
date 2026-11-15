package br.edu.ifam.sementeira.config;

import br.edu.ifam.sementeira.dto.EntradaEstoqueDTO;
import br.edu.ifam.sementeira.model.Especie;
import br.edu.ifam.sementeira.repository.EspecieRepository;
import br.edu.ifam.sementeira.service.EspecieService;
import br.edu.ifam.sementeira.service.EstoqueService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(name = "sementeira.dados-exemplo", havingValue = "true")
public class DadosExemplo implements CommandLineRunner {

    private final EspecieRepository especieRepository;
    private final EspecieService especieService;
    private final EstoqueService estoqueService;

    public DadosExemplo(EspecieRepository especieRepository, EspecieService especieService,
                        EstoqueService estoqueService) {
        this.especieRepository = especieRepository;
        this.especieService = especieService;
        this.estoqueService = estoqueService;
    }

    @Override
    public void run(String... args) {
        if (especieRepository.count() > 0) {
            return;
        }
        Especie andiroba = especie("Andiroba", "Carapa guianensis", "Sementes", "4–6 meses", "150.00");
        Especie ucuuba = especie("Ucuúba", "Virola sebifera", "Mudas", "8 meses", "7.00");
        Especie acai = especie("Açaí", "Euterpe precatoria", "Frutos", "6 meses", "90.00");
        especie("Copaíba", "Copaifera officinalis", "Óleo", "Contínuo", "60.00");

        estoqueService.lancarEntrada(new EntradaEstoqueDTO(andiroba.getId(), new BigDecimal("32"), "Sacas", "Família Souza – Rio Negro"));
        estoqueService.lancarEntrada(new EntradaEstoqueDTO(ucuuba.getId(), new BigDecimal("450"), "Unidades", "Família Marins – Costa do Sol"));
        estoqueService.lancarEntrada(new EntradaEstoqueDTO(acai.getId(), new BigDecimal("25"), "Sacas", "Família Lima – Igarapé Açú"));
    }

    private Especie especie(String popular, String cientifico, String recurso, String maturacao, String valor) {
        Especie e = new Especie();
        e.setNomePopular(popular);
        e.setNomeCientifico(cientifico);
        e.setTipoRecurso(recurso);
        e.setTempoMaturacao(maturacao);
        e.setValorMercado(new BigDecimal(valor));
        return especieService.cadastrar(e);
    }
}
