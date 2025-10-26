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
    @DisplayName("Limite: Subtotal exatamente 499.00 (Sem desconto) - L1")
    void deveAplicarSemDescontoParaSubtotalExatamente499_L1() {
        // ARRANGE
        Produto produto499 = new Produto(11L, "Prod 1kg", "...", new BigDecimal("499.00"), 
                                            BigDecimal.valueOf(1), // peso
                                            BigDecimal.valueOf(10), // c
                                            BigDecimal.valueOf(10), // l
                                            BigDecimal.valueOf(10), // a
                                            false, TipoProduto.LIVRO);
        
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
    @DisplayName("Limite: Subtotal exatamente 500.00 (Sem desconto) - L2")
    void deveAplicarSemDescontoParaSubtotalExatamente500_L2() {
        // ARRANGE
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L, produtoPesado, 1L)); // 15kg
        itens.add(new ItemCompra(2L, produtoMuitoPesado, 1L)); // 60kg
        carrinho.setItens(itens);

        // --- Cálculo Esperado ---
        // 1. Subtotal: 400.00 + 100.00 = 500.00
        // 2. Desc. Tipo (1+1): 0%
        // 3. Desc. Carrinho: (Não é > 500.00) -> 0%
        //    Subtotal Final: 500.00
        // 4. Frete (Peso): 15kg + 60kg = 75kg (Faixa D)
        //    Frete Base: (75.0 * 7.00) + 12.00 = 525.00 + 12.00 = 537.00
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
    @DisplayName("Limite: Subtotal exatamente 500.01 (10% desconto) - L3")
    void deveAplicarSemDescontoParaSubtotalExatamente500_01_L3() {
        // ARRANGE
        Produto produto500_01 = new Produto(12L, "Prod 1kg", "...", new BigDecimal("500.01"), 
                                            BigDecimal.valueOf(1), // peso
                                            BigDecimal.valueOf(10), // c
                                            BigDecimal.valueOf(10), // l
                                            BigDecimal.valueOf(10), // a
                                            false, TipoProduto.LIVRO);
        
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(Collections.singletonList(new ItemCompra(1L, produto500_01, 1L)));

        // ACT
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());

        // ASSERT
        assertThat(custoTotal)
            .as("Limite: Subtotal exatamente 500.01 (10% desconto)")
            .isEqualByComparingTo("450.01");
    }

    @Test
    @DisplayName("Limite: Subtotal exatamente 999.00 (10% desconto) - L4")
    void deveAplicarSemDescontoParaSubtotalExatamente999_L4() {
        // ARRANGE
        // Criar produto local para peso exato
        Produto produto999 = new Produto(13L, "Prod 1kg", "...", new BigDecimal("999.00"), 
                                            BigDecimal.valueOf(1), // peso
                                            BigDecimal.valueOf(10), // c
                                            BigDecimal.valueOf(10), // l
                                            BigDecimal.valueOf(10), // a
                                            false, TipoProduto.LIVRO);
        
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(Collections.singletonList(new ItemCompra(1L, produto999, 1L)));

        // ACT
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());

        // ASSERT
        assertThat(custoTotal)
            .as("Limite: Subtotal exatamente 999.00 (10% desconto)")
            .isEqualByComparingTo("899.10");
    }

    @Test
    @DisplayName("Limite: Subtotal exatamente 1000.00 (10% desconto) - L5")
    void deveAplicar10PorcentoDescontoParaSubtotalExatamente1000_L5() {
        // ARRANGE
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L, produtoLeve, 1L)); // 1kg
        itens.add(new ItemCompra(2L, produtoMuitoPesado, 1L)); // 60kg
        carrinho.setItens(itens);

        // --- Cálculo Esperado ---
        // 1. Subtotal: 900.00 + 100.00 = 1000.00
        // 2. Desc. Tipo (1+1): 0%
        // 3. Desc. Carrinho: (Não é > 1000.00, mas é > 500.00) -> 10%
        //    Subtotal Final: 1000.00 * 0.90 = 900.00
        // 4. Frete (Peso): 1kg + 60kg = 61kg (Faixa D)
        //    Frete Base: (61.0 * 7.00) + 12.00 = 427.00 + 12.00 = 439.00
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
    @DisplayName("Limite: Subtotal exatamente 1000.01 (20% desconto) - L6")
    void deveAplicarSemDescontoParaSubtotalExatamente1000_01_L6() {
        // ARRANGE
        Produto produto1000_01 = new Produto(14L, "Prod 1kg", "...", new BigDecimal("1000.01"), 
                                            BigDecimal.valueOf(1), // peso
                                            BigDecimal.valueOf(10), // c
                                            BigDecimal.valueOf(10), // l
                                            BigDecimal.valueOf(10), // a
                                            false, TipoProduto.LIVRO);
        
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
    @DisplayName("Limite: Peso exatamente 5.0kg (Limite do Frete Isento) - L7")
    void deveCalcularCustoTotalParaCincoItens_L7()
    {
        // ARRANGE
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        List<ItemCompra> itens = new ArrayList<>();
        
        // produtoLeve (1kg) * 5 = 5kg
        ItemCompra item1 = new ItemCompra(1L, produtoLeve, 5L); 
        
        itens.add(item1);
        carrinho.setItens(itens);

        // --- Cálculo Esperado ---
        // 1. Subtotal: 5 * 900.00 = 4500.00
        // 2. Desc. Tipo (5 itens): "5 a 7 itens do mesmo tipo" -> 10% de desconto 
        //    Subtotal (passo 2): 4500.00 * 0.90 = 4050.00
        // 3. Desc. Carrinho (>1000): 20% de desconto 
        //    Subtotal (passo 3): 4050.00 * 0.80 = 3240.00 
        // 4. Frete (Peso 5.0kg): Faixa A (<= 5kg) -> Frete Base 0.00
        // 5. Frete Cliente (Ouro): 100% desconto -> 0.00
        // 6. Total: 3240.00 + 0.00 = 3240.00 

        // ACT
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteOuro.getRegiao(), clienteOuro.getTipo());

        // ASSERT
        assertThat(custoTotal)
            .as("Custo Total da Compra para Cliente Ouro com 5 itens e 5kg")
            .isEqualByComparingTo("3240.00");
    }

    @Test
    @DisplayName("Limite: Peso exatamente 5.01kg (Início Faixa B) - L8")
    void deveAplicarFreteFaixaBParaPesoMinimamenteAcimaDe5kg_L8() {
        // ARRANGE
        Produto produto5_01kg = new Produto(10L, "Prod 5.01kg", "...", new BigDecimal("100.00"), 
                                            BigDecimal.valueOf(5.01), // peso
                                            BigDecimal.valueOf(10), // c
                                            BigDecimal.valueOf(10), // l
                                            BigDecimal.valueOf(10), // a
                                            false, TipoProduto.LIVRO);
        
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
    @DisplayName("Limite: Peso exatamente 9.99kg (Faixa B) - L9")
    void deveAplicarFreteFaixaBParaPeso9_99kg_L9() {
        Produto p = new Produto(101L, "Prod 9.99kg", "...", new BigDecimal("100.00"),
                BigDecimal.valueOf(9.99), // peso
                BigDecimal.valueOf(10), // c
                BigDecimal.valueOf(10), // l
                BigDecimal.valueOf(10), // a
                false, TipoProduto.LIVRO);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(Collections.singletonList(new ItemCompra(1L, p, 1L)));

        BigDecimal total = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());
        // frete: (2 * 9.99) + 12 = 19.98 + 12 = 31.98; total = 100 + 31.98 = 131.98
        assertThat(total).isEqualByComparingTo("131.98");
    }

    @Test
    @DisplayName("Limite: Peso exatamente 10.0kg (Fim Faixa B) - 10")
    void deveAplicarFreteFaixaBParaPesoExatamente10kg_L10() {
        // ARRANGE
        Produto produto10kg = new Produto(11L, "Prod 10kg", "...", new BigDecimal("100.00"), 
                                          BigDecimal.valueOf(10.0), // peso
                                          BigDecimal.valueOf(10), // c
                                          BigDecimal.valueOf(10), // l
                                          BigDecimal.valueOf(10), // a
                                          false, TipoProduto.LIVRO);
        
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
    @DisplayName("Limite: Peso exatamente 10.01kg (início da Faixa C) - L11")
    void deveAplicarFreteFaixaCParaPeso10_01kg_L11() {
        Produto p = new Produto(102L, "Prod 10.01kg", "...", new BigDecimal("100.00"),
                BigDecimal.valueOf(10.01), // peso
                BigDecimal.valueOf(10), // c
                BigDecimal.valueOf(10), // l
                BigDecimal.valueOf(10), // a
                false, TipoProduto.LIVRO);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(Collections.singletonList(new ItemCompra(1L, p, 1L)));

        BigDecimal total = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());
        // frete: (4 * 10.01) + 12 = 40.04 + 12 = 52.04; total = 100 + 52.04 = 152.04
        assertThat(total).isEqualByComparingTo("152.04");
    }

    @Test
    @DisplayName("Limite: Peso exatamente 49.99kg (Faixa C) - L12")
    void deveAplicarFreteFaixaCParaPeso49_99kg_L12() {
        Produto p = new Produto(103L, "Prod 49.99kg", "...", new BigDecimal("100.00"),
                BigDecimal.valueOf(49.99), // peso
                BigDecimal.valueOf(10), // c
                BigDecimal.valueOf(10), // l
                BigDecimal.valueOf(10), // a
                false, TipoProduto.LIVRO);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(Collections.singletonList(new ItemCompra(1L, p, 1L)));

        BigDecimal total = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());
        // frete: (4 * 49.99) + 12 = 199.96 + 12 = 211.96; total = 100 + 211.96 = 311.96
        assertThat(total).isEqualByComparingTo("311.96");
    }

    @Test
    @DisplayName("Limite: Peso exatamente 50.0kg (Fim Faixa C) - L13")
    void deveAplicarFreteFaixaCParaPesoExatamente50kg_L13() {
        // ARRANGE
        Produto produto50kg = new Produto(12L, "Prod 50kg", "...", new BigDecimal("100.00"), 
                                          BigDecimal.valueOf(50.0), // peso
                                          BigDecimal.valueOf(10), // c
                                          BigDecimal.valueOf(10), // l
                                          BigDecimal.valueOf(10), // a
                                          false, TipoProduto.LIVRO);
        
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

    @Test
    @DisplayName("Limite: Peso exatamente 50.01kg (início da Faixa D) - L14")
    void deveAplicarFreteFaixaDParaPeso50_01kg_L14() {
        Produto p = new Produto(104L, "Prod 50.01kg", "...", new BigDecimal("100.00"),
                BigDecimal.valueOf(50.01), // peso
                BigDecimal.valueOf(10), // c
                BigDecimal.valueOf(10), // l
                BigDecimal.valueOf(10), // a
                false, TipoProduto.LIVRO);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(Collections.singletonList(new ItemCompra(1L, p, 1L)));

        BigDecimal total = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());
        // frete: (7 * 50.01) + 12 = 350.07 + 12 = 362.07; total = 100 + 362.07 = 462.07
        assertThat(total).isEqualByComparingTo("462.07");
    }

    // ------------ Teste de múltiplos itens do mesmo tipo---------------

    private Produto produtoGrupoPreco100Peso05Kg() {
        // peso 0,5 kg para manter peso total <= 5kg até 8 itens (não interferir no frete)
        return new Produto(200L, "Item Grupo", "...", new BigDecimal("100.00"),
                BigDecimal.valueOf(0.5), // peso
                BigDecimal.valueOf(10), // c
                BigDecimal.valueOf(10), // l
                BigDecimal.valueOf(10), // a
                false, TipoProduto.ELETRONICO);
    }

    @Test
    @DisplayName("Qtd. mesmo tipo: 2 itens (0%) - L15")
    void deveAplicarDesconto0PorcentoPara2ItensDoMesmoTipo_L15() {
        CarrinhoDeCompras c = new CarrinhoDeCompras();
        c.setItens(Collections.singletonList(new ItemCompra(1L, produtoGrupoPreco100Peso05Kg(), 2L)));
        BigDecimal total = compraService.calcularCustoTotal(c, clienteBronze.getRegiao(), clienteBronze.getTipo());
        assertThat(total).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("Qtd. mesmo tipo: 3 itens (5%) - L16")
    void deveAplicarDesconto5PorcentoPara3ItensDoMesmoTipo_L16() {
        CarrinhoDeCompras c = new CarrinhoDeCompras();
        c.setItens(Collections.singletonList(new ItemCompra(1L, produtoGrupoPreco100Peso05Kg(), 3L)));
        BigDecimal total = compraService.calcularCustoTotal(c, clienteBronze.getRegiao(), clienteBronze.getTipo());
        assertThat(total).isEqualByComparingTo("285.00");
    }

    @Test
    @DisplayName("Qtd. mesmo tipo: 4 itens (5%) - L17")
    void deveAplicarDesconto5PorcentoPara4ItensDoMesmoTipo_L17() {
        CarrinhoDeCompras c = new CarrinhoDeCompras();
        c.setItens(Collections.singletonList(new ItemCompra(1L, produtoGrupoPreco100Peso05Kg(), 4L)));
        BigDecimal total = compraService.calcularCustoTotal(c, clienteBronze.getRegiao(), clienteBronze.getTipo());
        assertThat(total).isEqualByComparingTo("380.00");
    }

    @Test
    @DisplayName("Qtd. mesmo tipo: 5 itens (10%) - L18")
    void deveAplicarDesconto10PorcentoPara5ItensDoMesmoTipo_L18() {
        CarrinhoDeCompras c = new CarrinhoDeCompras();
        c.setItens(Collections.singletonList(new ItemCompra(1L, produtoGrupoPreco100Peso05Kg(), 5L)));
        BigDecimal total = compraService.calcularCustoTotal(c, clienteBronze.getRegiao(), clienteBronze.getTipo());
        assertThat(total).isEqualByComparingTo("450.00");
    }

    @Test
    @DisplayName("Qtd. mesmo tipo: 7 itens (10% + 10% por valor) - L19")
    void deveAplicarDesconto10PorcentoMaisValorPara7ItensDoMesmoTipo_L19() {
        CarrinhoDeCompras c = new CarrinhoDeCompras();
        c.setItens(Collections.singletonList(new ItemCompra(1L, produtoGrupoPreco100Peso05Kg(), 7L)));
        BigDecimal total = compraService.calcularCustoTotal(c, clienteBronze.getRegiao(), clienteBronze.getTipo());
        assertThat(total).isEqualByComparingTo("567.00");
    }

    @Test
    @DisplayName("Qtd. mesmo tipo: 8 itens (15% + 10% por valor) - L20")
    void deveAplicarDesconto15PorcentoMaisValorPara8ItensDoMesmoTipo_20() {
        CarrinhoDeCompras c = new CarrinhoDeCompras();
        c.setItens(Collections.singletonList(new ItemCompra(1L, produtoGrupoPreco100Peso05Kg(), 8L)));
        BigDecimal total = compraService.calcularCustoTotal(c, clienteBronze.getRegiao(), clienteBronze.getTipo());
        assertThat(total).isEqualByComparingTo("612.00");
    }
}