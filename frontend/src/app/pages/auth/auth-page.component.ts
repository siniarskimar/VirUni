import { Component, inject, Injectable, OnInit, signal, WritableSignal } from "@angular/core";
import { ActivatedRoute, ActivatedRouteSnapshot, Resolve, Router, RouterStateSnapshot } from "@angular/router";
import { Account } from "@app/services/account.service";
import { SignUpViewComponent } from "./sign-up-view/sign-up-view.component";
import { SignInViewComponent } from "./sign-in-view/sign-in-view.component";

@Component({
    selector: 'app-auth-page',
    imports: [
        SignUpViewComponent,
        SignInViewComponent,
    ],
    templateUrl: './auth-page.component.html',
    styleUrl: './auth-page.component.html'
})
export class AuthPageComponent implements OnInit {
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);

    authOption: 'signin' | 'signup' = 'signin';
    private goto: WritableSignal<string> = signal('/');

    authenticationError: WritableSignal<Error | undefined> = signal(undefined);

    ngOnInit() {
        this.route.data.subscribe({
            next: ({ goto }) => {
                this.goto.set(goto);
            }
        })
    }

    onAuthentication(acc: Account) {
        this.router.navigate([this.goto()]);
    }

    onChangeView(elem: Event) {
        const target = elem.target as HTMLInputElement;
        if (target.value !== "signin" && target.value !== "signup") return;

        this.authOption = target.value;
    }
}


export function authPageGotoResolver(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): string {
    const gotoParam = state.root.queryParamMap.get('goto');
    return (gotoParam) ? decodeURI(gotoParam) : '/';
}
