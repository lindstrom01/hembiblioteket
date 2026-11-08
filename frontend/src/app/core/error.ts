import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from './book.model';

export function errorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    const body = err.error as Partial<ApiError> | null;
    if (body?.message) {
      return [body.message, ...(body.details ?? [])].join(' – ');
    }
    if (err.status === 0) {
      return 'Kunde inte nå servern';
    }
  }
  return 'Något gick fel';
}
