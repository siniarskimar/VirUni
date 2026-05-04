import { Component, computed, effect, inject, input, OnDestroy, output, signal } from "@angular/core";
import { AccountService, Account } from "@app/services/account.service";
import { debounceTime, EMPTY, Subject, switchMap, takeUntil } from "rxjs";
import { toObservable } from "@angular/core/rxjs-interop";
import { SearchboxComponent } from "../searchbox/searchbox.component";
import { Course } from "@app/services/course.service";

@Component({
    selector: 'app-account-searchbox',
    templateUrl: './account-searchbox.component.html',
    styleUrl: './account-searchbox.component.css',
    imports: [
        SearchboxComponent
    ]
})
export class AccountSearchboxComponent implements OnDestroy {
    private readonly accountService = inject(AccountService);
    private destroy$ = new Subject<void>();

    showUsername = input<boolean>(false);
    showFullname = input<boolean>(true);
    resultCount = input<number>(5);
    course = input<Course>();
    placeholder = input<string>();

    query = signal<string>('');
    results = signal<Account[]>([]);

    clickResult = output<Account>();
    inputFocused = signal<boolean>(false);
    mouseHoverResults = signal<boolean>(false);
    resultsVisible = computed(() => this.inputFocused() || this.mouseHoverResults());

    query$ = toObservable(this.query);

    constructor() {
        this.query$.pipe(
            debounceTime(500),
            takeUntil(this.destroy$),
            switchMap((query) => query === "" ? EMPTY : this.accountService.getAccounts({
                query,
                subjectId: this.course()?.id
            }, {
                size: this.resultCount(),
                page: 0
            }))
        ).subscribe({
            next: (resp) => {
                this.results.set(resp.content);
            },
            error: (err) => console.error("failed to query accounts from searchbox", err)
        })
    }

    onResultClicked(account: Account) {
        this.clickResult.emit(account);
    }

    ngOnDestroy(): void {
        this.destroy$.next();
    }
}