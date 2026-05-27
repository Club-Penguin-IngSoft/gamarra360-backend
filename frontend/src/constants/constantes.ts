export const API_BASE_URL =
  (import.meta as any).env?.VITE_API_URL ?? 'http://localhost:8080/api/v1';

export const TOKEN_KEY = 'gamarra360_token';

export const GOOGLE_CLIENT_ID =
  (import.meta as any).env?.VITE_GOOGLE_CLIENT_ID ?? '123456789-placeholder.apps.googleusercontent.com';

export const RUTAS = {
  INICIO: '/',
  LOGIN: '/login',
  REGISTRO: '/registro',
  RECUPERAR_PASSWORD: '/recuperar-contrasena',
  DASHBOARD_CLIENTE: '/cliente/inicio',
  DASHBOARD_COMERCIANTE: '/comerciante/inicio',
  DASHBOARD_ADMIN: '/admin/inicio',
} as const;
