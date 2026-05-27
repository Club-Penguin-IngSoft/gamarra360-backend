import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import { RUTAS } from './constants/constantes';

const App = () => {
  return (
    <Routes>
      <Route path={RUTAS.LOGIN} element={<LoginPage />} />
      {/* Redirige la raíz al login mientras no haya página de inicio */}
      <Route path="*" element={<Navigate to={RUTAS.LOGIN} replace />} />
    </Routes>
  );
};

export default App;
