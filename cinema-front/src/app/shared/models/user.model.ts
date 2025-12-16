import {BookingDto} from '../../shared/models'
export interface UserRegisterDto {
  name: string;
  surname: string;
  email: string;
  birthDate: string; // или Date
  password: string;
}

export interface UserLoginDto {
  email: string;
  password: string;
}

export interface UserDto {
  userId: number;
  name: string;
  surname: string;
  email: string;
  birthDate: string;
  age: number;
  registrationDate: string;
  role: string;
  bookingList?: BookingDto[]; // Опционально, чтобы избежать циклических ссылок
}