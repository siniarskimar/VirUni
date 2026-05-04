import { Component, effect, inject, input, model, output, signal } from "@angular/core";
import { Account } from "@app/services/account.service";
import { Grade } from "@app/services/grade.service";
import { GradeService } from "@app/services/grade.service";
import { Course } from "@app/services/course.service";
import { GradeBoxComponent } from "@app/components/grade-box/grade-box.component";
import { ModalService } from "@app/services/modal/modal.service";

@Component({
    selector: 'app-course-grade-list',
    template: `
    @if(grades().length !== 0) {
        @for(grade of grades(); track grade.id) {
            <app-grade-box [id]="grade.id" [value]="grade.value" (click)="this.gradeClick.emit(grade)"></app-grade-box>
        }
    } @else {
        <ng-content></ng-content>
    }
    `,
    imports: [GradeBoxComponent]
})
export class CourseGradeList {
    private readonly gradeService = inject(GradeService);
    private readonly modalService = inject(ModalService);

    account = input<Account>();
    course = input<Course>();

    grades = model<Grade[]>([]);

    gradeClick = output<Grade>();

    fetchGradesEffect = effect(() => {
        const acc = this.account();
        const course = this.course();

        if (!acc || !course) { this.grades.set([]); return; }

        this.gradeService.getGrades({ subject: course.id, student: acc.id }).subscribe({
            next: (resp) => this.grades.set(resp.content)
        })
    });
}