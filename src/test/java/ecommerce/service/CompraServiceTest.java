package ecommerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra; // Usando a classe 'Item' correta
import ecommerce.entity.Produto;
import ecommerce.entity.Regiao;
import ecommerce.entity.TipoCliente;

public class CompraServiceTest
{
    private CompraService compraService;
    private Cliente clienteBronze;
    private Cliente clientePrata;
    private Cliente clienteOuro;
    private Produto produtoLeve; // < 5kg
    private Produto produtoMedio; // 8kg
    private Produto produtoPesado; // 15kg
    private Produto produtoMuitoPesado; // 60kg
    private Produto produtoComPesoErrado;

    @BeforeEach
    void setUp() {
        this.compraService = new CompraService(null, null, null, null);

        // --- Clientes ---
        this.clienteBronze = new Cliente();
        clienteBronze.setRegiao(Regiao.SUDESTE); // Multiplicador 1.0
        clienteBronze.setTipo(TipoCliente.BRONZE); // Sem desconto no frete

        this.clientePrata = new Cliente();
        clientePrata.setRegiao(Regiao.NORDESTE); // Multiplicador 1.10
        clientePrata.setTipo(TipoCliente.PRATA); // 50% desconto frete

        this.clienteOuro = new Cliente();
        clienteOuro.setRegiao(Regiao.SUDESTE);
        clienteOuro.setTipo(TipoCliente.OURO); // 100% desconto frete

        // --- Produtos ---
        // Subtotal > 500, Peso Leve (1kg -> Frete Isento)
        this.produtoLeve = new Produto(1L, "Celular", "...", new BigDecimal("900.00"), 1.0, 15, 8, 2, false, null);
        
        // Subtotal > 1000, Peso Médio (8kg -> Frete Faixa B), Frágil
        this.produtoMedio = new Produto(2L, "Monitor 4k", "...", new BigDecimal("1200.00"), 8.0, 60, 40, 15, true, null);
        
        // Subtotal < 500, Peso Pesado (15kg -> Frete Faixa C)
        this.produtoPesado = new Produto(3L, "Halter 15kg", "...", new BigDecimal("400.00"), 15.0, 30, 15, 15, false, null);

        // Subtotal < 500, Peso Muito Pesado (60kg -> Frete Faixa D)
        this.produtoMuitoPesado = new Produto(4L, "Saco de Cimento", "...", new BigDecimal("100.00"), 60.0, 80, 50, 10, false, null);
    
        // Produto com peso negativo
        this.produtoComPesoErrado = new Produto(4L, "Saco de Cimento", "...", new BigDecimal("100.00"), -60.0, 80, 50, 10, false, null);
    }
    
    // --- TESTES DE CENÁRIOS PRINCIPAIS ---

    @Test
    @DisplayName("Cobre: Subtotal > 500 (10% desc), Peso <= 5 (Frete Isento), Cliente Bronze")
    void deveCalcularCustoTotalParaSubtotalFaixa2FreteIsento()
    {
        // ARRANGE
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        ItemCompra itemUnico = new ItemCompra(1L, produtoLeve, 1L); // R$ 900.00, 1kg
        carrinho.setItens(Collections.singletonList(itemUnico));
        
        // --- Cálculo Esperado ---
        // Subtotal: 900.00 -> Com desc 10%: 810.00
        // Peso Total: 1.0kg -> Frete Isento (R$ 0.00)
        // Total: 810.00

        // ACT
        BigDecimal custoTotalCalculado = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());

