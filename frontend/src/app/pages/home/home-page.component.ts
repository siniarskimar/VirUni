import { Component, computed, inject, signal } from "@angular/core";
import { AuthenticationService } from "../../services/authentication.service";
import { toSignal } from "@angular/core/rxjs-interop";
import { Course, CourseService } from "@app/services/course.service";
import { PageResponse } from "@app/model/page-response.model";
import { CoursePagerComponent } from "@app/components/course-pager/course-pager.component";
import { Router } from "@angular/router";
import { ModalRef, ModalService } from "@app/services/modal/modal.service";
import { CreateCourseModalComponent } from "@app/components/create-course-modal/create-course-modal.component";
import { concatMap, EMPTY, of } from "rxjs";

@Component({
    selector: 'app-home-page',
    imports: [
        CoursePagerComponent,
    ],
    templateUrl: './home-page.component.html',
    styleUrl: './home-page.component.css'
})
export class HomePageComponent {
    private readonly modalService = inject(ModalService);
    private readonly authService = inject(AuthenticationService);
    private readonly router = inject(Router);

    accountSession = toSignal(this.authService.session$);
    courses = signal<Course[]>([]);

    canCreateSubjects = computed(() => {
        return this.accountSession()?.canManageSubjects() || false;
    })

    private _createCourseModalRef: ModalRef<CreateCourseModalComponent, Course> | null = null;

    navigateToCourse(course: Course) {
        this.router.navigate(['/course', course.id], { state: course });
    }

    openCreateCourseModal() {
        this._createCourseModalRef = this.modalService.open(CreateCourseModalComponent);
        this._createCourseModalRef.afterClosed.pipe(
            concatMap((v) => v ? of(v) : EMPTY)
        ).subscribe({
            next: (course) => this.router.navigate(['/course', course.id], { state: course })
        })
    }

    ngOnDestroy() {
        this._createCourseModalRef?.close();
    }
}