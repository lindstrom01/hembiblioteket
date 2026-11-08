import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'books' },
  {
    path: 'books',
    loadComponent: () => import('./pages/book-list/book-list-page').then((m) => m.BookListPage),
    title: 'Mitt bibliotek',
  },
  {
    path: 'books/:id',
    loadComponent: () => import('./pages/book-edit/book-edit-page').then((m) => m.BookEditPage),
    title: 'Redigera bok',
  },
  {
    path: 'scan',
    loadComponent: () => import('./pages/scan/scan-page').then((m) => m.ScanPage),
    title: 'Lägg till bok',
  },
  { path: '**', redirectTo: 'books' },
];
