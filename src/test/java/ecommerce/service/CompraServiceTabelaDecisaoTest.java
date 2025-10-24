package ecommerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.within;

import java.math.BigDecimal;
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

    // Helpers para montar carrinhos/itens rapidamente
    private CarrinhoDeCompras carrinho(ItemCompra... itens) {
        CarrinhoDeCompras c = new CarrinhoDeCompras();
        c.setItens(List.of(itens));
        return c;
    }
    private ItemCompra item(Produto p, Long q) { return new ItemCompra(null, p, q); }

    // -------------------------------------------------------
    // D1 — Existe item com quantidade == null ou quantidade ≤ 0
    // Ação: IllegalArgumentException("A quantidade do item deve ser positiva.")
    // -------------------------------------------------------
    @Test @DisplayName("D1 - quantidade null ou ≤0 → lança IllegalArgumentException (quantidade)")
    void D1_quantidadeInvalida_lanca_excecao() {
        Produto ok = new Produto(1L, "OK", "...", new BigDecimal("10.00"),
                1.0, 10,10,10, false, TipoProduto.LIVRO);

        // quantidade = 0
        CarrinhoDeCompras cZero = carrinho(item(ok, 0L));
        assertThatThrownBy(() ->
                compraService.calcularCustoTotal(cZero, clienteBronze.getRegiao(), clienteBronze.getTipo())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A quantidade do item deve ser positiva.");

        // quantidade = -1
        CarrinhoDeCompras cNeg = carrinho(item(ok, -1L));
        assertThatThrownBy(() ->
                compraService.calcularCustoTotal(cNeg, clienteBronze.getRegiao(), clienteBronze.getTipo())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A quantidade do item deve ser positiva.");
    }

    // -------------------------------------------------------
    // D2 — Existe item com precoUnitario < 0
    // Ação: IllegalArgumentException("O preço do item não pode ser negativo.")
    // -------------------------------------------------------
    @Test @DisplayName("D2 - preço unitário < 0 → lança IllegalArgumentException (preço negativo)")
    void D2_precoNegativo_lanca_excecao() {
        Produto precoNeg = new Produto(2L, "NEG", "...", new BigDecimal("-0.01"),
                1.0, 10,10,10, false, TipoProduto.LIVRO);

        CarrinhoDeCompras c = carrinho(item(precoNeg, 1L));

        assertThatThrownBy(() ->
                compraService.calcularCustoTotal(c, clienteBronze.getRegiao(), clienteBronze.getTipo())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O preço do item não pode ser negativo.");
    }

    // -------------------------------------------------------
    // D3 — Existe item com pesoFisico ≤ 0
    // Ação: IllegalArgumentException("O peso físico do item deve ser positivo.")
    // -------------------------------------------------------
    @Test @DisplayName("D3 - peso físico ≤ 0 → lança IllegalArgumentException (peso físico)")
    void D3_pesoFisicoInvalido_lanca_excecao() {
        Produto pesoZero = new Produto(3L, "P0", "...", new BigDecimal("10.00"),
                0.0, 10,10,10, false, TipoProduto.LIVRO);

        CarrinhoDeCompras c = carrinho(item(pesoZero, 1L));

        assertThatThrownBy(() ->
                compraService.calcularCustoTotal(c, clienteBronze.getRegiao(), clienteBronze.getTipo())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O peso físico do item deve ser positivo.");
    }

    // -------------------------------------------------------
    // D4 — Existe item com alguma dimensão ≤ 0 (C, L ou A)
    // Ação: IllegalArgumentException("As dimensões (C, L, A) do item devem ser positivas.")
    // -------------------------------------------------------
    @Test @DisplayName("D4 - alguma dimensão ≤ 0 → lança IllegalArgumentException (dimensões)")
    void D4_dimensoesInvalidas_lanca_excecao() {
        // comprimento = 0
        Produto dimZero = new Produto(4L, "DIM", "...", new BigDecimal("10.00"),
                1.0, 0, 10, 10, false, TipoProduto.LIVRO);

        CarrinhoDeCompras c = carrinho(item(dimZero, 1L));

        assertThatThrownBy(() ->
                compraService.calcularCustoTotal(c, clienteBronze.getRegiao(), clienteBronze.getTipo())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("As dimensões (C, L, A) do item devem ser positivas.");
    }

    // -------------------------------------------------------
    // D5 — Nenhuma das condições acima ocorre → prosseguir (itens válidos)
    // Verificamos que não lança exceção e calcula normalmente
    // -------------------------------------------------------
    @Test @DisplayName("D5 - itens válidos → prossegue e calcula")
    void D5_itensValidos_prossegue() {
        Produto ok = new Produto(5L, "OK", "...", new BigDecimal("10.00"),
                1.0, 10,10,10, false, TipoProduto.LIVRO);

        CarrinhoDeCompras c = carrinho(item(ok, 1L));

        // Subtotal=10.00; sem desconto por valor; frete isento (≤5kg); total=10.00
        var total = compraService.calcularCustoTotal(c, clienteBronze.getRegiao(), clienteBronze.getTipo());
        assertThat(total).isEqualByComparingTo("10.00");
    }

    // -------------------------------------------------------
    // calcularSubtotalComDescontoPorItens() + descontoPorQuantidade()
    // (tabela D6..D9)
    // -------------------------------------------------------

    @Test @DisplayName("D6 - qtdGrupo ≤ 2 → fator 1,00 (0%)")
    void D6_calcularSubtotalComDescontoPorItens_semDesconto_ate2() {
        // um grupo Tipo A com 2 itens de 10.00
        Produto a = new Produto(1L, "A", "...", new BigDecimal("10.00"),
                1.0, 10,10,10, false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinho(item(a, 2L));

        BigDecimal r = compraService.calcularSubtotalComDescontoPorItens(c);
        // 2 * 10.00 * 1.00 = 20.00
        assertThat(r).isEqualByComparingTo("20.00");

        // valida também descontoPorQuantidade isolado
        assertThat(compraService.descontoPorQuantidade(0L)).isEqualByComparingTo("0.00");
        assertThat(compraService.descontoPorQuantidade(2L)).isEqualByComparingTo("0.00");
    }

    @Test @DisplayName("D7 - 3 ≤ qtdGrupo ≤ 4 → fator 0,95 (5%)")
    void D7_calcularSubtotalComDescontoPorItens_cincoPorCento_3a4() {
        Produto a = new Produto(2L, "A", "...", new BigDecimal("10.00"),
                1.0, 10,10,10, false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinho(item(a, 3L)); // 3 * 10 = 30; 5% => 28.50

        BigDecimal r = compraService.calcularSubtotalComDescontoPorItens(c);
        assertThat(r).isEqualByComparingTo("28.50");

        assertThat(compraService.descontoPorQuantidade(3L)).isEqualByComparingTo("0.05");
        assertThat(compraService.descontoPorQuantidade(4L)).isEqualByComparingTo("0.05");
    }

    @Test @DisplayName("D8 - 5 ≤ qtdGrupo ≤ 7 → fator 0,90 (10%)")
    void D8_calcularSubtotalComDescontoPorItens_dezPorCento_5a7() {
        Produto a = new Produto(3L, "A", "...", new BigDecimal("10.00"),
                1.0, 10,10,10, false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinho(item(a, 5L)); // 5*10 = 50; 10% => 45.00

        BigDecimal r = compraService.calcularSubtotalComDescontoPorItens(c);
        assertThat(r).isEqualByComparingTo("45.00");

        assertThat(compraService.descontoPorQuantidade(5L)).isEqualByComparingTo("0.10");
        assertThat(compraService.descontoPorQuantidade(7L)).isEqualByComparingTo("0.10");
    }

    @Test @DisplayName("D9 - qtdGrupo ≥ 8 → fator 0,85 (15%)")
    void D9_calcularSubtotalComDescontoPorItens_quinzePorCento_8ouMais() {
        Produto a = new Produto(4L, "A", "...", new BigDecimal("10.00"),
                1.0, 10,10,10, false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinho(item(a, 8L)); // 8*10 = 80; 15% => 68.00

        BigDecimal r = compraService.calcularSubtotalComDescontoPorItens(c);
        assertThat(r).isEqualByComparingTo("68.00");

        assertThat(compraService.descontoPorQuantidade(8L)).isEqualByComparingTo("0.15");
        assertThat(compraService.descontoPorQuantidade(20L)).isEqualByComparingTo("0.15");
    }

    @Test @DisplayName("D6..D9 - múltiplos grupos: somar subtotais por grupo com seus fatores")
    void D6_D9_variosGrupos_somaPorGrupo() {
        // Grupo A: 3 itens x 10.00 → 30 * 0.95 = 28.50
        Produto a = new Produto(5L, "A", "...", new BigDecimal("10.00"),
                1.0, 10,10,10, false, TipoProduto.LIVRO);
        // Grupo B: 5 itens x 20.00 → 100 * 0.90 = 90.00
        Produto b = new Produto(6L, "B", "...", new BigDecimal("20.00"),
                1.0, 10,10,10, false, TipoProduto.ELETRONICO);

        CarrinhoDeCompras c = carrinho(item(a, 3L), item(b, 5L));
        BigDecimal r = compraService.calcularSubtotalComDescontoPorItens(c);
        assertThat(r).isEqualByComparingTo("118.50"); // 28.50 + 90.00
    }

    // -------------------------------------------------------
    // aplicarDescontoPorValor() (tabela D10..D12)
    // -------------------------------------------------------

    @Test @DisplayName("D10 - subtotal > 1000.00 → 20%")
    void D10_aplicarDescontoPorValor_maiorQue1000() {
        BigDecimal r = compraService.aplicarDescontoPorValor(new BigDecimal("1000.01"));
        assertThat(r).isEqualByComparingTo("800.01");
    }

    @Test @DisplayName("D11 - (500.00, 1000.00] → 10%")
    void D11_aplicarDescontoPorValor_entre500e1000() {
        BigDecimal r1 = compraService.aplicarDescontoPorValor(new BigDecimal("900.00"));
        BigDecimal r2 = compraService.aplicarDescontoPorValor(new BigDecimal("1000.00"));
        assertThat(r1).isEqualByComparingTo("810.00");
        assertThat(r2).isEqualByComparingTo("900.00");
    }

    @Test @DisplayName("D12 - ≤ 500.00 → 0%")
    void D12_aplicarDescontoPorValor_ate500() {
        BigDecimal r1 = compraService.aplicarDescontoPorValor(new BigDecimal("500.00"));
        BigDecimal r2 = compraService.aplicarDescontoPorValor(new BigDecimal("0.00"));
        assertThat(r1).isEqualByComparingTo("500.00");
        assertThat(r2).isEqualByComparingTo("0.00");
    }

    // -------------------------------------------------------
    // calcularFreteBasePorPeso() (tabela D13..D16)
    // -------------------------------------------------------

    @Test @DisplayName("D13 - 0 ≤ w ≤ 5 → freteBase=0 (sem taxa mínima)")
    void D13_calcularFreteBasePorPeso_faixaA() {
        assertThat(compraService.calcularFreteBasePorPeso(0.0)).isEqualByComparingTo("0.00");
        assertThat(compraService.calcularFreteBasePorPeso(5.0)).isEqualByComparingTo("0.00");
    }

    @Test @DisplayName("D14 - 5 < w ≤ 10 → 2*w + 12")
    void D14_calcularFreteBasePorPeso_faixaB() {
        assertThat(compraService.calcularFreteBasePorPeso(5.01)).isEqualByComparingTo("22.02"); // 2*5.01+12
        assertThat(compraService.calcularFreteBasePorPeso(10.0)).isEqualByComparingTo("32.00"); // 2*10+12
    }

    @Test @DisplayName("D15 - 10 < w ≤ 50 → 4*w + 12")
    void D15_calcularFreteBasePorPeso_faixaC() {
        assertThat(compraService.calcularFreteBasePorPeso(15.0)).isEqualByComparingTo("72.00");  // 4*15+12
        assertThat(compraService.calcularFreteBasePorPeso(50.0)).isEqualByComparingTo("212.00"); // 4*50+12
    }

    @Test @DisplayName("D16 - w > 50 → 7*w + 12")
    void D16_calcularFreteBasePorPeso_faixaD() {
        assertThat(compraService.calcularFreteBasePorPeso(60.0)).isEqualByComparingTo("432.00");   // 7*60+12
        assertThat(compraService.calcularFreteBasePorPeso(50.01)).isEqualByComparingTo("362.07");  // 7*50.01+12
    }

    // -------------------------------------------------------
    // calcularPesoTotalTributavel() (max(físico,cúbico) * qtd, somar)
    // (relacionado à D14 da sua tabela de peso tributável)
    // -------------------------------------------------------

    @Test @DisplayName("D14* - peso tributável soma max(físico,cúbico)×quantidade por item")
    void D14_star_calcularPesoTotalTributavel() {
        // Item 1: físico 1.0, cúbico = (60*40*15)/6000 = 6.0 → usa 6.0 * 2 = 12.0
        Produto volumoso = new Produto(100L, "Vol", "...", new BigDecimal("10.00"),
                1.0, 60, 40, 15, false, TipoProduto.LIVRO);
        // Item 2: físico 8.0, cúbico pequeno → usa 8.0 * 1 = 8.0
        Produto pesado = new Produto(101L, "Pes", "...", new BigDecimal("10.00"),
                8.0, 10, 10, 10, false, TipoProduto.LIVRO);

        CarrinhoDeCompras c = carrinho(item(volumoso, 2L), item(pesado, 1L));
        double peso = compraService.calcularPesoTotalTributavel(c);

        assertThat(peso).isEqualTo(20.0, within(1e-9));
    }

    // -------------------------------------------------------
    // calcularTaxaItensFrageis() (tabela D17..D18)
    // -------------------------------------------------------

    @Test @DisplayName("D17 - existe frágil → soma 5,00 × quantidade por item frágil")
    void D17_calcularTaxaItensFrageis_soma() {
        Produto f1 = new Produto(200L, "F1", "...", new BigDecimal("1.00"),
                1.0, 1,1,1, true, TipoProduto.LIVRO);  // 3 unid → 15
        Produto f2 = new Produto(201L, "F2", "...", new BigDecimal("1.00"),
                1.0, 1,1,1, true, TipoProduto.LIVRO);  // 2 unid → 10

        CarrinhoDeCompras c = carrinho(item(f1, 3L), item(f2, 2L));
        BigDecimal taxa = compraService.calcularTaxaItensFrageis(c);

        assertThat(taxa).isEqualByComparingTo("25.00");
    }

    @Test @DisplayName("D18 - nenhum frágil → 0")
    void D18_calcularTaxaItensFrageis_zero() {
        Produto n = new Produto(202L, "N", "...", new BigDecimal("1.00"),
                1.0, 1,1,1, false, TipoProduto.LIVRO);

        CarrinhoDeCompras c = carrinho(item(n, 5L));
        BigDecimal taxa = compraService.calcularTaxaItensFrageis(c);

        assertThat(taxa).isEqualByComparingTo("0.00");
    }

    // -------------------------------------------------------
    // aplicarDescontoFidelidadeFrete() (tabela D24..D26)
    // -------------------------------------------------------

    @Test @DisplayName("D24 - OURO → freteFinal = 0")
    void D24_aplicarDescontoFidelidadeFrete_ouro() {
        BigDecimal r = compraService.aplicarDescontoFidelidadeFrete(new BigDecimal("123.45"), TipoCliente.OURO);
        assertThat(r).isEqualByComparingTo("0.00");
    }

    @Test @DisplayName("D25 - PRATA → freteFinal = frete × 0,50")
    void D25_aplicarDescontoFidelidadeFrete_prata() {
        BigDecimal r = compraService.aplicarDescontoFidelidadeFrete(new BigDecimal("100.00"), TipoCliente.PRATA);
        assertThat(r).isEqualByComparingTo("50.00");
    }

    @Test @DisplayName("D26 - BRONZE → sem desconto (frete)")
    void D26_aplicarDescontoFidelidadeFrete_bronze() {
        BigDecimal r = compraService.aplicarDescontoFidelidadeFrete(new BigDecimal("100.00"), TipoCliente.BRONZE);
        assertThat(r).isEqualByComparingTo("100.00");
    }
}