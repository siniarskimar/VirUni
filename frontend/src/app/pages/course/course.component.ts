import { Component, computed, effect, inject, OnDestroy, OnInit, signal, WritableSignal } from "@angular/core";
import { ActivatedRoute, ActivatedRouteSnapshot, Router, RouterStateSnapshot } from "@angular/router";
import { catchError, concat, concatMap, EMPTY, map, mergeAll, Observable, of, Subject, tap, throwError } from "rxjs";
import { CourseService, Course, defaultCoursePermissions } from "../../services/course.service";
import { CourseParticipantsView } from "./participants-view/participants-view.component";
import { AccountSearchboxComponent } from "../../components/account-searchbox/account-searchbox.component";
import { EditableShorttextComponent } from "../../components/editable-shorttext/editable-shorttext.component";
import { ModalRef, ModalService } from "../../services/modal/modal.service";
import { AssignGradeModalComponent, AssignGradeModalReturnData } from "./participants-view/assign-grade-modal/assign-grade-modal.component";
import { AddParticipantsModalComponent, AddParticipantsModalReturn } from "../../components/add-participants-modal/add-participants-modal.component";
import { AuthenticationService } from "../../services/authentication.service";
import { Grade, GradeService } from "../../services/grade.service";
import { PageResponse } from "../../model/page-response.model";
import { Account } from "@app/services/account.service";
import { CourseGradeList } from "./participants-view/grade-list.component";
import { toSignal } from "@angular/core/rxjs-interop";
import { GradeModalComponent } from "@app/components/grade-modal/grade-modal.component";

@Component({
    selector: 'app-course-page',
    imports: [
        CourseParticipantsView,
        EditableShorttextComponent,
        CourseGradeList
    ],
    templateUrl: './course.component.html',
    styleUrl: './course.component.css'
})
export class CoursePageComponent implements OnInit, OnDestroy {
    private readonly courseService = inject(CourseService);
    private readonly route = inject(ActivatedRoute);
    private readonly modalService = inject(ModalService);
    private readonly authService = inject(AuthenticationService);
    private readonly gradeService = inject(GradeService);
    private readonly router = inject(Router);

    private readonly dateTimeFormatter = new Intl.DateTimeFormat(this.getLocale(), {
        year: "numeric",
        month: "numeric",
        day: "numeric",
    });

    private _assignGradeModalRef: ModalRef<AssignGradeModalComponent, AssignGradeModalReturnData> | null = null;
    private _addParticipantsModalRef: ModalRef<AddParticipantsModalComponent, AddParticipantsModalReturn> | null = null;
    private _gradeModalRef: ModalRef<GradeModalComponent, Grade> | null = null;
    private _refreshTrigger = new Subject<void>();

    refreshTrigger$ = this._refreshTrigger.asObservable();

    course = signal<Course | undefined>(undefined);

    participants = signal<PageResponse<Account> | undefined>(undefined);
    activeSession = toSignal(this.authService.session$);
    sessionAccount = computed(() => this.activeSession()?.account);

    creationDate = computed(() => {
        const course = this.course();
        if (!course) return undefined;
        return new Date(course.createdAt);
    })

    creationDateLocalized = computed(() => {
        const creationDate = this.creationDate();
        if (!creationDate) return undefined;

        return this.dateTimeFormatter.format(creationDate);
    })

    coursePermissions = computed(() => this.course()?.permissions || defaultCoursePermissions());

    ngOnInit() {
        this.route.data.subscribe({
            next: ({ course }) => this.course.set(course),
            error: (err) => {
                console.error(err);
            }
        })
    }

    ngOnDestroy(): void {
        this._assignGradeModalRef?.close();
        this._addParticipantsModalRef?.close();
        this._gradeModalRef?.close();
    }

    getLocale() {
        return (navigator.languages && navigator.languages.length) ? navigator.languages[0] : navigator.language;
    }

    updateCourseName(newName: string) {
        const course = this.course();
        if (!course) return;

        this.courseService.updateCourse(course.id, { name: newName }).subscribe({
            next: (updated) => this.course.set(updated)
        });
    }

    openAssignGradesModal() {
        const course = this.course();
        const sessionAccount = this.authService.session?.account;
        if (!course || !sessionAccount) return;

        this._assignGradeModalRef = this.modalService.open(AssignGradeModalComponent, { course })
        this._assignGradeModalRef.afterClosed.pipe(
            concatMap((v) => v ? of(v) : EMPTY),
            concatMap((v) => v.accounts.map((acc) => this.gradeService.assignGrade({
                subject: course.id,
                student: acc.id,
                teacher: sessionAccount.id,
                value: v.value
            }))),
            mergeAll(1)
        ).subscribe({
            error: (err) => console.error("failed to assign grade", err),
            complete: () => {
                this._refreshTrigger.next();
                this._assignGradeModalRef = null;
            }
        })
    }

    openAddParticipantModal() {
        const course = this.course();
        if (!course) return;

        this._addParticipantsModalRef = this.modalService.open(AddParticipantsModalComponent, { course });
        this._addParticipantsModalRef.afterClosed.pipe(
            concatMap((v) => v ? of(v.accounts) : EMPTY),
            concatMap((accs) => accs.map((acc) => this.courseService.addParticipant(course.id, acc.id))),
            mergeAll(1)
        ).subscribe({
            complete: () => {
                this._refreshTrigger.next();
                this._addParticipantsModalRef = null;
            },
            error: (err) => console.error("failed to add account as participant", err)
        });
    }

    onParticipantDeleteRequest(account: Account) {
        const course = this.course();
        if (!course) return;

        this.courseService.deleteParticipant(course.id, account.id)
            .subscribe({
                next: () => this._refreshTrigger.next()
            })
    }

    onGradeClick(grade: Grade) {
        console.log("hmm");
        this._gradeModalRef = this.modalService.open(GradeModalComponent, { grade });
        this._gradeModalRef.afterClosed.subscribe({
            complete: () => this._gradeModalRef = null
        });
    }

    onDeleteRequest() {
        const course = this.course();
        if (!course) return;

        this.courseService.deleteCourse(course.id).subscribe({
            next: () => this.router.navigate(['/']),
            error: (err) => console.error("failed to delete course", err)
        })
    }
};

export function coursePageResolver(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Course> {
    const courseService = inject(CourseService);
    const idParam = route.paramMap.get("id");
    const router = inject(Router);

    if (!idParam) return throwError(() => new Error("Expected parameter 'id' to be present on the route"));
    const id = parseInt(idParam, 10);

    if (isNaN(id)) {
        console.error("Course 'id' must be an integer");
        router.navigate(['/404'], { skipLocationChange: true, replaceUrl: true });
        return EMPTY;
    }

    return courseService.getCourse(id).pipe(
        catchError((err, caught) => {
            if (err.status === 404) {
                router.navigate(['/404'], { skipLocationChange: true, replaceUrl: true });
                return EMPTY;
            }
            return caught;
        })
    );
}