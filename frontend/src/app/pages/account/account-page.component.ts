import { Component, computed, effect, inject, Injectable, signal, Signal, WritableSignal } from '@angular/core';
import { AuthenticationService } from '../../services/authentication.service';
import { ActivatedRoute, ActivatedRouteSnapshot, MaybeAsync, RedirectCommand, Resolve, Router, RouterLink, RouterStateSnapshot } from '@angular/router';
import { AccountService, Account } from '@app/services/account.service';
import { BehaviorSubject, EMPTY, Observable, of, switchMap } from 'rxjs';
import { CourseService } from '../../services/course.service';
import { Course } from "@app/services/course.service";


@Component({
  selector: 'app-account-page',
  imports: [
    RouterLink
  ],
  templateUrl: './account-page.component.html',
  styleUrl: './account-page.component.css'
})
export class AccountPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly courseService = inject(CourseService);
  private readonly router = inject(Router);

  account = signal<Account | undefined>(undefined);
  courses = signal<Course[] | undefined>(undefined);

  courseEffect = effect(() => {
    const account = this.account();
    if (!account) return;

    this.courseService.getCourses({ participantId: account.id }, { size: 50 })
      .subscribe({
        next: resp => this.courses.set(resp.content)
      })
  })

  ngOnInit() {
    this.route.data.subscribe({
      next: ({ account }) => this.account.set(account)
    })

  }

  ngOnDestroy() {
    this.courseEffect.destroy();
  }

  navigateToCourse(course: Course) {
    this.router.navigate(['/course', course.id], { state: course });
  }

}

@Injectable({ providedIn: 'root' })
export class AccountResolver implements Resolve<Account> {
  private readonly authService = inject(AuthenticationService);
  private readonly accountService = inject(AccountService);
  private readonly router = inject(Router);

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Account> {
    const idParam = route.paramMap.get("id");
    if (!idParam) {
      const session = this.authService.session;
      if (!session) throw Error("invalid state, expected to be authenticated in the AccountResolver");

      this.router.navigate([`/account/${session.account.id}`]);
      return EMPTY;
    }

    const id = parseInt(idParam, 10);
    if (isNaN(id)) {
      this.router.navigate(['/error']);
      return EMPTY;
    }

    if (this.authService.session && this.authService.session.account.id == id) {
      return of(this.authService.session.account);
    }

    return this.accountService.getAccount(id);
  }

}
