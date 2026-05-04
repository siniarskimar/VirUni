import { AfterViewInit, Component, inject, signal, ViewChild } from '@angular/core';
import { RouterOutlet, Router, ActivatedRoute, RouterLink } from '@angular/router';
import { AuthenticationService } from './services/authentication.service';
import { AccountPageComponent } from './pages/account/account-page.component';
import { HomePageComponent } from './pages/home/home-page.component';
import { AuthPageComponent } from './pages/auth/auth-page.component';
import { toSignal } from '@angular/core/rxjs-interop';
import { CoursePageComponent } from './pages/course/course.component';
import { ModalHostComponent } from './services/modal/modal-host.component';
import { ModalService } from './services/modal/modal.service';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    AccountPageComponent,
    HomePageComponent,
    AuthPageComponent,
    CoursePageComponent,
    RouterLink,
    ModalHostComponent,
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements AfterViewInit {
  private readonly authService = inject(AuthenticationService);
  private readonly router = inject(Router);
  private readonly modalService = inject(ModalService);

  @ViewChild(ModalHostComponent) modalHostComponent!: ModalHostComponent;

  accountSession = toSignal(this.authService.session$.asObservable());
  navIsActive = false;

  signOut() {
    this.authService.signOut();
    this.router.navigate(['/auth']);
  }

  ngAfterViewInit(): void {
    this.modalService.registerHost(this.modalHostComponent);
  }
}
