# BFF de pendências de pagamentos

BFF Spring Boot que consulta a API de pagamentos e expõe somente o resumo das
pendências de autorização por cliente.

## Executar

Requisitos: Java 21.

```powershell
.\mvnw.cmd spring-boot:run
```

Por padrão, o BFF espera a API de pagamentos em `http://localhost:8081`. Para
usar outro endereço, configure `PAGAMENTOS_API_BASE_URL`, por exemplo:

```powershell
$env:PAGAMENTOS_API_BASE_URL = "http://localhost:9090"
.\mvnw.cmd spring-boot:run
```

O BFF chama `GET {PAGAMENTOS_API_BASE_URL}/api/pagamentos?clienteId={id}`.
O serviço de domínio deve retornar `clienteId` e uma lista `pagamentos` com
`id` (texto), `valor` (número decimal) e `status` (`PENDENTE` ou `AUTORIZADO`).
Os timeouts de conexão e leitura são, respectivamente, 1 e 2 segundos.

## Endpoint

```http
GET /clientes/{clienteId}/pendencias
```

`clienteId` deve ter de 1 a 50 caracteres alfanuméricos, hífen ou underscore.
Exemplo de resposta:

```json
{
  "possuiPendencias": true,
  "quantidade": 1,
  "valorTotal": 1500.00
}
```

Pagamentos com status diferente de `PENDENTE` não entram no cálculo. Sem
pendências, o endpoint retorna `false`, `0` e `0`.

Erros usam um corpo sem dados internos:

```json
{
  "status": 502,
  "message": "Não foi possível obter os pagamentos",
  "correlationId": "..."
}
```

Cliente inválido retorna HTTP 400; timeout da API retorna 504; indisponibilidade
ou payload inválido retorna 502. O BFF aceita `X-Correlation-Id` quando contém
até 64 caracteres seguros; caso contrário, gera um UUID, devolve-o na resposta,
propaga-o ao serviço de pagamentos e o inclui nos logs.

## Decisões e premissas

- A API de pagamentos é um serviço externo ao BFF, por isso é consumida por
  `RestClient`; não há chamada entre controllers/services para simular HTTP.
- O BFF não mantém dados de pagamento. A massa de exemplo fica nos testes.
- Valores monetários são calculados com `BigDecimal`.
- Respostas ausentes, inválidas ou com pagamentos incompletos são tratadas como
  falha controlada do serviço de domínio.

## Testes

```powershell
.\mvnw.cmd test
```

Os testes cobrem o cálculo do resumo, ausência de pendências, validação antes
da chamada externa, resposta HTTP do BFF e tratamento de erros do serviço de
pagamentos.
