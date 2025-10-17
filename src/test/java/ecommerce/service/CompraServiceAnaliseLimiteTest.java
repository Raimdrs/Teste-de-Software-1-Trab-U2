package ecommerce.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.ItemCompra;


public class CompraServiceAnaliseLimiteTest extends CompraServiceTestBase {

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
        // Usamos o 'clienteOuro' definido na classe base
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteOuro.getRegiao(), clienteOuro.getTipo());

        // ASSERT
        assertThat(custoTotal)
            .as("Custo Total da Compra para Cliente Ouro com 3 itens")
            .isEqualByComparingTo("3240.00");
    }
    
    // NOTA: Seria ideal adicionar mais testes de limite aqui, por exemplo:
    // - Subtotal exatamente 500.00
    // - Subtotal exatamente 1000.00
    // - Peso exatamente 5.01kg (primeiro valor fora do limite)
    // - Peso exatamente 10.0kg
    // - Peso exatamente 50.0kg
}