package ecommerce.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.ItemCompra;

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
}