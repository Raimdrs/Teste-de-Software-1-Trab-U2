package ecommerce.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.TipoCliente;
import ecommerce.entity.TipoProduto;
import ecommerce.entity.Regiao;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import jakarta.transaction.Transactional;

@Service
public class CompraService
{

	private final CarrinhoDeComprasService carrinhoService;
	private final ClienteService clienteService;

	private final IEstoqueExternal estoqueExternal;
	private final IPagamentoExternal pagamentoExternal;

	@Autowired
	public CompraService(CarrinhoDeComprasService carrinhoService, ClienteService clienteService,
			IEstoqueExternal estoqueExternal, IPagamentoExternal pagamentoExternal)
	{
		this.carrinhoService = carrinhoService;
		this.clienteService = clienteService;

		this.estoqueExternal = estoqueExternal;
		this.pagamentoExternal = pagamentoExternal;
	}

	@Transactional
	public CompraDTO finalizarCompra(Long carrinhoId, Long clienteId)
	{
		Cliente cliente = clienteService.buscarPorId(clienteId);
		CarrinhoDeCompras carrinho = carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);

		List<Long> produtosIds = carrinho.getItens().stream().map(i -> i.getProduto().getId())
				.collect(Collectors.toList());
		List<Long> produtosQtds = carrinho.getItens().stream().map(i -> i.getQuantidade()).collect(Collectors.toList());

		DisponibilidadeDTO disponibilidade = estoqueExternal.verificarDisponibilidade(produtosIds, produtosQtds);

		if (!disponibilidade.disponivel())
		{
			throw new IllegalStateException("Itens fora de estoque.");
		}

		// A chamada foi ajustada para passar o objeto 'cliente' inteiro.
		BigDecimal custoTotal = calcularCustoTotal(carrinho, cliente);

		PagamentoDTO pagamento = pagamentoExternal.autorizarPagamento(cliente.getId(), custoTotal.doubleValue());

		if (!pagamento.autorizado())
		{
			throw new IllegalStateException("Pagamento não autorizado.");
		}

		EstoqueBaixaDTO baixaDTO = estoqueExternal.darBaixa(produtosIds, produtosQtds);

		if (!baixaDTO.sucesso())
		{
			pagamentoExternal.cancelarPagamento(cliente.getId(), pagamento.transacaoId());
			throw new IllegalStateException("Erro ao dar baixa no estoque.");
		}

		CompraDTO compraDTO = new CompraDTO(true, pagamento.transacaoId(), "Compra finalizada com sucesso.");

