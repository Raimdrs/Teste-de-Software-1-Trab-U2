package ecommerce.service;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;

import ecommerce.entity.Cliente;
import ecommerce.entity.Produto;
import ecommerce.entity.Regiao;
import ecommerce.entity.TipoCliente;
import ecommerce.entity.TipoProduto;

/**
 * Classe base abstrata para testes do CompraService.
 * Contém a configuração comum (setUp) e as entidades de teste 
 * compartilhadas para evitar duplicação de código.
 */
public abstract class CompraServiceTestBase {

    protected CompraService compraService;
    protected Cliente clienteBronze;
    protected Cliente clientePrata;
    protected Cliente clienteOuro;
    protected Produto produtoLeve; // < 5kg
    protected Produto produtoMedio; // 8kg
    protected Produto produtoPesado; // 15kg
    protected Produto produtoMuitoPesado; // 60kg
    protected Produto produtoComPesoErrado; // < 0kg
    
    

    @BeforeEach
    void setUp() {
        this.compraService = new CompraService(null, null, null, null);

        // --- Clientes ---
        this.clienteBronze = new Cliente();
        clienteBronze.setRegiao(Regiao.SUDESTE); // Multiplicador 1.0
        clienteBronze.setTipo(TipoCliente.BRONZE); // Sem desconto no frete

        this.clientePrata = new Cliente();
        clientePrata.setRegiao(Regiao.NORDESTE); // Multiplicador 1.10
        clientePrata.setTipo(TipoCliente.PRATA); // 50% desconto frete

        this.clienteOuro = new Cliente();
        clienteOuro.setRegiao(Regiao.SUDESTE);
        clienteOuro.setTipo(TipoCliente.OURO); // 100% desconto frete

       // Subtotal > 500, Peso Leve (1kg)
        this.produtoLeve = new Produto(1L, "Celular", "...", new BigDecimal("900.00"), 1.0, 15, 8, 2, false, 
            TipoProduto.ELETRONICO); 
        
        // Subtotal > 1000, Peso Médio (8kg), Frágil
        this.produtoMedio = new Produto(2L, "Monitor 4k", "...", new BigDecimal("1200.00"), 8.0, 60, 40, 15, true, 
            TipoProduto.ELETRONICO); 
        
        // Subtotal < 500, Peso Pesado (15kg)
        this.produtoPesado = new Produto(3L, "Halter 15kg", "...", new BigDecimal("400.00"), 15.0, 30, 15, 15, false, 
            TipoProduto.MOVEL);

        // Subtotal < 500, Peso Muito Pesado (60kg)
        this.produtoMuitoPesado = new Produto(4L, "Saco de Cimento", "...", new BigDecimal("100.00"), 60.0, 80, 50, 10, false, 
            TipoProduto.MOVEL); 
    
        // Produto com peso negativo (para testes de robustez)
        this.produtoComPesoErrado = new Produto(5L, "Produto Inválido", "...", new BigDecimal("100.00"), -60.0, 80, 50, 10, false, 
            TipoProduto.MOVEL);
    }
}