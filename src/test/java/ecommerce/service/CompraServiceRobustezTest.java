package ecommerce.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;

public class CompraServiceRobustezTest extends CompraServiceTestBase {

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
    @DisplayName("Cobre: Validação de Lista de Itens Nula")
    void deveLancarExcecaoParaListaDeItensNula() {
        // ARRANGE
        CarrinhoDeCompras carrinhoComListaNula = new CarrinhoDeCompras();
        carrinhoComListaNula.setItens(null); // Define a lista de itens como nula

        // ACT & ASSERT
        assertThatThrownBy(() -> {
            compraService.calcularCustoTotal(carrinhoComListaNula, clienteBronze.getRegiao(), clienteBronze.getTipo());
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
        Produto produtoPrecoNegativo = new Produto(5L, "Inválido", "...", new BigDecimal("-10.00"), 
            BigDecimal.valueOf(1), // peso
            BigDecimal.valueOf(1), // comprimento
            BigDecimal.valueOf(1), // largura
            BigDecimal.valueOf(1), // altura
            false, 
            null);
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
    @DisplayName("Cobre: Validação de Peso <= 0")
    void deveLancarExcecaoParaPesoNegativo() {
        // ARRANGE
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        ItemCompra itemComErro = new ItemCompra(1L, produtoComPesoErrado, 1L);
        carrinho.setItens(Collections.singletonList(itemComErro));

        // ACT & ASSERT
        assertThatThrownBy(() -> {
            compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());
        })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("O peso físico do item deve ser positivo.");
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
        .isInstanceOf(NullPointerException.class) 
        .hasMessage("Tipo de cliente não pode ser nulo.");
    }

    @Test
    @DisplayName("Cobre: Validação de Dimensão (Comprimento <= 0)")
    void deveLancarExcecaoParaComprimentoInvalido() {
        // ARRANGE
        Produto produtoInvalido = new Produto(6L, "Invalido", "...", new BigDecimal("10.00"), 
                                              BigDecimal.valueOf(1),  // peso
                                              BigDecimal.valueOf(0),  // Comprimento
                                              BigDecimal.valueOf(10), // Largura
                                              BigDecimal.valueOf(10), // Altura
                                              false, null);
        
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        ItemCompra itemComErro = new ItemCompra(1L, produtoInvalido, 1L);
        carrinho.setItens(Collections.singletonList(itemComErro));

        // ACT & ASSERT
        assertThatThrownBy(() -> {
            compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());
        })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("As dimensões (C, L, A) do item devem ser positivas.");
    }

    @Test
    @DisplayName("Cobre: Validação de Dimensão (Largura <= 0)")
    void deveLancarExcecaoParaLarguraInvalida() {
        // ARRANGE
        Produto produtoInvalido = new Produto(7L, "Invalido", "...", new BigDecimal("10.00"), 
                                              BigDecimal.valueOf(1), 
                                              BigDecimal.valueOf(10),  // Comprimento
                                              BigDecimal.valueOf(-5),  // Largura
                                              BigDecimal.valueOf(10),  // Altura
                                              false, null);
        
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        ItemCompra itemComErro = new ItemCompra(1L, produtoInvalido, 1L);
        carrinho.setItens(Collections.singletonList(itemComErro));

        // ACT & ASSERT
        assertThatThrownBy(() -> {
            compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());
        })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("As dimensões (C, L, A) do item devem ser positivas.");
    }

    @Test
    @DisplayName("Cobre: Validação de Dimensão (Altura <= 0)")
    void deveLancarExcecaoParaAlturaInvalida() {
        // ARRANGE
        Produto produtoInvalido = new Produto(8L, "Invalido", "...", new BigDecimal("10.00"), 
                                              BigDecimal.valueOf(1), 
                                              BigDecimal.valueOf(10), // Comprimento
                                              BigDecimal.valueOf(10), // Largura
                                              BigDecimal.valueOf(0),  // Altura
                                              false, null);
        
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        ItemCompra itemComErro = new ItemCompra(1L, produtoInvalido, 1L);
        carrinho.setItens(Collections.singletonList(itemComErro));

        // ACT & ASSERT
        assertThatThrownBy(() -> {
            compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());
        })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("As dimensões (C, L, A) do item devem ser positivas.");
    }
}