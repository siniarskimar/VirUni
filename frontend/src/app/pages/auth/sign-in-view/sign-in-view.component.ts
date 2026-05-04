import { Component, inject, output } from '@angular/core';
import { AuthenticationService } from '../../../services/authentication.service';
import { Account } from '@app/services/account.service';
import { PasswordInputComponent } from '../../../components/password-input/password-input.component';

@Component({
  selector: 'app-sign-in-view',
  imports: [
    PasswordInputComponent,
  ],
  templateUrl: './sign-in-view.component.html',
  styleUrl: './sign-in-view.component.css'
})
export class SignInViewComponent {
  private readonly authService = inject(AuthenticationService);

  authenticated = output<Account>();
  errorOccured = output<Error>();

  onSubmit(ev: SubmitEvent) {
    ev.preventDefault();
    const data = new FormData(ev.target as HTMLFormElement);
    let username = data.get("username");
    let password = data.get("password");

    if (!username) { this.errorOccured.emit(new Error("missing form field: username")); return; }
    if (!password) { this.errorOccured.emit(new Error("missing form field: password")); return; }

    username = username.toString();
    password = password.toString();

    this.authService.signIn({ username, password })
      .subscribe({
        next: (session) => this.authenticated.emit(session.account),
        error: (err) => this.errorOccured.emit(new Error("failed to signin", { cause: err })),
      });
  }

}
