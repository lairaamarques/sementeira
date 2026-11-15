package br.edu.ifam.sementeira.service;

import br.edu.ifam.sementeira.dto.EntradaEstoqueDTO;
import br.edu.ifam.sementeira.dto.SaidaEstoqueDTO;
import br.edu.ifam.sementeira.exception.RegraNegocioException;
import br.edu.ifam.sementeira.model.Especie;
import br.edu.ifam.sementeira.model.Estoque;
import br.edu.ifam.sementeira.model.FamiliaProdutora;
import br.edu.ifam.sementeira.model.MovimentacaoEstoque;
import br.edu.ifam.sementeira.model.NomeCientifico;
import br.edu.ifam.sementeira.model.Semente;
import br.edu.ifam.sementeira.repository.EstoqueRepository;
import br.edu.ifam.sementeira.repository.FamiliaProdutoraRepository;
import br.edu.ifam.sementeira.repository.MovimentacaoEstoqueRepository;
import br.edu.ifam.sementeira.repository.SementeRepository;
import br.edu.ifam.sementeira.repository.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class EstoqueService {

    public static final Set<String> UNIDADES = Set.of("Sacas", "Unidades", "Quilos", "Litros");
    public static final Set<String> TIPOS_SAIDA = Set.of("BAIXA", "PERDA");

    private final EstoqueRepository estoqueRepository;
    private final FamiliaProdutoraRepository familiaRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final VendaRepository vendaRepository;
    private final EspecieService especieService;
    private final SementeRepository sementeRepository;

    public EstoqueService(EstoqueRepository estoqueRepository,
                          FamiliaProdutoraRepository familiaRepository,
                          MovimentacaoEstoqueRepository movimentacaoRepository,
                          VendaRepository vendaRepository,
                          EspecieService especieService,
                          SementeRepository sementeRepository) {
        this.estoqueRepository = estoqueRepository;
        this.familiaRepository = familiaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.vendaRepository = vendaRepository;
        this.especieService = especieService;
        this.sementeRepository = sementeRepository;
    }

    public List<Estoque> listar() {
        return estoqueRepository.findAllByOrderByDataEntradaDesc();
    }

    public List<MovimentacaoEstoque> listarMovimentacoes() {
        return movimentacaoRepository.findAllByOrderByDataDesc();
    }

    public List<FamiliaProdutora> listarFamilias() {
        return familiaRepository.findAllByOrderByNomeAsc();
    }

    public Estoque buscar(Long id) {
        return estoqueRepository.findById(id).orElseThrow(() -> RegraNegocioException.naoEncontrado("Lote de estoque"));
    }

    @Transactional
    public Estoque lancarEntrada(EntradaEstoqueDTO dto) {
        if (dto.quantidade() == null || dto.quantidade().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("Informe a quantidade (maior que zero)");
        }
        if (dto.especieId() == null) {
            throw new RegraNegocioException("Selecione a espécie");
        }
        if (dto.unidade() == null || !UNIDADES.contains(dto.unidade())) {
            throw new RegraNegocioException("Unidade de medida inválida");
        }
        if (dto.familiaProdutora() == null || dto.familiaProdutora().isBlank()) {
            throw new RegraNegocioException("Informe a família produtora");
        }
        Especie especie = especieService.buscar(dto.especieId());
        if (!especie.isAtivo()) {
            throw new RegraNegocioException("Esta espécie está inativa e não aceita novos estoques");
        }
        Semente matriz = buscarMatrizDeOrigem(dto.sementeId(), especie);

        String nomeFamilia = dto.familiaProdutora().trim();
        FamiliaProdutora familia = familiaRepository.findByNomeIgnoreCase(nomeFamilia)
                .orElseGet(() -> familiaRepository.save(new FamiliaProdutora(nomeFamilia)));

        Estoque lote = new Estoque();
        lote.setEspecie(especie);
        lote.setSemente(matriz);
        lote.setFamiliaProdutora(familia);
        lote.setUnidade(dto.unidade());
        lote.setQuantidadeInicial(dto.quantidade());
        lote.setSaldo(dto.quantidade());
        return estoqueRepository.save(lote);
    }

    private Semente buscarMatrizDeOrigem(Long sementeId, Especie especie) {
        if (sementeId == null) {
            return null;
        }
        Semente matriz = sementeRepository.findById(sementeId)
                .orElseThrow(() -> RegraNegocioException.naoEncontrado("Matriz de origem"));
        NomeCientifico nc = matriz.getNomeCientifico();
        if (nc != null && nc.getNome() != null && !nc.getNome().equalsIgnoreCase(especie.getNomeCientifico())) {
            throw new RegraNegocioException("A matriz escolhida é de " + nc.getNome()
                    + ", não de " + especie.getNomeCientifico());
        }
        return matriz;
    }

    @Transactional
    public void desfazerEntrada(Long id) {
        Estoque lote = buscar(id);
        if (vendaRepository.existsByEstoqueId(id) || movimentacaoRepository.existsByEstoqueId(id)) {
            throw new RegraNegocioException(
                    "Este lote já tem vendas ou baixas. Desfaça-as antes de remover a entrada");
        }
        estoqueRepository.delete(lote);
    }

    @Transactional
    public MovimentacaoEstoque registrarSaida(Long estoqueId, SaidaEstoqueDTO dto) {
        if (dto.tipo() == null || !TIPOS_SAIDA.contains(dto.tipo())) {
            throw new RegraNegocioException("Tipo de saída inválido (use BAIXA ou PERDA)");
        }
        if (dto.quantidade() == null || dto.quantidade().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("Informe a quantidade (maior que zero)");
        }
        Estoque lote = buscar(estoqueId);
        lote.decrementarSaldo(dto.quantidade());
        estoqueRepository.save(lote);

        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setEstoque(lote);
        mov.setTipo(dto.tipo());
        mov.setQuantidade(dto.quantidade());
        mov.setObservacao(dto.observacao());
        return movimentacaoRepository.save(mov);
    }

    @Transactional
    public void desfazerSaida(Long movimentacaoId) {
        MovimentacaoEstoque mov = movimentacaoRepository.findById(movimentacaoId)
                .orElseThrow(() -> RegraNegocioException.naoEncontrado("Movimentação"));
        Estoque lote = mov.getEstoque();
        lote.incrementarSaldo(mov.getQuantidade());
        estoqueRepository.save(lote);
        movimentacaoRepository.delete(mov);
    }
}
