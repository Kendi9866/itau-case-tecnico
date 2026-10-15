import { HttpErrorResponse } from '@angular/common/http';
import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PendenciasService, ResumoPendencias } from './pendencias.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CurrencyPipe, FormsModule],
  templateUrl: './app.component.html',
})
export class AppComponent {
  private readonly pendenciasService = inject(PendenciasService);

  clienteId = '';
  readonly resultado = signal<ResumoPendencias | null>(null);
  readonly carregando = signal(false);
  readonly erro = signal('');

  consultar(): void {
    const clienteId = this.clienteId.trim();
    if (!/^[A-Za-z0-9_-]{1,50}$/.test(clienteId)) {
      this.resultado.set(null);
      this.erro.set('Informe um ID com até 50 caracteres: letras, números, hífen ou underscore.');
      return;
    }

    this.erro.set('');
    this.resultado.set(null);
    this.carregando.set(true);

    this.pendenciasService.consultar(clienteId).subscribe({
      next: (resumo) => this.resultado.set(resumo),
      error: (error: unknown) => {
        this.erro.set(this.mensagemErro(error));
        this.carregando.set(false);
      },
      complete: () => this.carregando.set(false),
    });
  }

  private mensagemErro(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const mensagem = (error.error as { message?: unknown } | null)?.message;
      if (typeof mensagem === 'string') {
        return mensagem;
      }
    }
    return 'Não foi possível consultar as pendências. Tente novamente.';
  }
}
