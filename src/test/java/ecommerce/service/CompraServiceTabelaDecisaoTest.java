package ecommerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import ecommerce.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CompraServiceTabelaDecisaoTest extends CompraServiceTestBase {

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
                BigDecimal.valueOf(1.0), // peso
                BigDecimal.valueOf(10),  // c
                BigDecimal.valueOf(10),  // l
                BigDecimal.valueOf(10),  // a
                false, TipoProduto.LIVRO);

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
                BigDecimal.valueOf(1.0), // peso
                BigDecimal.valueOf(10),  // c
                BigDecimal.valueOf(10),  // l
                BigDecimal.valueOf(10),  // a
                false, TipoProduto.LIVRO);

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
                BigDecimal.valueOf(0.0), // peso
                BigDecimal.valueOf(10),  // c
                BigDecimal.valueOf(10),  // l
                BigDecimal.valueOf(10),  // a
                false, TipoProduto.LIVRO);

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
        Produto dimZero = new Produto(4L, "DIM", "...", new BigDecimal("10.00"),
                BigDecimal.valueOf(1.0), // peso
                BigDecimal.valueOf(0),   // c
                BigDecimal.valueOf(10),  // l
                BigDecimal.valueOf(10),  // a
                false, TipoProduto.LIVRO);

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
                BigDecimal.valueOf(1.0), // peso
                BigDecimal.valueOf(10),  // c
                BigDecimal.valueOf(10),  // l
                BigDecimal.valueOf(10),  // a
                false, TipoProduto.LIVRO);

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
        Produto a = new Produto(1L, "A", "...", new BigDecimal("10.00"),
                BigDecimal.valueOf(1.0), BigDecimal.valueOf(10), BigDecimal.valueOf(10), BigDecimal.valueOf(10),
                false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinho(item(a, 2L));

        BigDecimal r = compraService.calcularSubtotalComDescontoPorItens(c);
        assertThat(r).isEqualByComparingTo("20.00");

        assertThat(compraService.descontoPorQuantidade(0L)).isEqualByComparingTo("0.00");
        assertThat(compraService.descontoPorQuantidade(2L)).isEqualByComparingTo("0.00");
    }

    @Test @DisplayName("D7 - 3 ≤ qtdGrupo ≤ 4 → fator 0,95 (5%)")
    void D7_calcularSubtotalComDescontoPorItens_cincoPorCento_3a4() {
        Produto a = new Produto(2L, "A", "...", new BigDecimal("10.00"),
                BigDecimal.valueOf(1.0), BigDecimal.valueOf(10), BigDecimal.valueOf(10), BigDecimal.valueOf(10),
                false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinho(item(a, 3L)); // 3 * 10 = 30; 5% => 28.50

        BigDecimal r = compraService.calcularSubtotalComDescontoPorItens(c);
        assertThat(r).isEqualByComparingTo("28.50");

        assertThat(compraService.descontoPorQuantidade(3L)).isEqualByComparingTo("0.05");
        assertThat(compraService.descontoPorQuantidade(4L)).isEqualByComparingTo("0.05");
    }

    @Test @DisplayName("D8 - 5 ≤ qtdGrupo ≤ 7 → fator 0,90 (10%)")
    void D8_calcularSubtotalComDescontoPorItens_dezPorCento_5a7() {
        Produto a = new Produto(3L, "A", "...", new BigDecimal("10.00"),
                BigDecimal.valueOf(1.0), BigDecimal.valueOf(10), BigDecimal.valueOf(10), BigDecimal.valueOf(10),
                false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinho(item(a, 5L)); // 5*10 = 50; 10% => 45.00

        BigDecimal r = compraService.calcularSubtotalComDescontoPorItens(c);
        assertThat(r).isEqualByComparingTo("45.00");

        assertThat(compraService.descontoPorQuantidade(5L)).isEqualByComparingTo("0.10");
        assertThat(compraService.descontoPorQuantidade(7L)).isEqualByComparingTo("0.10");
    }

    @Test @DisplayName("D9 - qtdGrupo ≥ 8 → fator 0,85 (15%)")
    void D9_calcularSubtotalComDescontoPorItens_quinzePorCento_8ouMais() {
        Produto a = new Produto(4L, "A", "...", new BigDecimal("10.00"),
                BigDecimal.valueOf(1.0), BigDecimal.valueOf(10), BigDecimal.valueOf(10), BigDecimal.valueOf(10),
                false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinho(item(a, 8L)); // 8*10 = 80; 15% => 68.00

        BigDecimal r = compraService.calcularSubtotalComDescontoPorItens(c);
        assertThat(r).isEqualByComparingTo("68.00");

        assertThat(compraService.descontoPorQuantidade(8L)).isEqualByComparingTo("0.15");
        assertThat(compraService.descontoPorQuantidade(20L)).isEqualByComparingTo("0.15");
    }

    @Test @DisplayName("D6..D9 - múltiplos grupos: somar subtotais por grupo com seus fatores")
    void D6_D9_variosGrupos_somaPorGrupo() {
        Produto a = new Produto(5L, "A", "...", new BigDecimal("10.00"),
                BigDecimal.valueOf(1.0), BigDecimal.valueOf(10), BigDecimal.valueOf(10), BigDecimal.valueOf(10),
                false, TipoProduto.LIVRO);
        Produto b = new Produto(6L, "B", "...", new BigDecimal("20.00"),
                BigDecimal.valueOf(1.0), BigDecimal.valueOf(10), BigDecimal.valueOf(10), BigDecimal.valueOf(10),
                false, TipoProduto.ELETRONICO);

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
        assertThat(compraService.calcularFreteBasePorPeso(BigDecimal.valueOf(0.0))).isEqualByComparingTo("0.00");
        assertThat(compraService.calcularFreteBasePorPeso(BigDecimal.valueOf(5.0))).isEqualByComparingTo("0.00");
    }

    @Test @DisplayName("D14 - 5 < w ≤ 10 → 2*w + 12")
    void D14_calcularFreteBasePorPeso_faixaB() {
        assertThat(compraService.calcularFreteBasePorPeso(BigDecimal.valueOf(5.01))).isEqualByComparingTo("22.02"); // 2*5.01+12
        assertThat(compraService.calcularFreteBasePorPeso(BigDecimal.valueOf(10.0))).isEqualByComparingTo("32.00"); // 2*10+12
    }

    @Test @DisplayName("D15 - 10 < w ≤ 50 → 4*w + 12")
    void D15_calcularFreteBasePorPeso_faixaC() {
        assertThat(compraService.calcularFreteBasePorPeso(BigDecimal.valueOf(15.0))).isEqualByComparingTo("72.00");  // 4*15+12
        assertThat(compraService.calcularFreteBasePorPeso(BigDecimal.valueOf(50.0))).isEqualByComparingTo("212.00"); // 4*50+12
    }

    @Test @DisplayName("D16 - w > 50 → 7*w + 12")
    void D16_calcularFreteBasePorPeso_faixaD() {
        assertThat(compraService.calcularFreteBasePorPeso(BigDecimal.valueOf(60.0))).isEqualByComparingTo("432.00");   // 7*60+12
        assertThat(compraService.calcularFreteBasePorPeso(BigDecimal.valueOf(50.01))).isEqualByComparingTo("362.07");  // 7*50.01+12
    }

    // -------------------------------------------------------
    // calcularPesoTotalTributavel() (max(físico,cúbico) * qtd, somar)
    // (relacionado à D14 da sua tabela de peso tributável)
    // -------------------------------------------------------

    @Test @DisplayName("D14* - peso tributável soma max(físico,cúbico)×quantidade por item")
    void D14_star_calcularPesoTotalTributavel() {
        Produto volumoso = new Produto(100L, "Vol", "...", new BigDecimal("10.00"),
                BigDecimal.valueOf(1.0), // peso
                BigDecimal.valueOf(60),  // c
                BigDecimal.valueOf(40),  // l
                BigDecimal.valueOf(15),  // a
                false, TipoProduto.LIVRO);
        
        Produto pesado = new Produto(101L, "Pes", "...", new BigDecimal("10.00"),
                BigDecimal.valueOf(8.0), // peso
                BigDecimal.valueOf(10),  // c
                BigDecimal.valueOf(10),  // l
                BigDecimal.valueOf(10),  // a
                false, TipoProduto.LIVRO);

        CarrinhoDeCompras c = carrinho(item(volumoso, 2L), item(pesado, 1L));
        
        // Item 1: cúbico = (60*40*15)/6000 = 6.0. (usa 6.0) -> 6.0 * 2 = 12.0
        // Item 2: cúbico = (10*10*10)/6000 = 0.16... (usa 8.0) -> 8.0 * 1 = 8.0
        // Total = 12.0 + 8.0 = 20.0
        BigDecimal peso = compraService.calcularPesoTotalTributavel(c);
        
        assertThat(peso).isEqualByComparingTo("20.00");
    }

    // -------------------------------------------------------
    // calcularTaxaItensFrageis() (tabela D17..D18)
    // -------------------------------------------------------

    @Test @DisplayName("D17 - existe frágil → soma 5,00 × quantidade por item frágil")
    void D17_calcularTaxaItensFrageis_soma() {
        Produto f1 = new Produto(200L, "F1", "...", new BigDecimal("1.00"),
                BigDecimal.valueOf(1.0), BigDecimal.valueOf(1), BigDecimal.valueOf(1), BigDecimal.valueOf(1),
                true, TipoProduto.LIVRO);  // 3 unid → 15
        
        Produto f2 = new Produto(201L, "F2", "...", new BigDecimal("1.00"),
                BigDecimal.valueOf(1.0), BigDecimal.valueOf(1), BigDecimal.valueOf(1), BigDecimal.valueOf(1),
                true, TipoProduto.LIVRO);  // 2 unid → 10

        CarrinhoDeCompras c = carrinho(item(f1, 3L), item(f2, 2L));
        BigDecimal taxa = compraService.calcularTaxaItensFrageis(c);

        assertThat(taxa).isEqualByComparingTo("25.00");
    }

    @Test @DisplayName("D18 - nenhum frágil → 0")
    void D18_calcularTaxaItensFrageis_zero() {
        Produto n = new Produto(202L, "N", "...", new BigDecimal("1.00"),
                BigDecimal.valueOf(1.0), BigDecimal.valueOf(1), BigDecimal.valueOf(1), BigDecimal.valueOf(1),
                false, TipoProduto.LIVRO);

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