## Partições de Domínio

### Carinho de compras:

| Condição                                 | Valor | Partição |
| ---------------------------------------- | ----- | -------- |
| Carrinho é nulo                          | Sim   | **P1**   |
|                                          | Não   | **P2**   |
| Lista de produtos do carrinho está vazia | Sim   | **P3**   |
|                                          | Não   | **P4**   |

### Itens do Carrinho

| Condição                         | Valor | Partição |
| -------------------------------- | ----- | -------- |
| Quantidade do item > 0           | Sim   | **P5**   |
|                                  | Não   | **P6**   |
| Preço unitário do item ≥ 0       | Sim   | **P7**   |
|                                  | Não   | **P8**   |
| Peso físico do produto > 0       | Sim   | **P9**   |
|                                  | Não   | **P10**  |
| Todas as dimensões (C, L, A) > 0 | Sim   | **P11**  |
|                                  | Não   | **P12**  |
| O produto é frágil               | Sim   | **P13**  |
|                                  | Não   | **P14**  |


### esconto por Quantidade de Itens do Mesmo Tipo:

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

### Região:

| Condição              | Valor | Partição |
| --------------------- | ----- | -------- |
| Região é nula         | Sim   | **P19**  |
|                       | Não   | **P20**  |
| Região = Sudeste      | Sim   | **P21**  |
|                       | Não   | **P22**  |
| Região = Sul          | Sim   | **P23**  |
|                       | Não   | **P24**  |
| Região = Nordeste     | Sim   | **P25**  |
|                       | Não   | **P26**  |
| Região = Centro-Oeste | Sim   | **P27**  |
|                       | Não   | **P28**  |
| Região = Norte        | Sim   | **P29**  |
|                       | Não   | **P30**  |


### Tipo de Cliente:

Eu começo por um cenário base 100% válido e, em cada linha seguinte, altero só uma 
partição para cobrir todas as demais. Convenção

Cenário base (válido):
Carrinho com 1 item não frágil (qtd=1, preço=10, peso=4 kg, C=L=A=1), TipoProduto=A com 2 itens no grupo (sem desconto por quantidade), Região=SE, Cliente=BRONZE.

Tudo que não é citado em cada linha permanece igual ao base.

| Condição                 | Valor | Partição |
| ------------------------ | ----- | -------- |
| Tipo de cliente = BRONZE | Sim   | **P35**  |
|                          | Não   | **P36**  |
| Tipo de cliente = PRATA  | Sim   | **P37**  |
|                          | Não   | **P38**  |
| Tipo de cliente = OURO   | Sim   | **P39**  |
|                          | Não   | **P40**  |
| Tipo de cliente é nulo   | Sim   | **P41**  |
|                          | Não   | **P42**  |





| **Entrada (variação em relação ao cenário base)**         | **Saída Esperada**                                              | **Partição Coberta**                             |
| --------------------------------------------------------- | --------------------------------------------------------------- | ------------------------------------------------ |
| **Base válido** (descrição acima)                         | Aceito (segue cálculo; frete isento pela Faixa A)               | P2, P4, P5, P7, P9, P11, P14, P15, P21, P31, P35 |
| `carrinho = null`                                         | Exceção (NullPointerException)                                  | **P1**                                           |
| `carrinho.itens = null`                                   | Exceção (“Carrinho não pode estar vazio.”)                      | **P3**                                           |
| `carrinho.itens = []`                                     | Exceção (“Carrinho não pode estar vazio.”)                      | **P3**                                           |
| Item com `quantidade = 0L`                                | Exceção (“A quantidade do item deve ser positiva.”)             | **P6**                                           |
| Item com `preço = -0.01`                                  | Exceção (“O preço do item não pode ser negativo.”)              | **P8**                                           |
| Item com `pesoFisico = 0`                                 | Exceção (“O peso físico do item deve ser positivo.”)            | **P10**                                          |
| Item com `comprimento = 0` (ou `largura/altura ≤ 0`)      | Exceção (“As dimensões (C, L, A) do item devem ser positivas.”) | **P12**                                          |
| Item marcado **frágil** (`fragil=true`)                   | Aceito (frete inclui taxa por frágil)                           | **P13**                                          |
| Grupo `TipoProduto=A` com **3** itens                     | Aceito (aplica 5% por quantidade)                               | **P16**                                          |
| Grupo `TipoProduto=A` com **6** itens                     | Aceito (aplica 10% por quantidade)                              | **P17**                                          |
| Grupo `TipoProduto=A` com **8** itens                     | Aceito (aplica 15% por quantidade)                              | **P18**                                          |
| `regiao = SUL`                                            | Aceito (multiplicador 1,05)                                     | **P23**                                          |
| `regiao = NORDESTE`                                       | Aceito (multiplicador 1,10)                                     | **P25**                                          |
| `regiao = CENTRO_OESTE`                                   | Aceito (multiplicador 1,20)                                     | **P27**                                          |
| `regiao = NORTE`                                          | Aceito (multiplicador 1,30)                                     | **P29**                                          |
| `regiao = null`                                           | Exceção (NullPointerException)                                  | **P19**                                          |
| **peso total = 8 kg** (mantendo item/dimensões coerentes) | Aceito (frete Faixa B: 2×w + 12)                                | **P32**                                          |
| **peso total = 15 kg**                                    | Aceito (frete Faixa C: 4×w + 12)                                | **P33**                                          |
| **peso total = 60 kg**                                    | Aceito (frete Faixa D: 7×w + 12)                                | **P34**                                          |
| `tipoCliente = PRATA`                                     | Aceito (frete 50% após multiplicador)                           | **P37**                                          |
| `tipoCliente = OURO`                                      | Aceito (frete 0 após multiplicador)                             | **P39**                                          |
| `tipoCliente = null`                                      | Exceção (NullPointerException)                                  | **P41**                                          |


~~~~