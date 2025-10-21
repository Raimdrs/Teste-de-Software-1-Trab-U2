package ecommerce.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.ItemCompra;

/*
public class CompraServiceParticaoEquivalenciaTest extends CompraServiceTestBase {

    @Test
    @DisplayName("Partição: Subtotal (500, 1000] (10% desc), Peso [0, 5] (Isento)")
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
    @DisplayName("Partição: Subtotal [0, 500] (Sem desc), Peso (10, 50] (Frete C)")
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
    @DisplayName("Partição: Subtotal [0, 500] (Sem desc), Peso > 50 (Frete D)")
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
    @DisplayName("Partição: Subtotal > 1000 (20% desc), Peso [0, 5] (Isento) com 3 itens")
    void deveCalcularCustoTotalParaTresItens()
    {
        // ARRANGE
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        // 3 * 900 = 2700 (Subtotal Faixa 3)
        // 3 * 1kg = 3kg (Peso Faixa Isenta)
        ItemCompra itemUnico = new ItemCompra(1L, produtoLeve, 3L);
        carrinho.setItens(Collections.singletonList(itemUnico));

        // ACT
        BigDecimal custoTotalCalculado = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());

        // ASSERT
        assertThat(custoTotalCalculado).isEqualByComparingTo("2052.00");
    }

    @Test
    @DisplayName("Partição: Subtotal > 1000 (20% desc), Peso (5, 10] (Frete B) com 8 itens")
    void deveCalcularCustoTotalParaVariosItens()
    {
        // ARRANGE
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        // 8 * 900 = 7200 (Subtotal Faixa 3)
        // 8 * 1kg = 8kg (Peso Faixa B)
        ItemCompra itemUnico = new ItemCompra(1L, produtoLeve, 8L);
        carrinho.setItens(Collections.singletonList(itemUnico));

        // ACT
        BigDecimal custoTotalCalculado = compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());

        // ASSERT
        assertThat(custoTotalCalculado).isEqualByComparingTo("4924.00");
    }
}*/


import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import ecommerce.entity.Cliente;
import ecommerce.entity.Produto;
import ecommerce.entity.Regiao;
import ecommerce.entity.TipoCliente;
import ecommerce.entity.TipoProduto;

/**
 * Testes de Partição de Equivalência.
 * Cenário base (válido):
 * - 1 item NÃO frágil (qtd=1, preço=10, peso=4kg, C=L=A=1)
 * - TipoProduto=A, grupo com <= 2 itens (sem desconto por quantidade)
 * - Região=SUDESTE
 * - Cliente=BRONZE
 * - Frete isento (Faixa A)
 * Tudo que não é citado em cada teste permanece igual ao base.
 */
public class CompraServiceParticaoEquivalenciaTest extends CompraServiceTestBase {

    // ---------------------- Utilidades locais ----------------------

    private Produto baseProdutoNaoFragil() {
        // preço 10, peso 4kg (Faixa A), dimensões positivas
        return new Produto(1000L, "Base", "...", new BigDecimal("10.00"),
                4.0, 1, 1, 1, false, TipoProduto.ELETRONICO);
    }

    private CarrinhoDeCompras carrinhoCom(ItemCompra... itens) {
        CarrinhoDeCompras c = new CarrinhoDeCompras();
        c.setItens(List.of(itens));
        return c;
    }

    private Cliente cliente(Regiao regiao, TipoCliente tipo) {
        Cliente cli = new Cliente();
        cli.setRegiao(regiao);
        cli.setTipo(tipo);
        return cli;
    }

    // ---------------------- BASE VÁLIDO ----------------------

    @Test
    @DisplayName("Base válido: carrinho ok, 1 item não frágil (4kg), SE/BRONZE (frete isento)")
    void baseValido() {
        Produto p = baseProdutoNaoFragil();
        CarrinhoDeCompras carrinho = carrinhoCom(new ItemCompra(1L, p, 1L));

        BigDecimal total = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);

