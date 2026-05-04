import { HttpClient, HttpHeaders } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "@env/environment";
import { Observable } from "rxjs";

export interface RegistrationToken {
    id: number;
    token: string;
    expires: number;
    reusable: boolean;
}

export interface GenerateRegistrationTokenOptions {
    reusable: boolean;
    expires?: number;
}

const httpOptions = {
    headers: new HttpHeaders({
        'Content-Type': 'application/json'
    })
}

@Injectable({ providedIn: 'root' })
export class RegistrationTokenService {
    private readonly http = inject(HttpClient);

    generateToken(options: GenerateRegistrationTokenOptions): Observable<RegistrationToken> {
        return this.http.post<RegistrationToken>(
            `${environment.AUTH_BACKEND_URL}/teacher-token`,
            options,
            httpOptions
        );
    }

}