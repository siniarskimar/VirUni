import { Component, computed, inject, OnInit, signal, Signal, WritableSignal } from '@angular/core';
import { CourseService } from '../../services/course.service';
import { Observable, Subscription } from 'rxjs';
import { PageResponse } from '../../model/page-response.model';
import { Course } from "@app/services/course.service";
import { toSignal } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { AuthenticationService } from '../../services/authentication.service';
import { CourseCardComponent } from '../../components/course-card/course-card.component';
import { CoursePagerComponent } from '@app/components/course-pager/course-pager.component';

@Component({
  selector: 'app-course-list',
  imports: [
    CoursePagerComponent
  ],
  templateUrl: './course-list.component.html',
  styleUrl: './course-list.component.css'
})
export class CourseListPageComponent {
  private readonly courseService = inject(CourseService);
  private readonly authService = inject(AuthenticationService);
  private readonly router = inject(Router);

  accountSession = toSignal(this.authService.session$);
  courseList: WritableSignal<PageResponse<Course> | undefined> = signal(undefined);
  courses = signal<Course[]>([]);


  onCourseCardClick(course: Course) {
    this.router.navigate(['/course', course.id], { state: course });
  }

}
