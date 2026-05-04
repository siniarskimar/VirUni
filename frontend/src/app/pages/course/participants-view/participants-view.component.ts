import { Component, computed, effect, inject, input, model, OnDestroy, OnInit, output, signal, WritableSignal } from "@angular/core";
import { PageResponse } from "../../../model/page-response.model";
import { Account } from "@app/services/account.service";
import { AccountService } from "../../../services/account.service";
import { concatMap, debounceTime, EMPTY, map, mergeAll, Observable, of, Subject, switchMap, takeUntil, tap } from "rxjs";
import { Course } from "@app/services/course.service";
import { SearchboxComponent } from "../../../components/searchbox/searchbox.component";
import { AuthenticationService } from "../../../services/authentication.service";
import { CourseGradeList } from "./grade-list.component";
import { toObservable } from "@angular/core/rxjs-interop";
import { Grade } from "@app/services/grade.service";

@Component({
    selector: 'app-course-participants-view',
    imports: [
        SearchboxComponent,
        CourseGradeList,
    ],
    templateUrl: './participants-view.component.html',
    styleUrl: './participants-view.component.css'
})
export class CourseParticipantsView implements OnInit {
    private readonly accountService = inject(AccountService);
    private readonly authService = inject(AuthenticationService);

    readonly course = input.required<Course>();
    pageSize = signal(20);
    currentPage = signal(0);
    participants = model<PageResponse<Account>>();

    readonly refreshTrigger = input.required<Observable<void>>();

    deleted = output<Account>();
    gradeClicked = output<Grade>();

    query = signal<string>("");
    query$ = toObservable(this.query).pipe(debounceTime(500));

    constructor() {
        this.query$.pipe(
            switchMap((q) => {
                const course = this.course();
                if (!course) return EMPTY;

                return of({ q, course });
            })
        ).subscribe({
            next: ({ q, course }) => this.refreshParticipants(
                course,
                this.pageSize(),
                this.currentPage(),
                q.length !== 0 ? q : undefined
            )
        })
    }

    ngOnInit(): void {
        this.refreshTrigger().pipe(
            concatMap(() => {
                const course = this.course();
                const query = this.query();
                const q = query.length !== 0 ? query : undefined;

                if (!course) return EMPTY;

                return of({ query: q, course })
            })
        ).subscribe(({ query, course }) => this.refreshParticipants(course, this.pageSize(), this.currentPage(), query))
    }

    isSessionAccount(account: Account): boolean {
        const session = this.authService.session;
        if (!session) return false;

        return account.id == session.account.id;
    }

    removeParticipant(account: Account) {
        if (this.isSessionAccount(account)) {
            console.error("cannot remove urself from course")
            return;
        }
        this.deleted.emit(account);
    }

    refreshParticipants(course: Course, pageSize: number, page: number, query?: string) {
        this.accountService.getAccounts({
            subjectId: course.id,
            query
        }, {
            size: pageSize,
            page: page,
            sort: { field: 'lastname', order: 'asc' }
        }).subscribe({
            next: (resp) => {
                resp.content = resp.content.filter((v) => v.id != this.authService.sessionDetails?.accountId || true)
                this.participants.set(resp);
                this.currentPage.set(resp.page);
            },
            error: (err) => console.error("failed to fetch participants", err)
        })
    }

    gotoPrevPage() {
        this.currentPage.update((old) => Math.max(0, old - 1))
    }

    gotoNextPage() {
        this.currentPage.update((old) => Math.min(this.lastPage(), old + 1))
    }

    lastPage() {
        const participants = this.participants();
        if (!participants) return 1;

        return Math.floor(participants.totalElements / participants.size);
    }
}