import { Component, effect, inject, input, OnInit, signal } from "@angular/core";
import { Course } from "@app/services/course.service";
import { AccountSearchboxComponent } from "../../../../components/account-searchbox/account-searchbox.component";
import { Account } from "@app/services/account.service";
import { AccountPillComponent } from "../../../../components/account-pill.component";
import { ModalRef } from "../../../../services/modal/modal.service";

export interface AssignGradeModalReturnData {
    value: number;
    accounts: Account[]
}

@Component({
    selector: 'app-assign-grade-modal',
    templateUrl: './assign-grade-modal.component.html',
    imports: [
        AccountSearchboxComponent,
        AccountPillComponent,
    ]
})
export class AssignGradeModalComponent {
    private readonly modelRef = inject(ModalRef<AssignGradeModalComponent, AssignGradeModalReturnData>);
    course = input<Course>();

    accounts = signal<Account[]>([]);
    value = signal<number>(2);

    addAccount(account: Account) {
        console.log(account);
        this.accounts.update((old) => {
            const duplicate = old.find((v) => v.id === account.id);
            if (!!duplicate) return old;

            return [...old, account];
        });
    }

    confirm() {
        this.modelRef.close({
            value: this.value(),
            accounts: this.accounts()
        });
    }
}