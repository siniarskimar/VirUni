import { Component, computed, effect, inject, input, signal } from "@angular/core";
import { AuthenticationService } from "@app/services/authentication.service";
import { Grade, GradeService, UpdateGradeOptions } from "@app/services/grade.service";
import { ModalRef } from "@app/services/modal/modal.service";

@Component({
    selector: 'app-grade-modal',
    templateUrl: './grade-modal.component.html',
    imports: [
    ]
})
export class GradeModalComponent {
    private readonly modalRef = inject(ModalRef<GradeModalComponent, Grade>);

    private readonly authService = inject(AuthenticationService);
    private readonly gradeService = inject(GradeService);

    readonly grade = input<Grade>();
    value = signal<number>(0);

    editable = !!this.authService.session?.isTeacher();

    constructor() {
        effect(() => {
            const grade = this.grade();
            if (grade) {
                this.value.set(grade.value);
            }
        })
    }

    updateGrade() {
        const grade = this.grade();
        const value = this.value();

        if (!this.editable || !grade || value === grade.value) {
            this.modalRef.close();
            return;
        }

        this.gradeService.updateGrade(grade.id, { value }).subscribe({
            next: (grade) => this.modalRef.close(grade)
        })
    }
}