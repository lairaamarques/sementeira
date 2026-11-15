package br.edu.ifam.sementeira.service;

import br.edu.ifam.sementeira.dto.VendaDTO;
import br.edu.ifam.sementeira.exception.RegraNegocioException;
import br.edu.ifam.sementeira.model.Estoque;
import br.edu.ifam.sementeira.model.Venda;
import br.edu.ifam.sementeira.repository.EstoqueRepository;
import br.edu.ifam.sementeira.repository.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class VendaService {

    private final VendaRepository vendaRepository;
    private final EstoqueRepository estoqueRepository;
    private final EstoqueService estoqueService;

    public VendaService(VendaRepository vendaRepository,
                        EstoqueRepository estoqueRepository,
                        EstoqueService estoqueService) {
        this.vendaRepository = vendaRepository;
        this.estoqueRepository = estoqueRepository;
        this.estoqueService = estoqueService;
    }

    public List<Venda> listar() {
        return vendaRepository.findAllByOrderByDataVendaDesc();
    }

    @Transactional
    public Venda registrar(VendaDTO dto) {
        if (dto.estoqueId() == null) {
            throw new RegraNegocioException("Selecione o produto em estoque");
        }
        if (dto.quantidade() == null || dto.quantidade().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("Informe a quantidade (maior que zero)");
        }
        if (dto.valorTotal() == null || dto.valorTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("Informe o valor total da venda");
        }
        if (dto.comprador() == null || dto.comprador().isBlank()) {
            throw new RegraNegocioException("Informe o comprador");
        }

        Estoque lote = estoqueService.buscar(dto.estoqueId());
        lote.decrementarSaldo(dto.quantidade());
        estoqueRepository.save(lote);

        Venda venda = new Venda();
        venda.setEstoque(lote);
        venda.setQuantidade(dto.quantidade());
        venda.setValorTotal(dto.valorTotal());
        venda.setComprador(dto.comprador().trim());
        return vendaRepository.save(venda);
    }

    @Transactional
    public Venda cancelar(Long id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> RegraNegocioException.naoEncontrado("Venda"));
        if ("Cancelado".equals(venda.getStatus())) {
            throw new RegraNegocioException("Esta venda já foi cancelada");
        }
        Estoque lote = venda.getEstoque();
        lote.incrementarSaldo(venda.getQuantidade());
        estoqueRepository.save(lote);
        venda.setStatus("Cancelado");
        return vendaRepository.save(venda);
    }
}
