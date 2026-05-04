import { inject, Injectable, signal } from "@angular/core";
import { Observable, Subject } from "rxjs";
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { environment } from "../../environments/environment";
import { PageRequest, pageRequestToHttpParams, PageResponse } from "../model/page-response.model";

export interface Account {
    id: number;
    username: string;
    firstname: string;
    lastname: string;
}

export interface GetAccountsParams {
    query?: string,
    role?: string,
    subjectId?: number,
}

export interface UpdateAccountOptions {
    firstname?: string,
    lastname?: string,
}

const httpOptions = {
    headers: new HttpHeaders({
        'Content-Type': 'application/json'
    })
};

@Injectable({
    providedIn: 'root'
})
export class AccountService {
    private readonly httpClient = inject(HttpClient);

    getAccounts(params?: GetAccountsParams, pageOptions?: PageRequest): Observable<PageResponse<Account>> {
        let httpParams = pageOptions ? pageRequestToHttpParams(pageOptions) : new HttpParams();

        if (params) {
            if (params.role) httpParams = httpParams.set("role", params.role);
            if (params.query) httpParams = httpParams.set("query", params.query);
            if (params.subjectId) httpParams = httpParams.set("subjectId", params.subjectId);
        }


        return this.httpClient.get<PageResponse<Account>>(
            `${environment.AUTH_BACKEND_URL}/account`,
            { ...httpOptions, params: httpParams }
        )
    }

    getAccount(identifier: number): Observable<Account> {
        if (typeof identifier === 'number' && identifier < 0)
            throw new Error(`'identifier' must be bigger than 0`);

        return this.httpClient.get<Account>(
            `${environment.AUTH_BACKEND_URL}/account/${identifier}`,
            httpOptions
        );
    }

    deleteAccount(identifier: number): Observable<any> {
        if (identifier < 0)
            throw new Error(`'identifier' must be bigger than 0`);

        return this.httpClient.delete(
            `${environment.AUTH_BACKEND_URL}/account/${identifier}`,
            httpOptions
        )
    }

    updateAccount(id: number, updates: UpdateAccountOptions): Observable<Account> {
        return this.httpClient.patch<Account>(
            `${environment.AUTH_BACKEND_URL}/account/${id}`,
            updates,
            httpOptions
        )
    }

}