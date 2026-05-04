import { HttpParams } from "@angular/common/http";

export interface SortParam {
    field: string;
    order: 'asc' | 'desc';
};

export interface PageRequest {
    sort?: SortParam;
    size?: number;
    page?: number;
}

export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    last: boolean;
}

export function pageRequestToHttpParams(request: PageRequest): HttpParams {
    let params = new HttpParams();
    if (request.sort) params = params.append("sort", `${request.sort.field},${request.sort.order}`);
    if (request.size) params = params.append("size", request.size);
    if (request.page) params = params.append("page", request.page);

    return params;
}