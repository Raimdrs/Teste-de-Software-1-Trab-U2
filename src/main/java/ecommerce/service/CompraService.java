package ecommerce.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.math.MathContext; 
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

		BigDecimal custoTotal = calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());

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
	public BigDecimal calcularCustoTotal(CarrinhoDeCompras carrinho, Regiao regiao, TipoCliente tipoCliente){
		// Validação básica
		Objects.requireNonNull(carrinho, "Carrinho não pode ser nulo.");
		Objects.requireNonNull(regiao, "Região não pode ser nula.");
		Objects.requireNonNull(tipoCliente, "Tipo de cliente não pode ser nulo.");

		if (carrinho.getItens() == null || carrinho.getItens().isEmpty()) {
			throw new IllegalArgumentException("Carrinho não pode estar vazio.");
		}

		validarItens(carrinho);

        // Passo 1 Aplicar possível desconto por itens múltiplos
        BigDecimal subtotalDescontoPorItem = calcularSubtotalComDescontoPorItens(carrinho);

		// Passo 2: Aplicar desconto por valor de carrinho
		BigDecimal subtotalComDesconto = aplicarDescontoPorValor(subtotalDescontoPorItem);

		// Passo 3 e 4: Calcular frete + benefício do nível do cliente
		BigDecimal freteFinal = calcularFrete(carrinho, regiao, tipoCliente);

		// Passo 5: Calcular o total da compra
		BigDecimal total = subtotalComDesconto.add(freteFinal);

		// Arredondamento final para duas casas decimais
		return total.setScale(2, RoundingMode.HALF_UP);
	}

	// MÉTODOS AUXILIARES
	private static final BigDecimal SUBTOTAL_LIMITE_DESCONTO_10 = new BigDecimal("500.00");
	private static final BigDecimal SUBTOTAL_LIMITE_DESCONTO_20 = new BigDecimal("1000.00");
    private static final BigDecimal DESCONTO_5_PORCENTO = new BigDecimal("0.05");
	private static final BigDecimal DESCONTO_10_PORCENTO = new BigDecimal("0.10");
    private static final BigDecimal DESCONTO_15_PORCENTO = new BigDecimal("0.15");
	private static final BigDecimal DESCONTO_20_PORCENTO = new BigDecimal("0.20");
	private static final BigDecimal FATOR_PESO_CUBICO = new BigDecimal("6000.0");
	
	private static final BigDecimal TAXA_MINIMA_FRETE = new BigDecimal("12.00");
	private static final BigDecimal TAXA_ITEM_FRAGIL = new BigDecimal("5.00");
	private static final BigDecimal DESCONTO_FRETE_PRATA = new BigDecimal("0.50");

	private static final BigDecimal PESO_LIMITE_50 = new BigDecimal("50.0");
	private static final BigDecimal PESO_LIMITE_10 = new BigDecimal("10.0");
	private static final BigDecimal PESO_LIMITE_5 = new BigDecimal("5.0");
	private static final BigDecimal FATOR_FRETE_7 = new BigDecimal("7.00");
	private static final BigDecimal FATOR_FRETE_4 = new BigDecimal("4.00");
	private static final BigDecimal FATOR_FRETE_2 = new BigDecimal("2.00");


	public void validarItens(CarrinhoDeCompras carrinho) {
		for (ItemCompra item : carrinho.getItens()) {
			Produto produto = item.getProduto();
			
			// getQuantidade() retorna Long, a comparação com 0 (int) é válida.
			if (item.getQuantidade() <= 0) {
				throw new IllegalArgumentException("A quantidade do item deve ser positiva.");
			}
			if (item.getProduto().getPrecoUnitario().compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalArgumentException("O preço do item não pode ser negativo.");
			}
			
			if (produto.getPesoFisico().compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("O peso físico do item deve ser positivo.");
			}

			if (produto.getComprimento().compareTo(BigDecimal.ZERO) <= 0 ||
				produto.getLargura().compareTo(BigDecimal.ZERO) <= 0 ||
				produto.getAltura().compareTo(BigDecimal.ZERO) <= 0) {
					
				throw new IllegalArgumentException("As dimensões (C, L, A) do item devem ser positivas.");
			}
		}
	}

    public BigDecimal aplicarDescontoPorValor(BigDecimal subtotal) {
		if (subtotal.compareTo(SUBTOTAL_LIMITE_DESCONTO_20) > 0) {
			BigDecimal temp = subtotal.multiply(BigDecimal.ONE.subtract(DESCONTO_20_PORCENTO));
            return temp.setScale(2, RoundingMode.HALF_UP);
		} else if (subtotal.compareTo(SUBTOTAL_LIMITE_DESCONTO_10) > 0) {
            BigDecimal temp = subtotal.multiply(BigDecimal.ONE.subtract(DESCONTO_10_PORCENTO));
            return temp.setScale(2, RoundingMode.HALF_UP);
		}
		return subtotal;
	}

    public BigDecimal calcularSubtotalComDescontoPorItens(CarrinhoDeCompras carrinho) {
        final var qtdPorTipo = new java.util.HashMap<TipoProduto, Long>();
        final var subtotalPorTipo = new java.util.HashMap<TipoProduto, BigDecimal>();

        for (ItemCompra item : carrinho.getItens()) {
            final Produto p = item.getProduto();
            final TipoProduto tipo = p.getTipo();
            final Long qtd = item.getQuantidade();

            final BigDecimal valorItens =
                    p.getPrecoUnitario().multiply(BigDecimal.valueOf(qtd));

            qtdPorTipo.merge(tipo, qtd, Long::sum);
            subtotalPorTipo.merge(tipo, valorItens, BigDecimal::add);
        }

        BigDecimal subtotalComDesconto = BigDecimal.ZERO;

        for (var entry : subtotalPorTipo.entrySet()) {
            final TipoProduto tipo = entry.getKey();
            final BigDecimal subtotalGrupo = entry.getValue();
            final long qtdGrupo = qtdPorTipo.getOrDefault(tipo, 0L);

            final BigDecimal fatorDesconto = descontoPorQuantidade(qtdGrupo);
            final BigDecimal multiplicador = BigDecimal.ONE.subtract(fatorDesconto);

            subtotalComDesconto = subtotalComDesconto.add(subtotalGrupo.multiply(multiplicador));
        }

        return subtotalComDesconto.setScale(2, RoundingMode.HALF_UP);
    }

    /** Percentual de desconto (0.00, 0.05, 0.10, 0.15) conforme a quantidade do grupo. */
    public BigDecimal descontoPorQuantidade(long qtdGrupo) {
        if (qtdGrupo >= 8L) {
            return DESCONTO_15_PORCENTO;
        } else if (qtdGrupo >= 5L) {
            return DESCONTO_10_PORCENTO;
        } else if (qtdGrupo >= 3L) {
            return DESCONTO_5_PORCENTO;
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal calcularFrete(CarrinhoDeCompras carrinho, Regiao regiao, TipoCliente tipoCliente) {
		// 1. Calcular peso total tributável
    	BigDecimal pesoTotalTributavel = calcularPesoTotalTributavel(carrinho);

		// 2. Calcular frete base (baseado no peso + taxa mínima)
		BigDecimal freteBase = calcularFreteBasePorPeso(pesoTotalTributavel);

		// 3. Adicionar taxa de itens frágeis
		BigDecimal taxaFragil = calcularTaxaItensFrageis(carrinho);

		// 4. Somar frete base e taxas
		BigDecimal freteAntesDaRegiao = freteBase.add(taxaFragil);

		// 5. Multiplicação pelo fator da região
		BigDecimal freteComRegiao = freteAntesDaRegiao.multiply(regiao.getMultiplicador());

    	// 6. Aplicar desconto de fidelidade
		return aplicarDescontoFidelidadeFrete(freteComRegiao, tipoCliente);
	}

	/*
	 * Calcula o peso total tributável do carrinho, considerando o maior valor entre
	 peso físico e peso cúbico (cubagem) de cada item.
	 */
    public BigDecimal calcularPesoTotalTributavel(CarrinhoDeCompras carrinho) {
		return carrinho.getItens().stream()
				.map(this::calcularPesoTributavelItem)
				.reduce(BigDecimal.ZERO, BigDecimal::add); // Soma BigDecimals
	}

	/**
	 * Calcula o peso tributável para um único ItemCompra (unidade * quantidade).
	 */
    public BigDecimal calcularPesoTributavelItem(ItemCompra item) {
		Produto produto = item.getProduto();

		// 1. Obter todos os valores
        BigDecimal comprimento = produto.getComprimento();
        BigDecimal largura = produto.getLargura();
        BigDecimal altura = produto.getAltura();
        BigDecimal pesoFisico = produto.getPesoFisico();

        // 3. Calcular o volume (C * L * A) usando .multiply()
        BigDecimal volume = comprimento.multiply(largura).multiply(altura);

        // 4. Calcular o peso cúbico (Volume / Fator) usando .divide()
        BigDecimal pesoCubico = volume.divide(FATOR_PESO_CUBICO, MathContext.DECIMAL64);

        // 5. Obter o maior peso (físico ou cúbico) usando .max()
        BigDecimal pesoTributavelUnidade = pesoFisico.max(pesoCubico);

        // 6. Converter a quantidade (Long) para BigDecimal
        BigDecimal quantidade = BigDecimal.valueOf(item.getQuantidade());

        // 7. Calcular o peso total final
        BigDecimal pesoTotal = pesoTributavelUnidade.multiply(quantidade);

        // 8. Retornar como BigDecimal
		return pesoTotal;
	}

	/**
	 * Calcula o valor do frete base com base nas faixas de peso e aplica a taxa mínima.
	 */
    public BigDecimal calcularFreteBasePorPeso(BigDecimal pesoTotalTributavel) {
		BigDecimal freteBase = BigDecimal.ZERO;

		if (pesoTotalTributavel.compareTo(PESO_LIMITE_50) > 0) {
			freteBase = pesoTotalTributavel.multiply(FATOR_FRETE_7);
		} else if (pesoTotalTributavel.compareTo(PESO_LIMITE_10) > 0) {
			freteBase = pesoTotalTributavel.multiply(FATOR_FRETE_4);
		} else if (pesoTotalTributavel.compareTo(PESO_LIMITE_5) > 0) {
			freteBase = pesoTotalTributavel.multiply(FATOR_FRETE_2);
		}

		// Adição de taxa mínima
		if (freteBase.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal temp = freteBase.add(TAXA_MINIMA_FRETE);
            return temp.setScale(2, RoundingMode.HALF_UP);
		}

        return freteBase.setScale(2, RoundingMode.HALF_UP);
	}

	/**
	 * Calcula a taxa total adicional para itens frágeis no carrinho.
	 */
    public BigDecimal calcularTaxaItensFrageis(CarrinhoDeCompras carrinho) {
		return carrinho.getItens().stream()
				.filter(item -> item.getProduto().isFragil())
				.map(item -> TAXA_ITEM_FRAGIL.multiply(new BigDecimal(item.getQuantidade())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/**
	 * Aplica o desconto de frete com base no nível de fidelidade (TipoCliente).
	 */
    public BigDecimal aplicarDescontoFidelidadeFrete(BigDecimal freteComRegiao, TipoCliente tipoCliente) {
		switch (tipoCliente) {
			case OURO:
				return BigDecimal.ZERO;
			case PRATA:
				return freteComRegiao.multiply(DESCONTO_FRETE_PRATA);
			case BRONZE:
			default:
				return freteComRegiao;
		}
	}
}