# Consulta de pendências de pagamentos

Aplicação de demonstração com frontend Angular, BFF Spring Boot e uma API
simulada de pagamentos. O BFF consulta o serviço de pagamentos e expõe somente
o resumo das pendências de autorização por cliente.

## Executar a aplicação completa

Requisito: Docker com Docker Compose.

```sh
docker compose up --build
```

Abra [http://localhost:8080](http://localhost:8080). O ambiente inicia os três
serviços e encaminha as chamadas do frontend pelo BFF. Para encerrar:

```sh
docker compose down
```

## API simulada de pagamentos

O container `pagamentos-mock` implementa o contrato externo em memória para a
demo:

- Cliente `123`: dois pagamentos pendentes, total de R$ 1.542,30, e um pagamento
  autorizado.
- Cliente `456`: somente um pagamento autorizado, sem pendências.
- Outros IDs válidos: lista de pagamentos vazia.

O BFF chama `GET {PAGAMENTOS_API_BASE_URL}/api/pagamentos?clienteId={id}`. O
serviço deve retornar `clienteId` e uma lista `pagamentos` com `id` (texto),
`valor` (número decimal) e `status` (`PENDENTE` ou `AUTORIZADO`). Os timeouts de
conexão e leitura são, respectivamente, 1 e 2 segundos.

Para consultar uma API real, configure `PAGAMENTOS_API_BASE_URL` com um endereço
alcançável de dentro do container do BFF. Por exemplo, no PowerShell:

```powershell
$env:PAGAMENTOS_API_BASE_URL = "https://pagamentos.exemplo.com"
docker compose up --build
```

O padrão `http://pagamentos-mock:8081` continua sendo usado quando a variável
não está definida. A API simulada pode permanecer iniciada sem ser consultada.

## Executar somente o BFF localmente

Requisitos: Java 21 e uma API de pagamentos disponível. Por padrão, o BFF espera
essa API em `http://localhost:8081`.

```powershell
.\mvnw.cmd spring-boot:run
```

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
