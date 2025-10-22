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


### Desconto por Quantidade de Itens do Mesmo Tipo:

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


## Testes de Análise de Valor Limite:

### Testes para desconto aplicado por valor subtotal:

| **ID** | **Input (Subtotal)** | **Contexto**                           | **R.E (Resultado Esperado)** | **Valor Final (R$)** |
| ------ | -------------------- | -------------------------------------- | ---------------------------- | -------------------: |
| L1     | R$ 499,00            | Cliente Bronze / 1 kg                  | SUCESSO (Desc. 0%)           |               499,00 |
| L2     | R$ 500,00            | Cliente Bronze / 61 kg (frete faixa D) | SUCESSO (Desc. 0%)           |             1.037,00 |
| L3     | R$ 500,01            | Cliente Bronze / 1 kg                  | SUCESSO (Desc. 10%)          |               450,01 |
| L4     | R$ 999,00            | Cliente Bronze / 1 kg                  | SUCESSO (Desc. 10%)          |               899,10 |
| L5     | R$ 1.000,00          | Cliente Bronze / 61 kg (frete faixa D) | SUCESSO (Desc. 10%)          |             1.339,00 |
| L6     | R$ 1.000,01          | Cliente Bronze / 1 kg                  | SUCESSO (Desc. 20%)          |               800,01 |


### Teste para calculo do frete com relação ao peso dos itens:

Base dos casos: 1 item, preço R$ 100, cliente Bronze, região Sudeste.

| **ID** | **Input (Peso Total)** | **Contexto**            | **R.E (Resultado Esperado)** | **Valor Final (R$)** |
| ------ | ---------------------- | ----------------------- | ---------------------------- | -------------------: |
| L7     | 5,00 kg                | Cliente Bronze / R$ 100 | SUCESSO (Frete isento)       |               100,00 |
| L8     | 5,01 kg                | Cliente Bronze / R$ 100 | SUCESSO (Frete Faixa B)      |               122,02 |
| L9     | 9,99 kg                | Cliente Bronze / R$ 100 | SUCESSO (Frete Faixa B)      |               131,98 |
| L10    | 10,00 kg               | Cliente Bronze / R$ 100 | SUCESSO (Frete Faixa B)      |               132,00 |
| L11    | 10,01 kg               | Cliente Bronze / R$ 100 | SUCESSO (Frete Faixa C)      |               152,04 |
| L12    | 49,99 kg               | Cliente Bronze / R$ 100 | SUCESSO (Frete Faixa C)      |               311,96 |
| L13    | 50,00 kg               | Cliente Bronze / R$ 100 | SUCESSO (Frete Faixa C)      |               312,00 |
| L14    | 50,01 kg               | Cliente Bronze / R$ 100 | SUCESSO (Frete Faixa D)      |               462,07 |


### Teste para múltiplos itens do mesmo tipo:

Base: mesmo TipoProduto, preço unitário R$ 100, peso 0,5 kg por item 
(para não interferir no frete), cliente Bronze, região Sudeste.

| **ID** | **Input (Qtd. Mesmo Tipo)** | **Contexto**            | **R.E (Resultado Esperado)**         | **Valor Final (R$)** |
|--------| --------------------------- | ----------------------- | ------------------------------------ | -------------------: |
| L15    | 2 iténs                     | Cliente Bronze / 1 kg   | SUCESSO (Desc. Tipo 0%)              |               200,00 |
| L16    | 3 iténs                     | Cliente Bronze / 1,5 kg | SUCESSO (Desc. Tipo 5%)              |               285,00 |
| L17    | 4 iténs                     | Cliente Bronze / 2 kg   | SUCESSO (Desc. Tipo 5%)              |               380,00 |
| L18    | 5 iténs                     | Cliente Bronze / 2,5 kg | SUCESSO (Desc. Tipo 10%)             |               450,00 |
| L19    | 7 iténs                     | Cliente Bronze / 3,5 kg | SUCESSO (Desc. Tipo 10% + Valor 10%) |               567,00 |
| L20    | 8 iténs                     | Cliente Bronze / 4 kg   | SUCESSO (Desc. Tipo 15% + Valor 10%) |               612,00 |


### Complexidade ciclomática

P_total = P(main) + P(validar) + P(descValor) + P(descItens) + P(descQtd) + P(freteTotal)
P_total = 5 + 7 + 2 + 2 + 3 + 9
P_total = 28
V(G) = P_total + 1
V(G) = 28 + 1
V(G) = 29
