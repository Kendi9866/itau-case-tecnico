import { createServer } from 'node:http';

const pagamentosPorCliente = {
  '123': [
    { id: '001', valor: 1500.00, status: 'PENDENTE' },
    { id: '002', valor: 300.00, status: 'AUTORIZADO' },
    { id: '003', valor: 42.30, status: 'PENDENTE' },
  ],
  '456': [
    { id: '004', valor: 275.50, status: 'AUTORIZADO' },
  ],
};

const server = createServer((request, response) => {
  const url = new URL(request.url ?? '/', 'http://localhost');

  if (request.method === 'GET' && url.pathname === '/health') {
    response.writeHead(200, { 'Content-Type': 'application/json' });
    response.end(JSON.stringify({ status: 'UP' }));
    return;
  }

  if (request.method !== 'GET' || url.pathname !== '/api/pagamentos') {
    response.writeHead(404);
    response.end();
    return;
  }

  const clienteId = url.searchParams.get('clienteId');
  if (!clienteId) {
    response.writeHead(400, { 'Content-Type': 'application/json' });
    response.end(JSON.stringify({ message: 'clienteId é obrigatório' }));
    return;
  }

  const correlationId = request.headers['x-correlation-id'];
  response.writeHead(200, {
    'Content-Type': 'application/json',
    ...(correlationId ? { 'X-Correlation-Id': correlationId } : {}),
  });
  response.end(JSON.stringify({
    clienteId,
    pagamentos: pagamentosPorCliente[clienteId] ?? [],
  }));
});

server.listen(8081, '0.0.0.0', () => {
  console.log('API simulada de pagamentos ouvindo na porta 8081');
});
