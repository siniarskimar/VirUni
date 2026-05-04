import { Component, inject, input, OnInit, signal } from "@angular/core";
import { Account } from "@app/services/account.service";
import { AccountSearchboxComponent } from "../account-searchbox/account-searchbox.component";
import { AccountPillComponent } from "../account-pill.component";
import { ModalRef } from "../../services/modal/modal.service";
import { Course } from "@app/services/course.service";

export interface AddParticipantsModalReturn {
    accounts: Account[]
}

@Component({
    selector: 'app-assign-grade-modal',
    templateUrl: './add-participants-modal.component.html',
    imports: [
        AccountSearchboxComponent,
        AccountPillComponent,
    ]
})
export class AddParticipantsModalComponent {
    private readonly modelRef = inject(ModalRef<AddParticipantsModalComponent, AddParticipantsModalReturn>);

    course = input<Course>();

    accounts = signal<Account[]>([]);
    value = signal<number>(2);

    addAccount(account: Account) {
        this.accounts.update((old) => {
            const duplicate = old.find((v) => v.id === account.id);
            if (!!duplicate) return old;

            return [...old, account];
        });
    }

    confirm() {
        this.modelRef.close({
            accounts: this.accounts()
        });
    }

    removeAccount(account: Account) {
        this.accounts.update((old) => {
            const idx = old.findIndex((v) => v.id == account.id);
            if (idx == -1) return old;

            return old.splice(idx, 1);
        })
    }
}