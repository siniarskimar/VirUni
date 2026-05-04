import { Component, inject, output } from '@angular/core';
import { AuthenticationService, SignUpForm } from '../../../services/authentication.service';
import { Account } from "@app/services/account.service";


@Component({
  selector: 'app-sign-up-view',
  imports: [],
  templateUrl: './sign-up-view.component.html',
  styleUrl: './sign-up-view.component.css'
})
export class SignUpViewComponent {
  private readonly authService = inject(AuthenticationService);

  authenticated = output<Account>();
  errorOccured = output<Error>();

  isTeacher = false;
  teacherToken = '';

  onSubmit(ev: SubmitEvent) {
    ev.preventDefault();
    const data = new FormData(ev.target as HTMLFormElement);
    const username = data.get("username")?.toString();
    const password = data.get("password")?.toString();
    const firstname = data.get("firstname")?.toString();
    const lastname = data.get("lastname")?.toString();

    if (!username) {
      this.errorOccured.emit(new Error("username is requred"));
      return;
    }
    if (!password) {
      this.errorOccured.emit(new Error("password is requred"));
      return;
    }
    if (!firstname) {
      this.errorOccured.emit(new Error("firstname is requred"));
      return;
    }
    if (!lastname) {
      this.errorOccured.emit(new Error("lastname is requred"));
      return;
    }

    const form: SignUpForm = {
      username: username,
      password,
      firstname,
      lastname
    };

    if (data.get('is-teacher')) {
      form.role = "ROLE_TEACHER";
      form.teacherToken = data.get("teacher-token")?.toString();
      if (!form.teacherToken) {
        this.errorOccured.emit(new Error("registration token is required"));
        return;
      }
    }

    this.authService.signUp(form).subscribe({
      next: (session) => this.authenticated.emit(session.account),
      error: (err) => this.errorOccured.emit(new Error("failed to signup", { cause: err }))
    });
  }

  setIsTeacher(ev: Event) {
    const target = (ev.target as HTMLInputElement | null);
    if (!target) return;

    this.isTeacher = target.checked;
  }

}
