export interface Authority {
    authority: string
}

export default interface SignInResponse {
    token: string;
    tokenExpires: number;
    type: string;
    accountId: number;
    username: string;
    authorities: string[];
}