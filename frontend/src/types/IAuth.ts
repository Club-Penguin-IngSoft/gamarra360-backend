export type RolUsuario = 'CLIENTE' | 'COMERCIANTE' | 'ADMIN';

export interface ILoginRequest {
  email: string;
  contrasena: string;
}

export interface ILoginResponse {
  token: string;
  rol: RolUsuario;
  nombreCompleto: string;
  email: string;
  tenantId?: number;
}

export interface IGoogleLoginRequest {
  idToken: string;
}

export interface IGoogleAuthResponse {
  registrado: boolean;
  token?: string;
  usuarioId?: number;
  email?: string;
  rol?: RolUsuario;
  nombres?: string;
  primerApellido?: string;
  googleEmail?: string;
}

export interface IGoogleRegistroRequest {
  idToken: string;
  nombres: string;
  primerApellido: string;
  segundoApellido?: string;
  tipoDocumento: string;
  numeroDocumento: string;
  celular: string;
  contrasenha: string;
  rol: 'CLIENTE' | 'COMERCIANTE';
  idTienda?: number;
  ruc?: string;
  razonSocial?: string;
}
