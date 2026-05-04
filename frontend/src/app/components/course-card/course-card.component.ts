import { Component, computed, input, output } from "@angular/core";
import { Course } from "@app/services/course.service";

@Component({
    selector: 'app-course-card',
    templateUrl: './course-card.component.html',
    styleUrl: './course-card.component.css'
})
export class CourseCardComponent {

    course = input.required<Course>();

    teacherFullname = computed(() => {
        return `${this.course().leadingTeacher.firstname} ${this.course().leadingTeacher.lastname}`;
    })

    clicked = output<Course>();

    onClick() {
        this.clicked.emit(this.course());
    }
}