import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import { ILoginRequest, RolUsuario } from '../types/IAuth';
import { TOKEN_KEY, RUTAS } from '../constants/constantes';

const rutaPorRol: Record<RolUsuario, string> = {
  CLIENTE: RUTAS.DASHBOARD_CLIENTE,
  COMERCIANTE: RUTAS.DASHBOARD_COMERCIANTE,
  ADMIN: RUTAS.DASHBOARD_ADMIN,
};

const useLogin = () => {
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const iniciarSesion = async (credentials: ILoginRequest) => {
    try {
      setCargando(true);
      setError(null);

      const response = await authService.login(credentials);
      localStorage.setItem(TOKEN_KEY, response.token);
      navigate(rutaPorRol[response.rol]);
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { mensaje?: string } } };
      const mensaje =
        axiosError?.response?.data?.mensaje ??
        'Correo o contraseña incorrectos. Inténtalo de nuevo.';
      setError(mensaje);
    } finally {
      setCargando(false);
    }
  };

  const limpiarError = () => setError(null);

  return { iniciarSesion, cargando, error, limpiarError };
};

export default useLogin;
