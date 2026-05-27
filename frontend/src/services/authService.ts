import axios from 'axios';
import { ILoginRequest, ILoginResponse, IGoogleAuthResponse, IGoogleRegistroRequest } from '../types/IAuth';
import { API_BASE_URL } from '../constants/constantes';

const authService = {
  login: async (credentials: ILoginRequest): Promise<ILoginResponse> => {
    const { data } = await axios.post<ILoginResponse>(
      `${API_BASE_URL}/auth/login`,
      credentials
    );
    return data;
  },

  loginConGoogle: async (idToken: string): Promise<IGoogleAuthResponse> => {
    const { data } = await axios.post<IGoogleAuthResponse>(
      `${API_BASE_URL}/auth/google`,
      { idToken }
    );
    return data;
  },

  registroConGoogle: async (registrationData: IGoogleRegistroRequest): Promise<ILoginResponse> => {
    const { data } = await axios.post<ILoginResponse>(
      `${API_BASE_URL}/auth/google/register`,
      registrationData
    );
    return data;
  },
};

export default authService;
