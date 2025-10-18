## Partições de Domínio

Carinho de compras é null

| Carrinho de compra é nulo | sim | p1 |
|---------------------------|-----|----|
|                           | Não | p2 |

| A lista de produtos do carrinho está vazia | sim | p3 |
|--------------------------------------------|-----|----|
|                                            | Não | p4 |

| Qantidade do item > 0 | sim | p4 |
|-----------------------|-----|----|
|                       | Não | p5 |

| Preço unitario do item > 0 | sim | p7 |
|----------------------------|-----|----|
|                            | Não | p8 |

| Peso do produto > 0 | sim | p9  |
|---------------------|-----|-----|
|                     | Não | p10 |

| Todas dimensões do produto são > 0 | sim | p11 |
|------------------------------------|-----|-----|
|                                    | Não | p12 |

// Não sei se esse entra, pois em ambos os casos é uma entrada válida,
perguntar ao professor na próxima aula.

| O produto é frágio | sim | p13 |
|--------------------|-----|-----|
|                    | Não | p14 |

| A região é null | sim | p15 |
|-----------------|-----|-----|
|                 | Não | p16 |

| A região é a Sudeste | sim | p17 |
|----------------------|-----|-----|
|                      | Não | p18 |

| A região é a Sul | sim | p19 |
|------------------|-----|-----|
|                  | Não | p20 |

| A região é a Nordeste | sim | p21 |
|-----------------------|-----|-----|
|                       | Não | p22 |

| A região é a Centro-Oeste | sim | p23 |
|---------------------------|-----|-----|
|                           | Não | p24 |

| A região é a Norte | sim | p25 |
|--------------------|-----|-----|
|                    | Não | p26 |



| O tipo de cliente é BRONZE  | sim | p27 |
|-----------------------------|-----|-----|
|                             | Não | p28 |

| O tipo de cliente é PRATA  | sim | p29 |
|----------------------------|-----|-----|
|                            | Não | p30 |

| O tipo de cliente é OURO | sim | p31 |
|--------------------------|-----|-----|
|                          | Não | p32 |

| O tipo de lciente é null | sim | p33 |
|--------------------------|-----|-----|
|                          | Não | p34 |


| Condição                                | Partição                              | Id    |
| --------------------------------------- | ------------------------------------- | ----- |
| Carrinho existe e possui lista de itens | Válido                                | (C1)  |
| Carrinho é `null`                       | Inválido                              | (C2)  |
| Lista de itens é `null`                 | Inválido                              | (C3)  |
| Lista de itens é vazia                  | Inválido                              | (C4)  |
| Quantidade do item (`Long`)             | Válido: `≥ 1`                         | (I1)  |
|                                         | Inválido: `≤ 0` ou `null`             | (I2)  |
| Preço unitário (`BigDecimal`)           | Válido: `≥ 0`                         | (I3)  |
|                                         | Inválido: `< 0`                       | (I4)  |
| Peso físico (`double`)                  | Válido: `> 0`                         | (I5)  |
|                                         | Inválido: `≤ 0`                       | (I6)  |
| Dimensões C×L×A (`double`)              | Válido: **todas** `> 0`               | (I7)  |
|                                         | Inválido: **alguma** `≤ 0`            | (I8)  |
| Tipo do produto (`TipoProduto`)         | Válido: não nulo                      | (I9)  |
|                                         | Inválido: `null`                      | (I10) |
| Fragilidade                             | Válido: **não frágil**                | (I11) |
|                                         | Válido: **frágil** (gera taxa depois) | (I12) |


