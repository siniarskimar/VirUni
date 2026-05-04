import { Component, input, output } from "@angular/core";
import { Account } from "@app/services/account.service";

@Component({
    selector: 'app-account-pill',
    template: `<span class="tag is-small" (click)="emitClick()">
        @if(account(); as account) {
            {{account.firstname}} {{account.lastname}}
        }
    </span>`
})
export class AccountPillComponent {
    account = input<Account>();
    click = output<Account>();

    emitClick() {
        const acc = this.account();
        if (!acc) return;

        this.click.emit(acc);
    }

}