        assertThat(total).isEqualByComparingTo("10.00"); // sem desconto por valor, frete isento
    }

    // ---------------------- CARRINHO ----------------------

    @Test
    @DisplayName("P1: carrinho = null → NPE")
    void P1_carrinhoNull() {
        assertThatThrownBy(() ->
                compraService.calcularCustoTotal(null, Regiao.SUDESTE, TipoCliente.BRONZE)
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("P3: carrinho.itens = null → 'Carrinho não pode estar vazio.'")
    void P3_carrinhoItensNull() {
        CarrinhoDeCompras c = new CarrinhoDeCompras();
        c.setItens(null);

        assertThatThrownBy(() ->
                compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Carrinho não pode estar vazio");
    }

    @Test
    @DisplayName("P3: carrinho.itens = [] → 'Carrinho não pode estar vazio.'")
    void P3_carrinhoItensVazio() {
        CarrinhoDeCompras c = new CarrinhoDeCompras();
        c.setItens(Collections.emptyList());

        assertThatThrownBy(() ->
                compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Carrinho não pode estar vazio");
    }

    // ---------------------- ITENS DO CARRINHO ----------------------

    @Test
    @DisplayName("P6: quantidade = 0 → 'A quantidade do item deve ser positiva.'")
    void P6_quantidadeZero() {
        Produto p = baseProdutoNaoFragil();
        CarrinhoDeCompras c = carrinhoCom(new ItemCompra(1L, p, 0L));

        assertThatThrownBy(() ->
                compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A quantidade do item deve ser positiva");
    }

    @Test
    @DisplayName("P8: preço unitário < 0 → 'O preço do item não pode ser negativo.'")
    void P8_precoNegativo() {
        Produto p = new Produto(1001L, "X", "...", new BigDecimal("-0.01"),
                4.0, 1, 1, 1, false, TipoProduto.ELETRONICO);
        CarrinhoDeCompras c = carrinhoCom(new ItemCompra(1L, p, 1L));

        assertThatThrownBy(() ->
                compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("O preço do item não pode ser negativo");
    }

    @Test
    @DisplayName("P10: peso físico = 0 → 'O peso físico do item deve ser positivo.'")
    void P10_pesoFisicoZero() {
        Produto p = new Produto(1002L, "X", "...", new BigDecimal("10.00"),
                0.0, 1, 1, 1, false, TipoProduto.ELETRONICO);
        CarrinhoDeCompras c = carrinhoCom(new ItemCompra(1L, p, 1L));

        assertThatThrownBy(() ->
                compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("O peso físico do item deve ser positivo");
    }

    @Test
    @DisplayName("P12: alguma dimensão = 0 → 'As dimensões (C, L, A) do item devem ser positivas.'")
    void P12_dimensaoZero() {
        Produto p = new Produto(1003L, "X", "...", new BigDecimal("10.00"),
                4.0, 0, 1, 1, false, TipoProduto.ELETRONICO);
        CarrinhoDeCompras c = carrinhoCom(new ItemCompra(1L, p, 1L));

        assertThatThrownBy(() ->
                compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("As dimensões (C, L, A) do item devem ser positivas");
    }

    @Test
    @DisplayName("P13: item frágil = true → taxa adicional de frágil (+5)")
    void P13_itemFragil() {
        Produto p = new Produto(1004L, "X", "...", new BigDecimal("10.00"),
                4.0, 1, 1, 1, true, TipoProduto.ELETRONICO);
        CarrinhoDeCompras c = carrinhoCom(new ItemCompra(1L, p, 1L));

        BigDecimal total = compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE);
        // frete isento por peso, mas aplica taxa frágil (+5)
        assertThat(total).isEqualByComparingTo("15.00");
    }

    // ---------------------- DESCONTO POR QUANTIDADE (mesmo tipo) ----------------------

    private Produto grupoPreco100Peso050() {
        // peso 0,5 kg por item: até 8 itens continua na faixa isenta
        return new Produto(1100L, "G100", "...", new BigDecimal("100.00"),
                0.5, 1, 1, 1, false, TipoProduto.ELETRONICO);
    }

    @Test
    @DisplayName("P16: grupo com 3 itens → 5% por quantidade")
    void P16_grupo3itens() {
        CarrinhoDeCompras c = carrinhoCom(new ItemCompra(1L, grupoPreco100Peso050(), 3L));
        BigDecimal total = compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("285.00"); // 300 * 0.95
    }

    @Test
    @DisplayName("P17: grupo com 6 itens → 10% por quantidade (+ 10% por valor > 500)")
    void P17_grupo6itens() {
        CarrinhoDeCompras c = carrinhoCom(new ItemCompra(1L, grupoPreco100Peso050(), 6L));
        BigDecimal total = compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE);
        // 600 * 0.90 = 540; depois 10% por valor -> 486
        assertThat(total).isEqualByComparingTo("486.00");
    }

    @Test
    @DisplayName("P18: grupo com 8 itens → 15% por quantidade (+ 10% por valor > 500)")
    void P18_grupo8itens() {
        CarrinhoDeCompras c = carrinhoCom(new ItemCompra(1L, grupoPreco100Peso050(), 8L));
        BigDecimal total = compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE);
        // 800 * 0.85 = 680; depois 10% por valor -> 612
        assertThat(total).isEqualByComparingTo("612.00");
    }

    // ---------------------- REGIÃO (precisa ter frete > 0 para efeito aparecer) ----------------------

    private CarrinhoDeCompras carrinhoPeso8kgSubtotal100() {
        Produto p = new Produto(1200L, "8kg", "...", new BigDecimal("100.00"),
                8.0, 10, 10, 10, false, TipoProduto.LIVRO); // 8kg => faixa B
        return carrinhoCom(new ItemCompra(1L, p, 1L));
    }

    @Test
    @DisplayName("P23: região = SUL (mult. 1,05)")
    void P23_regiaoSul() {
        BigDecimal total = compraService.calcularCustoTotal(carrinhoPeso8kgSubtotal100(), Regiao.SUL, TipoCliente.BRONZE);
        // frete base B: 2*8 + 12 = 28; região 1,05 -> 29,40
        assertThat(total).isEqualByComparingTo("129.40");
    }

    @Test
    @DisplayName("P25: região = NORDESTE (mult. 1,10)")
    void P25_regiaoNordeste() {
        BigDecimal total = compraService.calcularCustoTotal(carrinhoPeso8kgSubtotal100(), Regiao.NORDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("130.80"); // 28 * 1,10 + 100
    }

    @Test
    @DisplayName("P27: região = CENTRO-OESTE (mult. 1,20)")
    void P27_regiaoCentroOeste() {
        BigDecimal total = compraService.calcularCustoTotal(carrinhoPeso8kgSubtotal100(), Regiao.CENTRO_OESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("133.60"); // 28 * 1,20 + 100
    }

    @Test
    @DisplayName("P29: região = NORTE (mult. 1,30)")
    void P29_regiaoNorte() {
        BigDecimal total = compraService.calcularCustoTotal(carrinhoPeso8kgSubtotal100(), Regiao.NORTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("136.40"); // 28 * 1,30 + 100
    }

    @Test
    @DisplayName("P19: região = null → NPE")
    void P19_regiaoNull() {
        assertThatThrownBy(() ->
                compraService.calcularCustoTotal(carrinhoPeso8kgSubtotal100(), null, TipoCliente.BRONZE)
        ).isInstanceOf(NullPointerException.class);
    }

    // ---------------------- FAIXAS DE PESO (somente a partição de peso muda) ----------------------

    @Test
    @DisplayName("P32: peso total = 8 kg → Faixa B (2*w + 12)")
    void P32_pesoFaixaB() {
        BigDecimal total = compraService.calcularCustoTotal(carrinhoPeso8kgSubtotal100(), Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("128.00"); // 100 + (28)
    }

    @Test
    @DisplayName("P33: peso total = 15 kg → Faixa C (4*w + 12)")
    void P33_pesoFaixaC() {
        Produto p = new Produto(1300L, "15kg", "...", new BigDecimal("100.00"),
                15.0, 10, 10, 10, false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinhoCom(new ItemCompra(1L, p, 1L));

        BigDecimal total = compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("172.00"); // 100 + (4*15 + 12)
    }

    @Test
    @DisplayName("P34: peso total = 60 kg → Faixa D (7*w + 12)")
    void P34_pesoFaixaD() {
        Produto p = new Produto(1301L, "60kg", "...", new BigDecimal("100.00"),
                60.0, 10, 10, 10, false, TipoProduto.LIVRO);
        CarrinhoDeCompras c = carrinhoCom(new ItemCompra(1L, p, 1L));

        BigDecimal total = compraService.calcularCustoTotal(c, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("532.00"); // 100 + (7*60 + 12)
    }

    // ---------------------- TIPO DE CLIENTE (apenas fidelidade muda) ----------------------

    @Test
    @DisplayName("P37: tipoCliente = PRATA → 50% do frete após multiplicador")
    void P37_tipoClientePrata() {
        BigDecimal total = compraService.calcularCustoTotal(carrinhoPeso8kgSubtotal100(), Regiao.SUDESTE, TipoCliente.PRATA);
        assertThat(total).isEqualByComparingTo("114.00"); // 100 + (28 * 0.50)
    }

    @Test
    @DisplayName("P39: tipoCliente = OURO → frete grátis")
    void P39_tipoClienteOuro() {
        BigDecimal total = compraService.calcularCustoTotal(carrinhoPeso8kgSubtotal100(), Regiao.SUDESTE, TipoCliente.OURO);
        assertThat(total).isEqualByComparingTo("100.00"); // 100 + 0
    }

    @Test
    @DisplayName("P41: tipoCliente = null → NPE")
    void P41_tipoClienteNull() {
        assertThatThrownBy(() ->
                compraService.calcularCustoTotal(carrinhoPeso8kgSubtotal100(), Regiao.SUDESTE, null)
        ).isInstanceOf(NullPointerException.class);
    }
}
