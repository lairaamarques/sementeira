package br.edu.ifam.sementeira.service;

import br.edu.ifam.sementeira.dto.IndicadoresDTO;
import br.edu.ifam.sementeira.dto.IndicadoresDTO.Alerta;
import br.edu.ifam.sementeira.dto.IndicadoresDTO.EstoqueEspecie;
import br.edu.ifam.sementeira.model.Estoque;
import br.edu.ifam.sementeira.model.Venda;
import br.edu.ifam.sementeira.repository.EspecieRepository;
import br.edu.ifam.sementeira.repository.EstoqueRepository;
import br.edu.ifam.sementeira.repository.NomeCientificoRepository;
import br.edu.ifam.sementeira.repository.SementeRepository;
import br.edu.ifam.sementeira.repository.VendaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IndicadoresService {

    private static final BigDecimal LIMITE_ESTOQUE_BAIXO = new BigDecimal("0.2");

    private final EstoqueRepository estoqueRepository;
    private final VendaRepository vendaRepository;
    private final SementeRepository sementeRepository;
    private final EspecieRepository especieRepository;
    private final NomeCientificoRepository nomeCientificoRepository;

    public IndicadoresService(EstoqueRepository estoqueRepository,
                              VendaRepository vendaRepository,
                              SementeRepository sementeRepository,
                              EspecieRepository especieRepository,
                              NomeCientificoRepository nomeCientificoRepository) {
        this.estoqueRepository = estoqueRepository;
        this.vendaRepository = vendaRepository;
        this.sementeRepository = sementeRepository;
        this.especieRepository = especieRepository;
        this.nomeCientificoRepository = nomeCientificoRepository;
    }

    private long contarMatrizes(Long especieId) {
        if (especieId == null) {
            return sementeRepository.count();
        }
        return especieRepository.findById(especieId)
                .flatMap(e -> nomeCientificoRepository.findFirstByNomeIgnoreCase(e.getNomeCientifico()))
                .map(nc -> sementeRepository.countByNomeCientificoId(nc.getId()))
                .orElse(0L);
    }

    public IndicadoresDTO calcular(Long especieId) {
        List<Estoque> lotes = estoqueRepository.findAll().stream()
                .filter(l -> especieId == null || l.getEspecie().getId().equals(especieId))
                .toList();
        List<Venda> vendas = vendaRepository.findAll().stream()
                .filter(v -> "Concluído".equals(v.getStatus()))
                .filter(v -> especieId == null || v.getEstoque().getEspecie().getId().equals(especieId))
                .toList();

        BigDecimal sacas = somaSaldo(lotes, "Sacas");
        BigDecimal mudas = somaSaldo(lotes, "Unidades");

        BigDecimal faturamentoEstimado = lotes.stream()
                .filter(l -> l.getEspecie().getValorMercado() != null)
                .map(l -> l.getSaldo().multiply(l.getEspecie().getValorMercado()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal faturamentoRealizado = vendas.stream()
                .map(Venda::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        YearMonth mesAtual = YearMonth.now();
        List<Venda> vendasDoMes = vendas.stream()
                .filter(v -> YearMonth.from(v.getDataVenda()).equals(mesAtual))
                .toList();
        BigDecimal faturamentoMes = vendasDoMes.stream()
                .map(Venda::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new IndicadoresDTO(
                contarMatrizes(especieId),
                sacas,
                mudas,
                faturamentoEstimado,
                faturamentoRealizado,
                faturamentoMes,
                vendasDoMes.size(),
                estoquePorEspecie(lotes),
                alertas(lotes, vendas)
        );
    }

    private static BigDecimal somaSaldo(List<Estoque> lotes, String unidade) {
        return lotes.stream()
                .filter(l -> unidade.equals(l.getUnidade()))
                .map(Estoque::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static List<EstoqueEspecie> estoquePorEspecie(List<Estoque> lotes) {
        Map<String, EstoqueEspecie> grupos = new LinkedHashMap<>();
        for (Estoque l : lotes) {
            String chave = l.getEspecie().getId() + "|" + l.getUnidade();
            grupos.merge(chave,
                    new EstoqueEspecie(l.getEspecie().getNomePopular(), l.getEspecie().getTipoRecurso(),
                            l.getUnidade(), l.getSaldo()),
                    (a, b) -> new EstoqueEspecie(a.especie(), a.tipoRecurso(), a.unidade(), a.saldo().add(b.saldo())));
        }
        return grupos.values().stream()
                .sorted(Comparator.comparing(EstoqueEspecie::saldo).reversed())
                .toList();
    }

    private static List<Alerta> alertas(List<Estoque> lotes, List<Venda> vendas) {
        List<Alerta> alertas = new ArrayList<>();

        LocalDateTime trintaDias = LocalDateTime.now().minusDays(30);
        vendas.stream()
                .filter(v -> v.getDataVenda().isAfter(trintaDias))
                .collect(Collectors.groupingBy(v -> v.getEstoque().getEspecie().getNomePopular(),
                        Collectors.reducing(BigDecimal.ZERO, Venda::getValorTotal, BigDecimal::add)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(e -> alertas.add(new Alerta("warn", "Alta Demanda: " + e.getKey(),
                        "Espécie com maior faturamento nos últimos 30 dias ("
                                + NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(e.getValue()) + ").")));

        for (Estoque l : lotes) {
            String nome = l.getEspecie().getNomePopular() + " (" + l.getFamiliaProdutora().getNome() + ")";
            if (l.getSaldo().signum() == 0) {
                alertas.add(new Alerta("warn", "Estoque Esgotado: " + nome,
                        "Não há mais " + l.getUnidade().toLowerCase() + " disponíveis neste lote."));
            } else if (l.getSaldo().compareTo(l.getQuantidadeInicial().multiply(LIMITE_ESTOQUE_BAIXO)) < 0) {
                alertas.add(new Alerta("warn", "Estoque Baixo: " + nome,
                        "Restam " + l.getSaldo().stripTrailingZeros().toPlainString() + " "
                                + l.getUnidade().toLowerCase() + "."));
            }
        }

        if (alertas.isEmpty() && !lotes.isEmpty()) {
            alertas.add(new Alerta("ok", "Estoque Saudável", "Nenhum lote está esgotado ou com saldo baixo."));
        }
        return alertas;
    }
}
