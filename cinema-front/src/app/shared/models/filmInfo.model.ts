import { FilmDto } from "./film.model";

export interface CountryDto {
    countryId: number;
    countryName: string;
    directorList: DirectorDto[];
    filmList: FilmDto[];
}

export interface DirectorDto {
    directorId: number;
    directorNameAndSurname: string;
    birthDate: string;
    countryId: number;
    filmList: FilmDto[];
}

export interface FilmInfoListDto {
    directors: DirectorDto[];
    countries: CountryDto[];
}