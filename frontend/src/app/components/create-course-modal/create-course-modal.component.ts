import { Component, inject, signal } from "@angular/core";
import { toSignal } from "@angular/core/rxjs-interop";
import { AuthenticationService } from "@app/services/authentication.service";
import { Course, CourseService } from "@app/services/course.service";
import { ModalRef } from "@app/services/modal/modal.service";
import { map } from "rxjs";

@Component({
    selector: 'app-create-course-modal',
    templateUrl: './create-course-modal.component.html'
})
export class CreateCourseModalComponent {
    private readonly modalRef = inject(ModalRef<CreateCourseModalComponent, Course>);
    private readonly authService = inject(AuthenticationService);
    private readonly courseService = inject(CourseService);

    name = signal<string>("");
    description = signal<string>("");
    account = toSignal(this.authService.session$.pipe(map((s) => s?.account)))

    onCreate() {
        const account = this.account()
        if (!account) return;

        this.courseService.createCourse({
            name: this.name(),
            description: this.description(),
            leadingTeacher: account.id,
            participants: []
        }).subscribe({
            next: (course) => this.modalRef.close(course)
        })
    }
}