		return compraDTO;
	}

	/**
	 * Calcula o custo total da compra com base nas regras de negócio.
	 * A assinatura foi ajustada para receber o Cliente para ter acesso a todas as informações necessárias.
	 * @param carrinho O carrinho de compras.
	 * @param cliente O cliente que está realizando a compra.
	 * @return O valor total da compra, arredondado para duas casas decimais.
	 */
	public BigDecimal calcularCustoTotal(CarrinhoDeCompras carrinho, Cliente cliente)
	{
		// Validação básica
		Objects.requireNonNull(carrinho, "Carrinho não pode ser nulo.");
		Objects.requireNonNull(cliente, "Cliente não pode ser nulo.");
		if (carrinho.getItens() == null || carrinho.getItens().isEmpty()) {
			throw new IllegalArgumentException("Carrinho não pode estar vazio.");
		}
		validarItens(carrinho);

		// Passo 1: Calcular Subtotal dos itens 
		BigDecimal subtotal = calcularSubtotal(carrinho);

		// Passo 2: Aplicar desconto por valor de carrinho 
		BigDecimal subtotalComDesconto = aplicarDescontoPorValor(subtotal);

		// Passo 3: Calcular frete base e Passo 4: Aplicar benefício de nível do cliente 
		BigDecimal freteFinal = calcularFrete(carrinho, cliente);

		// Passo 5: Calcular o total da compra
		BigDecimal total = subtotalComDesconto.add(freteFinal); 

		// Arredondamento final para duas casas decimais
		return total.setScale(2, RoundingMode.HALF_UP);
	}

	// MÉTODOS AUXILIARES
	private static final BigDecimal SUBTOTAL_LIMITE_DESCONTO_10 = new BigDecimal("500.00");
	private static final BigDecimal SUBTOTAL_LIMITE_DESCONTO_20 = new BigDecimal("1000.00");
	private static final BigDecimal DESCONTO_10_PORCENTO = new BigDecimal("0.10");
	private static final BigDecimal DESCONTO_20_PORCENTO = new BigDecimal("0.20");
	private static final double FATOR_PESO_CUBICO = 6000.0;
	private static final BigDecimal TAXA_MINIMA_FRETE = new BigDecimal("12.00");
	private static final BigDecimal TAXA_ITEM_FRAGIL = new BigDecimal("5.00");
	private static final BigDecimal DESCONTO_FRETE_PRATA = new BigDecimal("0.50");

	private void validarItens(CarrinhoDeCompras carrinho) {
		for (ItemCompra item : carrinho.getItens()) {
			if (item.getQuantidade() <= 0) {
				throw new IllegalArgumentException("A quantidade do item deve ser positiva."); 
			}
			if (item.getProduto().getPrecoUnitario().compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalArgumentException("O preço do item não pode ser negativo.");
			}
		}
	}

	private BigDecimal calcularSubtotal(CarrinhoDeCompras carrinho) {
		return carrinho.getItens().stream()
				.map(item -> item.getProduto().getPrecoUnitario().multiply(new BigDecimal(item.getQuantidade()))) 
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal aplicarDescontoPorValor(BigDecimal subtotal) {
		if (subtotal.compareTo(SUBTOTAL_LIMITE_DESCONTO_20) > 0) { // Se Subtotal > R$ 1000,00 -> 20% de desconto 
			return subtotal.multiply(BigDecimal.ONE.subtract(DESCONTO_20_PORCENTO));
		} else if (subtotal.compareTo(SUBTOTAL_LIMITE_DESCONTO_10) > 0) { // Se Subtotal > R$ 500,00 -> 10% de desconto 
			return subtotal.multiply(BigDecimal.ONE.subtract(DESCONTO_10_PORCENTO));
		}
		return subtotal; // Se não for nenhum dos casos, retorna o subtotal sem desconto
	}

	private BigDecimal calcularFrete(CarrinhoDeCompras carrinho, Cliente cliente) {
		Regiao regiao = cliente.getRegiao();
		TipoCliente nivelFidelidade = cliente.getNivelFidelidade();

		// Cálculo do peso total tributável da compra 
		double pesoTotalTributavel = carrinho.getItens().stream()
			.mapToDouble(item -> {
				Produto produto = item.getProduto();
				double pesoCubico = (produto.getComprimento() * produto.getLargura() * produto.getAltura()) / FATOR_PESO_CUBICO; 
				double pesoTributavel = Math.max(produto.getPesoFisico(), pesoCubico);
				return pesoTributavel * item.getQuantidade();
			}).sum();

		// Cálculo do frete por faixas de peso 
		BigDecimal freteBase = BigDecimal.ZERO;
		if (pesoTotalTributavel > 50.0) {
			freteBase = new BigDecimal(pesoTotalTributavel).multiply(new BigDecimal("7.00"));
		} else if (pesoTotalTributavel > 10.0) {
			freteBase = new BigDecimal(pesoTotalTributavel).multiply(new BigDecimal("4.00"));
		} else if (pesoTotalTributavel > 5.0) {
			freteBase = new BigDecimal(pesoTotalTributavel).multiply(new BigDecimal("2.00"));
		}

		// Adição de taxas extras
		if (freteBase.compareTo(BigDecimal.ZERO) > 0) {
			freteBase = freteBase.add(TAXA_MINIMA_FRETE); // Taxa mínima de R$ 12,00 
		}

		for (ItemCompra item : carrinho.getItens()) {
			if (item.getProduto().isFragil()) {
				freteBase = freteBase.add(TAXA_ITEM_FRAGIL.multiply(new BigDecimal(item.getQuantidade()))); // Taxa de manuseio especial 
			}
		}

		// Multiplicação pelo fator da região 
		BigDecimal freteComRegiao = freteBase.multiply(regiao.getMultiplicador());

		// Aplicação do benefício de nível do cliente 
		switch (nivelFidelidade) {
			case OURO:
				return BigDecimal.ZERO; // 100% de desconto no frete 
			case PRATA:
				return freteComRegiao.multiply(DESCONTO_FRETE_PRATA); // 50% de desconto sobre o frete 
			case BRONZE:
			default:
				return freteComRegiao; // Paga o frete integral 
		}
	}
}