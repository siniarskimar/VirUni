import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { PageRequest, pageRequestToHttpParams, PageResponse, SortParam } from "../model/page-response.model";
import { Observable } from "rxjs";
import { environment } from "@env/environment";
import { AccountService, Account } from "@app/services/account.service";

const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
}

export interface GetCoursesParams {
    participantId?: number,
    leadingTeacher?: number,
    query?: string,
}

export interface UpdateCourseDetails {
    name?: string,
    leadingTeacher?: number
}

export interface CreateCourseOptions {
    name: string,
    description?: string,
    leadingTeacher: number,
    participants: number[],
}

export interface CoursePermissions {
    canManageGrades: boolean;
    canManageAccounts: boolean;
    canDelete: boolean;
    canUpdate: boolean;
}

export function defaultCoursePermissions(): CoursePermissions {
    return {
        canDelete: false,
        canManageAccounts: false,
        canUpdate: false,
        canManageGrades: false,
    };
}

export interface Course {
    id: number;
    name: string;
    description: string | null;
    leadingTeacher: Account;
    createdAt: string;
    permissions?: CoursePermissions;
}

@Injectable({
    providedIn: 'root'
})
export class CourseService {
    private readonly http = inject(HttpClient);
    private readonly accountService = inject(AccountService);

    createCourse(options: CreateCourseOptions): Observable<Course> {
        return this.http.post<Course>(
            `${environment.AUTH_BACKEND_URL}/subject`,
            options,
            httpOptions
        )
    }

    getCourses(params?: GetCoursesParams, pageOptions?: PageRequest): Observable<PageResponse<Course>> {
        let httpParams = pageOptions ? pageRequestToHttpParams(pageOptions) : new HttpParams();

        if (params) {
            if (params.participantId) httpParams = httpParams.append("participant", params.participantId);
            if (params.leadingTeacher) httpParams = httpParams.append("leadingTeacher", params.leadingTeacher);
            if (params.query) httpParams = httpParams.append("query", params.query);
        }

        return this.http.get<PageResponse<Course>>(
            `${environment.AUTH_BACKEND_URL}/subject`,
            { ...httpOptions, params: httpParams });
    }

    getCourse(id: number): Observable<Course> {
        return this.http.get<Course>(
            `${environment.AUTH_BACKEND_URL}/subject/${id}`,
            httpOptions
        );
    }

    deleteCourse(id: number): Observable<any> {
        return this.http.delete(
            `${environment.AUTH_BACKEND_URL}/subject/${id}`,
            httpOptions
        );
    }

    updateCourse(id: number, details: UpdateCourseDetails): Observable<Course> {
        return this.http.patch<Course>(
            `${environment.AUTH_BACKEND_URL}/subject/${id}`,
            details,
            httpOptions
        )
    }

    getParticipants(id: number, pageOptions?: PageRequest): Observable<PageResponse<Account>> {
        return this.accountService.getAccounts({
            subjectId: id
        }, pageOptions);
    }

    addParticipant(id: number, accountId: number): Observable<void> {
        return this.http.post<void>(
            `${environment.AUTH_BACKEND_URL}/subject/${id}/account`,
            { account: accountId },
            httpOptions
        )
    }

    deleteParticipant(id: number, accountId: number): Observable<void> {
        return this.http.delete<void>(
            `${environment.AUTH_BACKEND_URL}/subject/${id}/account/${accountId}`,
            httpOptions
        )
    }
};