        // ASSERT
        assertThat(custoTotalCalculado).isEqualByComparingTo("810.00");
    }
    
    @Test
    @DisplayName("Cobre: Subtotal > 1000 (20% desc), Peso > 5 (Frete B), Frágil, Cliente Prata")
    void deveCalcularCustoTotalComFreteItemFragilEDescontoPrata() {
        // ARRANGE
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        ItemCompra itemMonitor = new ItemCompra(1L, produtoMedio, 1L); // R$ 1200.00, 8kg, Frágil
        carrinho.setItens(Collections.singletonList(itemMonitor));
        
        // --- Cálculo Esperado ---
        // Subtotal: 1200.00 -> Com desc 20%: 960.00
        // Peso: 8.0kg -> Frete: 8.0 * 2.00/kg = 16.00
        // Frete + Taxa Mínima (+12.00) + Taxa Frágil (+5.00) = 33.00
        // Frete com Região (1.10): 33.00 * 1.10 = 36.30
        // Frete com Desconto Prata (50%): 36.30 * 0.5 = 18.15
        // Total Final: 960.00 + 18.15 = 978.15
        
        // ACT
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clientePrata.getRegiao(), clientePrata.getTipo());

        // ASSERT
        assertThat(custoTotal)
            .as("Custo total para item pesado, frágil, com desconto e cliente prata")
            .isEqualByComparingTo("978.15");
    }

    // --- TESTES PARA COBRIR OS RAMOS FALTANTES ---

    @Test
    @DisplayName("Cobre: Subtotal <= 500 (Sem desc), Peso > 10 (Frete C), Cliente Bronze")
    void deveCalcularFreteParaFaixaDePesoEntre10e50kg() {
        // ARRANGE
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        ItemCompra item = new ItemCompra(1L, produtoPesado, 1L); // R$ 400.00, 15kg
        carrinho.setItens(Collections.singletonList(item));

        // --- Cálculo Esperado ---
        // Subtotal: 400.00 (Sem desconto)
        // Peso: 15.0kg -> Frete: 15.0 * 4.00/kg = 60.00
        // Frete + Taxa Mínima (+12.00) = 72.00
        // Frete com Região (1.00): 72.00 * 1.0 = 72.00
        // Frete com Desconto Bronze (0%): 72.00
        // Total Final: 400.00 + 72.00 = 472.00
        
        // ACT
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());

        // ASSERT
        assertThat(custoTotal).isEqualByComparingTo("472.00");
    }

    @Test
    @DisplayName("Cobre: Subtotal <= 500 (Sem desc), Peso > 50 (Frete D), Cliente Bronze")
    void deveCalcularFreteParaFaixaDePesoAcimaDe50kg() {
        // ARRANGE
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        ItemCompra item = new ItemCompra(1L, produtoMuitoPesado, 1L); // R$ 100.00, 60kg
        carrinho.setItens(Collections.singletonList(item));

        // --- Cálculo Esperado ---
        // Subtotal: 100.00 (Sem desconto)
        // Peso: 60.0kg -> Frete: 60.0 * 7.00/kg = 420.00
        // Frete + Taxa Mínima (+12.00) = 432.00
        // Frete com Região (1.00): 432.00 * 1.0 = 432.00
        // Frete com Desconto Bronze (0%): 432.00
        // Total Final: 100.00 + 432.00 = 532.00
        
        // ACT
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());

        // ASSERT
        assertThat(custoTotal).isEqualByComparingTo("532.00");
    }
    
    @Test
    @DisplayName("Cobre: Cliente Ouro (Frete 100% desc)")
    void deveAplicarFreteZeroParaClienteOuro() {
        // ARRANGE
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        ItemCompra item = new ItemCompra(1L, produtoPesado, 1L); // R$ 400.00, 15kg
        carrinho.setItens(Collections.singletonList(item));
        
        // --- Cálculo Esperado ---
        // Subtotal: 400.00 (Sem desconto)
        // Peso: 15.0kg -> Frete base seria 72.00
        // Frete com Desconto Ouro (100%): 0.00
        // Total Final: 400.00 + 0.00 = 400.00
        
        // ACT
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteOuro.getRegiao(), clienteOuro.getTipo());

        // ASSERT
        assertThat(custoTotal).isEqualByComparingTo("400.00");
    }

    // --- TESTES DE ROBUSTEZ (COBRINDO RAMOS DE EXCEÇÃO) ---

    @Test
    @DisplayName("Cobre: Validação de Carrinho Vazio")
    void deveLancarExcecaoParaCarrinhoVazio() {
        // ARRANGE
        CarrinhoDeCompras carrinhoVazio = new CarrinhoDeCompras();
        carrinhoVazio.setItens(Collections.emptyList());

        // ACT & ASSERT
        assertThatThrownBy(() -> {
            compraService.calcularCustoTotal(carrinhoVazio, clienteBronze.getRegiao(), clienteBronze.getTipo());
        })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Carrinho não pode estar vazio.");
    }

    @Test
    @DisplayName("Cobre: Validação de Quantidade <= 0")
    void deveLancarExcecaoParaQuantidadeZero() {
        // ARRANGE
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        ItemCompra itemComErro = new ItemCompra(1L, produtoLeve, 0L); // Quantidade ZERO
        carrinho.setItens(Collections.singletonList(itemComErro));

        // ACT & ASSERT
        assertThatThrownBy(() -> {
            compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());
        })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("A quantidade do item deve ser positiva.");
    }

    @Test
    @DisplayName("Cobre: Validação de Preço < 0")
    void deveLancarExcecaoParaPrecoNegativo() {
        // ARRANGE
        Produto produtoPrecoNegativo = new Produto(5L, "Inválido", "...", new BigDecimal("-10.00"), 1, 1, 1, 1, false, null);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        ItemCompra itemComErro = new ItemCompra(1L, produtoPrecoNegativo, 1L);
        carrinho.setItens(Collections.singletonList(itemComErro));

        // ACT & ASSERT
        assertThatThrownBy(() -> {
            compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());
        })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("O preço do item não pode ser negativo.");
    }
    
    @Test
    @DisplayName("Cobre: Validação de Cliente Nulo")
    void deveLancarExcecaoParaClienteNulo() {
        // ARRANGE
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        ItemCompra itemUnico = new ItemCompra(1L, produtoLeve, 1L);
        carrinho.setItens(Collections.singletonList(itemUnico));

        // ACT & ASSERT
        assertThatThrownBy(() -> {
            compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), null);
        })
        .isInstanceOf(NullPointerException.class) // O Objects.requireNonNull lança NPE
        .hasMessage("Tipo de cliente não pode ser nulo.");
    }
	@Test
    @DisplayName("Cobre: Subtotal > 500 (10% desc), Peso <= 5 (Frete Isento), Cliente Bronze")
    void deveCalcularCustoTotalParaVariosItens()
    {
        // ARRANGE
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        ItemCompra itemUnico = new ItemCompra(1L, produtoLeve, 8L); // R$ 900.00, 1kg
        carrinho.setItens(Collections.singletonList(itemUnico));
        
        // --- Cálculo Esperado ---
        // Subtotal: 900.00 -> Com desc 10%: 810.00
        // Peso Total: 1.0kg -> Frete Isento (R$ 0.00)
        // Total: 810.00

        // ACT
        BigDecimal custoTotalCalculado = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());

        // ASSERT
        assertThat(custoTotalCalculado).isEqualByComparingTo("4924.00");
    }
	@Test
    @DisplayName("Cobre: Subtotal > 500 (10% desc), Peso <= 5 (Frete Isento), Cliente Bronze")
    void deveCalcularCustoTotalParaCincoItens()
    {
        // ARRANGE
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        ItemCompra itemUnico = new ItemCompra(1L, produtoLeve, 5L); // R$ 900.00, 1kg
        carrinho.setItens(Collections.singletonList(itemUnico));
        
        // --- Cálculo Esperado ---
        // Subtotal: 900.00 -> Com desc 10%: 810.00
        // Peso Total: 1.0kg -> Frete Isento (R$ 0.00)
        // Total: 810.00

        // ACT
        BigDecimal custoTotalCalculado = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());

        // ASSERT
        assertThat(custoTotalCalculado).isEqualByComparingTo("3240.00");
    }
	@Test
    @DisplayName("Cobre: Subtotal > 500 (10% desc), Peso <= 5 (Frete Isento), Cliente Bronze")
    void deveCalcularCustoTotalParaTresItens()
    {
        // ARRANGE
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        ItemCompra itemUnico = new ItemCompra(1L, produtoLeve, 3L); // R$ 900.00, 1kg
        carrinho.setItens(Collections.singletonList(itemUnico));
        
        // --- Cálculo Esperado ---
        // Subtotal: 900.00 -> Com desc 10%: 810.00
        // Peso Total: 1.0kg -> Frete Isento (R$ 0.00)
        // Total: 810.00

        // ACT
        BigDecimal custoTotalCalculado = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());

        // ASSERT
        assertThat(custoTotalCalculado).isEqualByComparingTo("2052.00");
    }
}