import { HttpClient, HttpEvent, HttpHandler, HttpHandlerFn, HttpHeaders, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { environment } from '../../environments/environment';
import { BehaviorSubject, catchError, concatMap, EMPTY, map, Observable, of, tap, throwError } from 'rxjs';
import { AccountService, Account } from './account.service';
import { ActivatedRoute, ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from '@angular/router';
import SignInResponse, { Authority } from '../model/signin-response.model';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
}

const localStorageKey = 'SESSION';

type AccountRole = 'ROLE_USER' | 'ROLE_TEACHER';

export interface SignUpForm {
  username: string,
  password: string,
  firstname: string,
  lastname: string,
  role?: AccountRole,
  teacherToken?: string,
}

export interface SignInForm {
  username: string,
  password: string,
}

export interface SessionDetails {
  token: string;
  tokenExpires: number;
  accountId: number;
  authorities: string[];
}

export class Session {
  constructor(public details: SessionDetails, public account: Account) {
  }

  hasAuthority(authority: string): boolean {
    return !!this.details.authorities.find(v => v === authority);
  }

  isAdmin(): boolean {
    return this.hasAuthority("ROLE_ADMIN");
  }

  isTeacher(): boolean {
    return this.isAdmin() || this.hasAuthority("ROLE_TEACHER");
  }

  isUser(): boolean {
    return this.isTeacher() || this.hasAuthority("ROLE_USER");
  }

  canBrowseCourses(): boolean {
    return this.hasAuthority("BROWSE_SUBJECTS");
  }

  canManageSubjects(): boolean {
    return this.hasAuthority("SUBJECT_MANAGEMENT");
  }

  canManageGrades(): boolean {
    return this.hasAuthority("GRADE_MANAGEMENT");
  }

  canCreateRegistrationTokens(): boolean {
    return this.hasAuthority("CREATE_TEACHER_TOKENS");
  }
}

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  private readonly http = inject(HttpClient);
  private readonly accountService = inject(AccountService);

  session$ = new BehaviorSubject<Session | undefined>(undefined);

  sessionDetails: SessionDetails | undefined = undefined;

  restoreFromLocalstorage(): Observable<Session> {
    const savedSessionDetails = this.getLocalStorageSessionDetails();
    if (!savedSessionDetails) {
      this.session$.next(undefined);
      return EMPTY;
    }

    if (isSessionExpired(savedSessionDetails)) {
      this.session$.next(undefined);
      this.signOut();
      return throwError(() => new Error("session expired"));
    }

    const sessionObservable = sessionAboutToExpire(savedSessionDetails) ?
      this.renewToken(savedSessionDetails).pipe(map((resp) => this.sessionDetailsFromSignin(resp)))
      : of(savedSessionDetails)

    return sessionObservable.pipe(
      concatMap((sessionDetails) => {
        this.signOut();
        this.sessionDetails = sessionDetails;

        return this.accountService.getAccount(sessionDetails.accountId).pipe(
          map(acc => new Session(sessionDetails, acc))
        )
      }),
      tap({
        next: (session) => {
          this.session$.next(session);
          localStorage.setItem(localStorageKey, JSON.stringify(session.details));
        },
        error: (err) => {
          this.sessionDetails = undefined;
          console.warn(`failed to restore session:`, err);
        }
      })
    );
  }

  getLocalStorageSessionDetails(): SessionDetails | undefined {
    const details = localStorage.getItem(localStorageKey);
    if (!details) return undefined;

    return JSON.parse(details) as SessionDetails;
  }

  signIn(form: SignInForm): Observable<Session> {
    return this.http.post<SignInResponse>(
      `${environment.AUTH_BACKEND_URL}/signin`,
      form,
      httpOptions
    ).pipe(
      concatMap((resp) => {
        this.signOut();
        const details: SessionDetails = this.sessionDetailsFromSignin(resp);
        this.sessionDetails = details;

        return this.accountService.getAccount(resp.accountId).pipe(
          map(acc => new Session(details, acc))
        )
      }),
      tap({
        next: (session) => {
          localStorage.setItem(localStorageKey, JSON.stringify(session.details))
          this.session$.next(session)
        },
        error: () => this.sessionDetails = undefined
      })
    );
  }

  signUp(
    form: SignUpForm
  ): Observable<Session> {
    return this.http.post<void>(
      environment.AUTH_BACKEND_URL + '/signup',
      form,
      httpOptions
    ).pipe(
      concatMap(_ => {
        return this.signIn({ username: form.username, password: form.password });
      })
    );
  }

  signOut() {
    this.session$.next(undefined);
    localStorage.removeItem(localStorageKey);
  }

  get session(): Session | undefined {
    return this.session$.value;
  }

  isAuthenticated() {
    return !!this.session;
  }

  canManageCourses() {
    return this.isAuthenticated() && this.session?.hasAuthority("SUBJECT_MANAGEMENT");
  }

  private renewToken(sessionDetails: SessionDetails): Observable<SignInResponse> {
    const headers = httpOptions.headers.append("Authorization", `Bearer ${sessionDetails.token}`);

    return this.http.post<SignInResponse>(
      `${environment.AUTH_BACKEND_URL}/auth/renew`,
      null,
      { ...httpOptions, headers }
    )
  }

  private sessionDetailsFromSignin(signin: SignInResponse): SessionDetails {
    return {
      token: signin.token,
      tokenExpires: signin.tokenExpires,
      accountId: signin.accountId,
      authorities: signin.authorities,
    };
  }
}

const apiHostname = URL.parse(environment.AUTH_BACKEND_URL)?.hostname;

export function authInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> {
  const url = URL.parse(req.url);
  if (url?.hostname !== apiHostname) {
    return next(req);
  }
  const auth = inject(AuthenticationService);

  const session = auth.session;
  const sessionDetails = session?.details || auth.sessionDetails;
  if (!sessionDetails) {
    return next(req);
  }

  if (sessionDetails.token && !req.headers.get("Authorization")) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${sessionDetails.token}`
      }
    });
  }

  return next(req);
}

function isSessionExpired(details: SessionDetails): boolean {
  return (new Date(details.tokenExpires * 1000)) < (new Date());
}

function sessionAboutToExpire(sessionDetails: SessionDetails): boolean {
  const expiration = new Date(sessionDetails.tokenExpires * 1000);
  const now = new Date();

  const timeLeft = expiration.getTime() - now.getTime();

  return timeLeft < 10 * 1000;
}

export function authenticationGuard(navigateBack?: boolean): CanActivateFn {
  return (next: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
    const authService = inject(AuthenticationService);
    const router = inject(Router);

    if (!authService.session) {
      if (navigateBack) {
        router.navigate(['/auth'], { queryParams: { goto: state.url } });
      }

      return false;
    }
    return true;
  }
}
