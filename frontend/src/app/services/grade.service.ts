import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PageRequest, pageRequestToHttpParams, PageResponse, SortParam } from "../model/page-response.model";
import { Course } from "@app/services/course.service";
import { Account } from "@app/services/account.service";

const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
}

export interface Grade {
    id: number;
    subject: Course;
    student: Account;
    teacher: Account;
    value: number;
    creation: string | Date;
}

export interface GetGradesOptions {
    student?: number,
    teacher?: number,
    subject?: number,
};

export interface UpdateGradeOptions {
    value?: number,
}

export interface AssignGrade {
    value: number;
    student: number;
    teacher: number;
    subject: number;
}

@Injectable({ providedIn: 'root' })
export class GradeService {
    private readonly http = inject(HttpClient);

    getGrades(options?: GetGradesOptions, pageOptions?: PageRequest): Observable<PageResponse<Grade>> {
        let params = pageOptions ? pageRequestToHttpParams(pageOptions) : new HttpParams();

        if (options) {
            if (options.student) params = params.set("student", options.student);
            if (options.subject) params = params.set("subject", options.subject);
            if (options.teacher) params = params.set("teacher", options.teacher);
        }

        return this.http.get<PageResponse<Grade>>(
            `${environment.AUTH_BACKEND_URL}/grade`,
            { ...httpOptions, params }
        );
    }

    getGrade(id: number): Observable<Grade> {
        return this.http.get<Grade>(
            `${environment.AUTH_BACKEND_URL}/grade/${id}`,
            httpOptions
        );
    }

    deleteGrade(id: number): Observable<any> {
        return this.http.delete(
            `${environment.AUTH_BACKEND_URL}/grade/${id}`,
            httpOptions
        );
    }

    updateGrade(id: number, updates: UpdateGradeOptions): Observable<Grade> {
        return this.http.patch<Grade>(
            `${environment.AUTH_BACKEND_URL}/grade/${id}`,
            updates,
            httpOptions
        );
    }

    assignGrade(form: AssignGrade): Observable<Grade> {
        return this.http.post<Grade>(
            `${environment.AUTH_BACKEND_URL}/grade`,
            form,
            httpOptions
        )
    }

}