import { Component, inject, signal } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from '@angular/router';
import { AuthenticationService } from '@app/services/authentication.service';
import { RegistrationTokenService } from '@app/services/registration-token.service';

@Component({
  selector: 'app-admin',
  imports: [],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminPageComponent {
  private readonly registrationTokenService = inject(RegistrationTokenService);

  generatedToken = signal<string | null>(null);

  generateToken(reusable: boolean, expires?: Date) {
    this.registrationTokenService.generateToken({ reusable, expires: expires?.getTime() }).subscribe({
      next: (resp) => {
        this.generatedToken.set(resp.token);
      }
    })
  }
}


export function admininistratorGuard(): CanActivateFn {
  return (next: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
    const authService = inject(AuthenticationService);

    return authService.session?.isAdmin() || false;
  }
}


