import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface ResumoPendencias {
  possuiPendencias: boolean;
  quantidade: number;
  valorTotal: number;
}

@Injectable({ providedIn: 'root' })
export class PendenciasService {
  private readonly http = inject(HttpClient);

  consultar(clienteId: string): Observable<ResumoPendencias> {
    const correlationId = globalThis.crypto.randomUUID();
    return this.http.get<ResumoPendencias>(
      `/api/clientes/${encodeURIComponent(clienteId)}/pendencias`,
      { headers: new HttpHeaders({ 'X-Correlation-Id': correlationId }) },
    );
  }
}
