package ecommerce.entity;

import java.math.BigDecimal;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Produto
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nome;
	private String descricao;

	/** Preço unitário em reais (R$). */
	private BigDecimal precoUnitario;

	/** Peso físico em quilogramas (kg). */
	private BigDecimal pesoFisico;

	/** Dimensões em centímetros (cm). */
	private BigDecimal comprimento;
	private BigDecimal largura;
	private BigDecimal altura;

	/** Indica se o produto é frágil. */
	private boolean fragil;

	@Enumerated(EnumType.STRING)
	private TipoProduto tipo;

	public Produto()
	{
	}

	public Produto(Long id, String nome, String descricao, BigDecimal precoUnitario, BigDecimal pesoFisico,
			BigDecimal comprimento, BigDecimal largura, BigDecimal altura, boolean fragil, TipoProduto tipo)
	{
		this.id = id;
		this.nome = nome;
		this.descricao = descricao;
		this.precoUnitario = precoUnitario;
		this.pesoFisico = pesoFisico;
		this.comprimento = comprimento;
		this.largura = largura;
		this.altura = altura;
		this.fragil = fragil;
		this.tipo = tipo;
	}

	// Getters e Setters
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getNome()
	{
		return nome;
	}

	public void setNome(String nome)
	{
		this.nome = nome;
	}

	public String getDescricao()
	{
		return descricao;
	}

	public void setDescricao(String descricao)
	{
		this.descricao = descricao;
	}

	public BigDecimal getPrecoUnitario()
	{
		return precoUnitario;
	}

	public void setPrecoUnitario(BigDecimal precoUnitario)
	{
		this.precoUnitario = precoUnitario;
	}

	public BigDecimal getPesoFisico()
	{
		return pesoFisico;
	}

	public void setPesoFisico(BigDecimal pesoFisico)
	{
		this.pesoFisico = pesoFisico;
	}

	public BigDecimal getComprimento()
	{
		return comprimento;
	}

	public void setComprimento(BigDecimal comprimento)
	{
		this.comprimento = comprimento;
	}

	public BigDecimal getLargura()
	{
		return largura;
	}

	public void setLargura(BigDecimal largura)
	{
		this.largura = largura;
	}

	public BigDecimal getAltura()
	{
		return altura;
	}

	public void setAltura(BigDecimal altura)
	{
		this.altura = altura;
	}

	public boolean isFragil()
	{
		return fragil;
	}

	public void setFragil(boolean fragil)
	{
		this.fragil = fragil;
	}

	public TipoProduto getTipo()
	{
		return tipo;
	}

	public void setTipo(TipoProduto tipo)
	{
		this.tipo = tipo;
	}
}