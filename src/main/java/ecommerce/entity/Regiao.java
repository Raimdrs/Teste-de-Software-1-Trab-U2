package ecommerce.entity;

import java.math.BigDecimal;

public enum Regiao {
    SUDESTE(new BigDecimal("1.00")),
    SUL(new BigDecimal("1.05")),
    CENTRO_OESTE(new BigDecimal("1.20")),
    NORDESTE(new BigDecimal("1.10")),
	NORTE(new BigDecimal("1.30"));
	

    private final BigDecimal multiplicador;

    Regiao(BigDecimal multiplicador) {
        this.multiplicador = multiplicador;
    }

    public BigDecimal getMultiplicador() {
        return multiplicador;
    }
}