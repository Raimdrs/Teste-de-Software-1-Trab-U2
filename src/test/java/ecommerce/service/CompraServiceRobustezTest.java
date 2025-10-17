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
        Produto produtoPrecoNegativo = new Produto(5L, "Inválido", "...", new BigDecimal("-10.00"), 1, 1, 1, 1, false, null);
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
        // produtoComPesoErrado é inicializado no setUp com peso -60.0
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        ItemCompra itemComErro = new ItemCompra(1L, produtoComPesoErrado, 1L);
        carrinho.setItens(Collections.singletonList(itemComErro));

        // ACT & ASSERT
        assertThatThrownBy(() -> {
            compraService.calcularCustoTotal(carrinho, clienteBronze.getRegiao(), clienteBronze.getTipo());
        })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("O peso físico do item deve ser positivo."); // Mensagem inferida
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
        // Produto com comprimento ZERO
        Produto produtoInvalido = new Produto(6L, "Invalido", "...", new BigDecimal("10.00"), 1, 
                                              0,  // Comprimento
                                              10, // Largura
                                              10, // Altura
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
        // Produto com largura NEGATIVA
        Produto produtoInvalido = new Produto(7L, "Invalido", "...", new BigDecimal("10.00"), 1, 
                                              10,  // Comprimento
                                              -5,  // Largura
                                              10,  // Altura
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
        // Produto com altura ZERO
        Produto produtoInvalido = new Produto(8L, "Invalido", "...", new BigDecimal("10.00"), 1, 
                                              10, // Comprimento
                                              10, // Largura
                                              0,  // Altura
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