import { Routes } from '@angular/router';
import { MainLayoutComponent } from './components/shared/main-layout/main-layout.component';

export const routes: Routes = [
  { path: '', redirectTo: '/repertoire', pathMatch: 'full' },

  {
    path: '',
    component: MainLayoutComponent,
    children: [
      {
        path: 'repertoire',
        loadComponent: () =>
          import('./components/repertoire/repertoire.component').then(
            (m) => m.RepertoireComponent
          ),
      },
    ],
  },

  { path: '**', redirectTo: '/repertoire' },
];
