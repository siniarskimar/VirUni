import { Component, computed, inject, input, model, OnInit, output, signal } from "@angular/core";
import { PageRequest, PageResponse } from "@app/model/page-response.model";
import { Account } from "@app/services/account.service";
import { Course, CourseService } from "@app/services/course.service";
import { CourseCardComponent } from "../course-card/course-card.component";

@Component({
    selector: 'app-course-pager',
    templateUrl: './course-pager.component.html',
    styleUrl: './course-pager.component.css',
    imports: [CourseCardComponent]
})
export class CoursePagerComponent {
    private readonly courseService = inject(CourseService);

    currentPage = 0;
    participant = input<Account>();
    coursePage = signal<PageResponse<Course> | undefined>(undefined);
    courses = model<Course[]>([]);
    hasMore = computed(() => {
        const courses = this.coursePage()
        if (!courses) return false;

        return !courses.last;
    });

    courseClicked = output<Course>();

    ngOnInit() {
        this.fetchCourses()
    }

    fetchCourses() {
        this.courseService.getCourses(
            { participantId: this.participant()?.id },
            { size: 10, page: this.currentPage }).subscribe({
                next: (resp) => {
                    this.coursePage.set(resp)
                    this.courses.update((old) => [...old, ...resp.content]);
                    this.currentPage += 1
                }
            })

    }
}