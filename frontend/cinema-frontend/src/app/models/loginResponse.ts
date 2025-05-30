/**
 * Cinema Service API
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


/**
 * Odpowiedź po logowaniu: access i refresh tokeny
 */
export interface LoginResponse { 
    /**
     * JWT dostępowy
     */
    accessToken?: string;
    /**
     * Refresh token
     */
    refreshToken?: string;
    /**
     * Typ tokenu dostępowego
     */
    tokenType?: LoginResponse.TokenTypeEnum;
}
export namespace LoginResponse {
    export const TokenTypeEnum = {
        Bearer: 'Bearer',
        Refresh: 'Refresh'
    } as const;
    export type TokenTypeEnum = typeof TokenTypeEnum[keyof typeof TokenTypeEnum];
}


