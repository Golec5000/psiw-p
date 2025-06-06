/**
 * Cinema Service API
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { ScreeningSummaryDto } from './screeningSummaryDto';


/**
 * Film dostępny w repertuarze z listą seansów
 */
export interface MovieResponse { 
    /**
     * ID filmu
     */
    id?: number;
    /**
     * Tytuł filmu
     */
    title?: string;
    /**
     * Opis filmu
     */
    description?: string;
    /**
     * URL do plakatu
     */
    image?: string;
    /**
     * Lista seansów dla filmu
     */
    screenings?: Array<ScreeningSummaryDto>;
}

