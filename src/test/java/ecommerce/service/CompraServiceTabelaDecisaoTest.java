package ecommerce.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.ItemCompra;

public class CompraServiceTabelaDecisaoTest extends CompraServiceTestBase {

    @Test
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
    }
}