import { Routes } from '@angular/router';
import { RepertoireComponent } from './components/repertoire/repertoire.component';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: '/repertoire',
  },
  { path: 'repertoire', component: RepertoireComponent },
];
