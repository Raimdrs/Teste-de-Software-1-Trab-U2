package ecommerce.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.TipoProduto;
import java.util.Collections;


public class CompraServiceAnaliseLimiteTest extends CompraServiceTestBase {

    //---------- Testes de Valolres limites por preço ----------

    @Test
    @DisplayName("Limite: Subtotal exatamente 499.00 (Sem desconto)")
    void deveAplicarSemDescontoParaSubtotalExatamente499() {
        // ARRANGE
        // Criar produto local para peso exato
        Produto produto499 = new Produto(11L, "Prod 1kg", "...", new BigDecimal("499.00"), 
                                            1, 10, 10, 10, false, TipoProduto.LIVRO);
        
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(Collections.singletonList(new ItemCompra(1L, produto499, 1L)));

        // ACT
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());

        // ASSERT
        assertThat(custoTotal)
            .as("Limite: Subtotal exatamente 499.00 (Sem desconto)")
            .isEqualByComparingTo("499.00");
    }
    
    @Test
    @DisplayName("Limite: Subtotal exatamente 500.00 (Sem desconto)")
    void deveAplicarSemDescontoParaSubtotalExatamente500() {
        // ARRANGE
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        List<ItemCompra> itens = new ArrayList<>();
        // produtoPesado (R$ 400.00) + produtoMuitoPesado (R$ 100.00) = R$ 500.00
        // Tipos diferentes (Esporte, CasaConstrucao) -> Sem desc. por tipo.
        itens.add(new ItemCompra(1L, produtoPesado, 1L));
        itens.add(new ItemCompra(2L, produtoMuitoPesado, 1L));
        carrinho.setItens(itens);

        // --- Cálculo Esperado ---
        // 1. Subtotal: 400.00 + 100.00 = 500.00
        // 2. Desc. Tipo (1+1): 0%
        // 3. Desc. Carrinho: (Não é > 500.00) -> 0%
        //    Subtotal Final: 500.00
        // 4. Frete (Peso): 15kg + 60kg = 75kg (Faixa D)
        //    Frete Base: (75.0 * 7.00) + 12.00 = 537.00
        // 5. Frete (Bronze, Sudeste): 537.00 * 1.0 = 537.00
        // 6. Total: 500.00 + 537.00 = 1037.00

        // ACT
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());

        // ASSERT
        assertThat(custoTotal)
            .as("Custo total para subtotal exatamente R$ 500.00 (sem desconto)")
            .isEqualByComparingTo("1037.00");
    }

    @Test
    @DisplayName("Limite: Subtotal exatamente 500.01 (Sem desconto)")
    void deveAplicarSemDescontoParaSubtotalExatamente500_01() {
        // ARRANGE
        // Criar produto local para peso exato
        Produto produto500_01 = new Produto(12L, "Prod 1kg", "...", new BigDecimal("500.01"), 
                                            1, 10, 10, 10, false, TipoProduto.LIVRO);
        
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(Collections.singletonList(new ItemCompra(1L, produto500_01, 1L)));

        // ACT
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());

        // ASSERT
        assertThat(custoTotal)
            .as("Limite: Subtotal exatamente 500.01 (Sem desconto)")
            .isEqualByComparingTo("450.01");
    }

    @Test
    @DisplayName("Limite: Subtotal exatamente 999.00 (Sem desconto)")
    void deveAplicarSemDescontoParaSubtotalExatamente999() {
        // ARRANGE
        // Criar produto local para peso exato
        Produto produto500_01 = new Produto(13L, "Prod 1kg", "...", new BigDecimal("999.00"), 
                                            1, 10, 10, 10, false, TipoProduto.LIVRO);
        
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(Collections.singletonList(new ItemCompra(1L, produto500_01, 1L)));

        // ACT
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());

        // ASSERT
        assertThat(custoTotal)
            .as("Limite: Subtotal exatamente 999.00 (Sem desconto)")
            .isEqualByComparingTo("899.10");
    }

    @Test
    @DisplayName("Limite: Subtotal exatamente 1000.00 (10% desconto)")
    void deveAplicar10PorcentoDescontoParaSubtotalExatamente1000() {
        // ARRANGE
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        List<ItemCompra> itens = new ArrayList<>();
        // produtoLeve (R$ 900.00) + produtoMuitoPesado (R$ 100.00) = R$ 1000.00
        // Tipos diferentes (Eletronico, CasaConstrucao) -> Sem desc. por tipo.
        itens.add(new ItemCompra(1L, produtoLeve, 1L));
        itens.add(new ItemCompra(2L, produtoMuitoPesado, 1L));
        carrinho.setItens(itens);

        // --- Cálculo Esperado ---
        // 1. Subtotal: 900.00 + 100.00 = 1000.00
        // 2. Desc. Tipo (1+1): 0%
        // 3. Desc. Carrinho: (Não é > 1000.00, mas é > 500.00) -> 10%
        //    Subtotal Final: 1000.00 * 0.90 = 900.00
        // 4. Frete (Peso): 1kg + 60kg = 61kg (Faixa D)
        //    Frete Base: (61.0 * 7.00) + 12.00 = 439.00
        // 5. Frete (Bronze, Sudeste): 439.00 * 1.0 = 439.00
        // 6. Total: 900.00 + 439.00 = 1339.00

        // ACT
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());

        // ASSERT
        assertThat(custoTotal)
            .as("Custo total para subtotal exatamente R$ 1000.00 (10% desconto)")
            .isEqualByComparingTo("1339.00");
    }

    @Test
    @DisplayName("Limite: Subtotal exatamente 1000.01 (Sem desconto)")
    void deveAplicarSemDescontoParaSubtotalExatamente1000_01() {
        // ARRANGE
        // Criar produto local para peso exato
        Produto produto1000_01 = new Produto(14L, "Prod 1kg", "...", new BigDecimal("1000.01"), 
                                            1, 10, 10, 10, false, TipoProduto.LIVRO);
        
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(Collections.singletonList(new ItemCompra(1L, produto1000_01, 1L)));

        // ACT
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());

        // ASSERT
        assertThat(custoTotal)
            .as("Limite: Subtotal exatamente 1000.01 (20% desconto)")
            .isEqualByComparingTo("800.01");
    }

    // ---------- Testes de Valolres limites por Peso ----------

    @Test
    @DisplayName("Limite: Peso exatamente 5.0kg (Limite do Frete Isento)")
    void deveCalcularCustoTotalParaCincoItens()
    {
        // ARRANGE
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        List<ItemCompra> itens = new ArrayList<>();

        ItemCompra item1 = new ItemCompra(1L, produtoLeve, 5L); // 15kg
        
        itens.add(item1);
        carrinho.setItens(itens);

        // --- Cálculo Esperado ---
        // 1. Subtotal: 5 * 900.00 = 4500.00
        // 2. Desc. Tipo (5 itens): "5 a 7 itens do mesmo tipo" -> 10% de desconto 
        //    Subtotal (passo 2): 4500.00 * 0.90 = 4050.00
        // 3. Desc. Carrinho (>1000): 20% de desconto 
        //    (Aplicado após o desconto por tipo)
        //    Subtotal (passo 3): 4050.00 * 0.80 = 3240.00 
        // 4. Frete (Peso 5.0kg): "0,00 <= peso <= 5,00" -> Isento (R$ 0,00) 
        // 5. Frete Cliente (Bronze): "paga o frete integral" (de R$ 0,00) 
        // 6. Total: 3240.00 + 0.00 = 3240.00 

        // ACT
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteOuro.getRegiao(), clienteOuro.getTipo());

        // ASSERT
        assertThat(custoTotal)
            .as("Custo Total da Compra para Cliente Ouro com 3 itens")
            .isEqualByComparingTo("3240.00");
    }

    @Test
    @DisplayName("Limite: Peso exatamente 5.01kg (Início Faixa B)")
    void deveAplicarFreteFaixaBParaPesoMinimamenteAcimaDe5kg() {
        // ARRANGE
        // Criar produto local para peso exato
        Produto produto5_01kg = new Produto(10L, "Prod 5.01kg", "...", new BigDecimal("100.00"), 
                                            5.01, 10, 10, 10, false, TipoProduto.LIVRO);
        
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(Collections.singletonList(new ItemCompra(1L, produto5_01kg, 1L)));

        // --- Cálculo Esperado ---
        // 1. Subtotal: 100.00
        // 2. Desc. Tipo (1 item): 0%
        // 3. Desc. Carrinho (< 500): 0%
        //    Subtotal Final: 100.00
        // 4. Frete (Peso): 5.01kg (Faixa B)
        //    Frete Base: (5.01 * 2.00) + 12.00 = 10.02 + 12.00 = 22.02
        // 5. Frete (Bronze, Sudeste): 22.02 * 1.0 = 22.02
        // 6. Total: 100.00 + 22.02 = 122.02

        // ACT
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());

        // ASSERT
        assertThat(custoTotal)
            .as("Custo total para peso 5.01kg (início da Faixa B)")
            .isEqualByComparingTo("122.02");
    }

    @Test
    @DisplayName("Limite: Peso exatamente 10.0kg (Fim Faixa B)")
    void deveAplicarFreteFaixaBParaPesoExatamente10kg() {
        // ARRANGE
        Produto produto10kg = new Produto(11L, "Prod 10kg", "...", new BigDecimal("100.00"), 
                                          10.0, 10, 10, 10, false, TipoProduto.LIVRO);
        
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(Collections.singletonList(new ItemCompra(1L, produto10kg, 1L)));

        // --- Cálculo Esperado ---
        // 1. Subtotal: 100.00
        // 2. Desc. Tipo (1 item): 0%
        // 3. Desc. Carrinho (< 500): 0%
        //    Subtotal Final: 100.00
        // 4. Frete (Peso): 10.0kg (Faixa B)
        //    Frete Base: (10.0 * 2.00) + 12.00 = 20.00 + 12.00 = 32.00
        // 5. Frete (Bronze, Sudeste): 32.00 * 1.0 = 32.00
        // 6. Total: 100.00 + 32.00 = 132.00

        // ACT
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());

        // ASSERT
        assertThat(custoTotal)
            .as("Custo total para peso 10.0kg (limite final da Faixa B)")
            .isEqualByComparingTo("132.00");
    }

    @Test
    @DisplayName("Limite: Peso exatamente 50.0kg (Fim Faixa C)")
    void deveAplicarFreteFaixaCParaPesoExatamente50kg() {
        // ARRANGE
        Produto produto50kg = new Produto(12L, "Prod 50kg", "...", new BigDecimal("100.00"), 
                                          50.0, 10, 10, 10, false, TipoProduto.LIVRO);
        
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(Collections.singletonList(new ItemCompra(1L, produto50kg, 1L)));

        // --- Cálculo Esperado ---
        // 1. Subtotal: 100.00
        // 2. Desc. Tipo (1 item): 0%
        // 3. Desc. Carrinho (< 500): 0%
        //    Subtotal Final: 100.00
        // 4. Frete (Peso): 50.0kg (Faixa C)
        //    Frete Base: (50.0 * 4.00) + 12.00 = 200.00 + 12.00 = 212.00
        // 5. Frete (Bronze, Sudeste): 212.00 * 1.0 = 212.00
        // 6. Total: 100.00 + 212.00 = 312.00

        // ACT
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());

        // ASSERT
        assertThat(custoTotal)
            .as("Custo total para peso 50.0kg (limite final da Faixa C)")
            .isEqualByComparingTo("312.00");
    }
}
