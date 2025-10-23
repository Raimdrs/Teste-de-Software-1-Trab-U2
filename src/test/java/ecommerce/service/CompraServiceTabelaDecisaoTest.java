package ecommerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import ecommerce.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CompraServiceTabelaDecisaoTest extends CompraServiceTestBase {

   /* @Test
    @DisplayName("Tabela Decisão: Subtotal>1000, Peso>5, Frágil=S, Cliente=Prata, Região=NE")
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

    @Test
    @DisplayName("Tabela Decisão: Cliente=Ouro (Regra de Override de Frete)")
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
    }*/

    /*private CarrinhoDeCompras carrinho(ItemCompra... itens) {
        CarrinhoDeCompras c = new CarrinhoDeCompras();
        c.setItens(List.of(itens));
        return c;
    }
    private ItemCompra item(Produto p, long q) { return new ItemCompra(null, p, q); }

    // ===================== 1) validarItens =====================

    @Test @DisplayName("D1: validarItens: quantidade ≤ 0 → lança msg de quantidade")
    void D1_validarItens_quantidadeNaoPositiva() {
        Produto p = new Produto(1L, "X", "...", new BigDecimal("10.00"), 1.0, 1,1,1, false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinho(item(p, 0L));

        assertThatThrownBy(() -> compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A quantidade do item deve ser positiva");
    }

    @Test @DisplayName("D2: validarItens: precoUnitario < 0 → lança msg de preço")
    void D2_validarItens_precoNegativo() {
        Produto p = new Produto(2L, "X", "...", new BigDecimal("-0.01"), 1.0, 1,1,1, false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinho(item(p, 1L));

        assertThatThrownBy(() -> compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("O preço do item não pode ser negativo");
    }

    @Test @DisplayName("D3: validarItens: pesoFisico ≤ 0 → lança msg de peso")
    void D3_validarItens_pesoNaoPositivo() {
        Produto p = new Produto(3L, "X", "...", new BigDecimal("10.00"), 0.0, 1,1,1, false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinho(item(p, 1L));

        assertThatThrownBy(() -> compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("O peso físico do item deve ser positivo");
    }

    @Test @DisplayName("D4: validarItens: alguma dimensão ≤ 0 → lança msg de dimensões")
    void D4_validarItens_dimensaoNaoPositiva() {
        Produto p = new Produto(4L, "X", "...", new BigDecimal("10.00"), 1.0, 0,1,1, false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinho(item(p, 1L));

        assertThatThrownBy(() -> compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("As dimensões (C, L, A) do item devem ser positivas");
    }

    @Test @DisplayName("D5: validarItens: itens válidos → prossegue")
    void D5_validarItens_itensValidosProssegue() {
        Produto p = new Produto(5L, "X", "...", new BigDecimal("10.00"), 1.0, 1,1,1, false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinho(item(p, 1L));

        BigDecimal total = compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("10.00");
    }

// ===================== 2) calcularSubtotalComDescontoPorItens =====================

    @Test @DisplayName("D6: desconto por grupo: qtd ≤ 2 → 0%")
    void D6_descontoGrupo_ate2() {
        Produto g = new Produto(10L, "G", "...", new BigDecimal("100.00"), 0.5, 1,1,1, false, TipoProduto.ELETRONICO);
        CarrinhoDeCompras c = carrinho(item(g, 2L)); // subtotal=200, sem frete
        BigDecimal total = compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("200.00");
    }

    @Test @DisplayName("D7: desconto por grupo: 3–4 → 5%")
    void D7_descontoGrupo_3a4() {
        Produto g = new Produto(11L, "G", "...", new BigDecimal("100.00"), 0.5, 1,1,1, false, TipoProduto.ELETRONICO);
        CarrinhoDeCompras c = carrinho(item(g, 3L)); // 300 * 0.95 = 285
        BigDecimal total = compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("285.00");
    }

    @Test @DisplayName("D8: desconto por grupo: 5–7 → 10% (e depois 10% por valor > 500 quando aplicável)")
    void D8_descontoGrupo_5a7() {
        Produto g = new Produto(12L, "G", "...", new BigDecimal("100.00"), 0.5, 1,1,1, false, TipoProduto.ELETRONICO);
        CarrinhoDeCompras c = carrinho(item(g, 6L)); // 600 * 0.90 = 540 -> (>500) *0.90 = 486
        BigDecimal total = compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("486.00");
    }

    @Test @DisplayName("D9: desconto por grupo: ≥ 8 → 15% (e depois 10% por valor > 500)")
    void D9_descontoGrupo_8ouMais() {
        Produto g = new Produto(13L, "G", "...", new BigDecimal("100.00"), 0.5, 1,1,1, false, TipoProduto.ELETRONICO);
        CarrinhoDeCompras c = carrinho(item(g, 8L)); // 800 * 0.85 = 680 -> *0.90 = 612
        BigDecimal total = compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("612.00");
    }

// ===================== 3) aplicarDescontoPorValor =====================

    @Test @DisplayName("D10: desconto por valor: subtotal > 1000 → 20%")
    void D10_descontoPorValor_maiorQue1000() {
        Produto p = new Produto(20L, "X", "...", new BigDecimal("1000.01"), 1.0, 1,1,1, false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinho(item(p, 1L));
        BigDecimal total = compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("800.01");
    }

    @Test @DisplayName("D11: desconto por valor: (500, 1000] → 10%")
    void D11_descontoPorValor_entre500e1000() {
        Produto p = new Produto(21L, "X", "...", new BigDecimal("900.00"), 1.0, 1,1,1, false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinho(item(p, 1L));
        BigDecimal total = compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("810.00");
    }

    @Test @DisplayName("D12: desconto por valor: ≤ 500 → 0%")
    void D12_descontoPorValor_ate500() {
        Produto p = new Produto(22L, "X", "...", new BigDecimal("500.00"), 1.0, 1,1,1, false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinho(item(p, 1L));
        BigDecimal total = compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("500.00");
    }

// ===================== 4) peso tributável + frete base + taxa mínima =====================

    @Test @DisplayName("D13: peso tributável: usa máximo(físico, cúbico) × quantidade")
    void D13_pesoTributavel_maxFisicoCubico() {
        // físico 1.0, cúbico = (100*100*100)/6000 = 166.66...
        Produto p = new Produto(30L, "Volumoso", "...", new BigDecimal("10.00"),
                1.0, 100,100,100, false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinho(item(p, 1L)); // w ≈ 166.67 → faixa D
        BigDecimal total = compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE);
        // frete: 7*w + 12 ≈ 7*166.67 + 12 = 1,166.69 + 12 ≈ 1,178.69 → total ≈ 1,188.69
        // Arredondamento HALF_UP (2 casas)
        assertThat(total).isEqualByComparingTo("1188.69");
    }

    @Test @DisplayName("D14: frete base: 0 ≤ w ≤ 5 → 0 (sem taxa mínima)")
    void D14_freteBase_faixaA() {
        Produto p = new Produto(31L, "5kg", "...", new BigDecimal("100.00"),
                5.0, 10,10,10, false, TipoProduto.LIVRO);
        BigDecimal total = compraService.calcularCustoTotal(carrinho(item(p,1L)), Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("100.00");
    }

    @Test @DisplayName("D15: frete base: 5 < w ≤ 10 → 2*w + 12")
    void D15_freteBase_faixaB() {
        Produto p = new Produto(32L, "8kg", "...", new BigDecimal("100.00"),
                8.0, 10,10,10, false, TipoProduto.LIVRO);
        BigDecimal total = compraService.calcularCustoTotal(carrinho(item(p,1L)), Regiao.SUDESTE, TipoCliente.BRONZE);
        // 2*8 + 12 = 28; 100 + 28 = 128
        assertThat(total).isEqualByComparingTo("128.00");
    }

    @Test @DisplayName("D16: frete base: 10 < w ≤ 50 → 4*w + 12")
    void D16_freteBase_faixaC() {
        Produto p = new Produto(33L, "15kg", "...", new BigDecimal("100.00"),
                15.0, 10,10,10, false, TipoProduto.LIVRO);
        BigDecimal total = compraService.calcularCustoTotal(carrinho(item(p,1L)), Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("172.00");
    }

    @Test @DisplayName("D17: frete base: w > 50 → 7*w + 12")
    void D17_freteBase_faixaD() {
        Produto p = new Produto(34L, "60kg", "...", new BigDecimal("100.00"),
                60.0, 10,10,10, false, TipoProduto.LIVRO);
        BigDecimal total = compraService.calcularCustoTotal(carrinho(item(p,1L)), Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("532.00");
    }

// ===================== 5) taxa de itens frágeis =====================

    @Test @DisplayName("D18: taxa frágil: soma 5,00 × quantidade para cada item frágil")
    void D18_taxaFragil_somaPorQuantidade() {
        Produto f1 = new Produto(40L, "F1", "...", new BigDecimal("10.00"), 1.0, 1,1,1, true,  TipoProduto.LIVRO);
        Produto f2 = new Produto(41L, "F2", "...", new BigDecimal("10.00"), 1.0, 1,1,1, true,  TipoProduto.LIVRO);
        // total itens = 3*10 + 2*10 = 50; frete isento; taxa frágil = (3+2)*5 = 25 → 75
        CarrinhoDeCompras c = carrinho(item(f1,3L), item(f2,2L));
        BigDecimal total = compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("75.00");
    }

    @Test @DisplayName("D19: taxa frágil: nenhum item frágil → não soma taxa")
    void D19_taxaFragil_semFragil() {
        Produto n = new Produto(42L, "N", "...", new BigDecimal("10.00"), 1.0, 1,1,1, false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinho(item(n,2L));
        BigDecimal total = compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("20.00");
    }

// ===================== 6) multiplicador da região =====================

    @Test @DisplayName("D20: região: multiplica frete por 1,05 (SUL)")
    void D20_regiao_SUL() {
        Produto p = new Produto(50L, "8kg", "...", new BigDecimal("100.00"),
                8.0, 10,10,10, false, TipoProduto.LIVRO);
        BigDecimal total = compraService.calcularCustoTotal(carrinho(item(p,1L)), Regiao.SUL, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("129.40"); // 100 + 28*1.05
    }

    @Test @DisplayName("D21: região: multiplica frete por 1,10 (NORDESTE)")
    void D21_regiao_NORDESTE() {
        Produto p = new Produto(51L, "8kg", "...", new BigDecimal("100.00"),
                8.0, 10,10,10, false, TipoProduto.LIVRO);
        BigDecimal total = compraService.calcularCustoTotal(carrinho(item(p,1L)), Regiao.NORDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("130.80");
    }

    @Test @DisplayName("D22: região: multiplica frete por 1,20 (CENTRO_OESTE)")
    void D22_regiao_CENTRO_OESTE() {
        Produto p = new Produto(52L, "8kg", "...", new BigDecimal("100.00"),
                8.0, 10,10,10, false, TipoProduto.LIVRO);
        BigDecimal total = compraService.calcularCustoTotal(carrinho(item(p,1L)), Regiao.CENTRO_OESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("133.60");
    }

    @Test @DisplayName("D23: região: multiplica frete por 1,30 (NORTE)")
    void D23_regiao_NORTE() {
        Produto p = new Produto(53L, "8kg", "...", new BigDecimal("100.00"),
                8.0, 10,10,10, false, TipoProduto.LIVRO);
        BigDecimal total = compraService.calcularCustoTotal(carrinho(item(p,1L)), Regiao.NORTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("136.40");
    }

// ===================== 7) fidelidade do frete =====================

    @Test @DisplayName("D24: fidelidade: PRATA → 50% do frete após multiplicador")
    void D24_fidelidade_PRATA() {
        Produto p = new Produto(60L, "8kg", "...", new BigDecimal("100.00"),
                8.0, 10,10,10, false, TipoProduto.LIVRO);
        BigDecimal total = compraService.calcularCustoTotal(carrinho(item(p,1L)), Regiao.SUDESTE, TipoCliente.PRATA);
        assertThat(total).isEqualByComparingTo("114.00"); // 100 + 28 * 0.50
    }

    @Test @DisplayName("D25: fidelidade: OURO → frete 0")
    void D25_fidelidade_OURO() {
        Produto p = new Produto(61L, "8kg", "...", new BigDecimal("100.00"),
                8.0, 10,10,10, false, TipoProduto.LIVRO);
        BigDecimal total = compraService.calcularCustoTotal(carrinho(item(p,1L)), Regiao.SUDESTE, TipoCliente.OURO);
        assertThat(total).isEqualByComparingTo("100.00");
    }

    @Test @DisplayName("D26: fidelidade: BRONZE (padrão) → sem desconto no frete")
    void D26_fidelidade_BRONZE() {
        Produto p = new Produto(62L, "8kg", "...", new BigDecimal("100.00"),
                8.0, 10,10,10, false, TipoProduto.LIVRO);
        BigDecimal total = compraService.calcularCustoTotal(carrinho(item(p,1L)), Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("128.00");
    }*/
}