import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import MaterialIcon from '../components/MaterialIcon';
import LogoGamarra from '../components/LogoGamarra';
import InputTexto from '../components/InputTexto';
import BotonPrimario from '../components/BotonPrimario';
import BotonGoogle from '../components/BotonGoogle';
import useLogin from '../hooks/useLogin';
import authService from '../services/authService';
import { RUTAS, TOKEN_KEY } from '../constants/constantes';
import { ILoginRequest, RolUsuario } from '../types/IAuth';
import { COLORES } from '../styles/tokens';

const rutaPorRol: Record<RolUsuario, string> = {
  CLIENTE: RUTAS.DASHBOARD_CLIENTE,
  COMERCIANTE: RUTAS.DASHBOARD_COMERCIANTE,
  ADMIN: RUTAS.DASHBOARD_ADMIN,
};

const LoginPage = () => {
  // Estados de Login tradicional
  const [form, setForm] = useState<ILoginRequest>({ email: '', contrasena: '' });
  const [mostrarPassword, setMostrarPassword] = useState(false);
  const { iniciarSesion, cargando: loginCargando, error: loginError, limpiarError } = useLogin();
  const navigate = useNavigate();

  // Estados para el flujo de Google
  const [vista, setVista] = useState<'LOGIN' | 'GOOGLE_REGISTRO'>('LOGIN');
  const [googleData, setGoogleData] = useState<{
    idToken: string;
    email: string;
  } | null>(null);

  // Formulario de Registro para primera vez de Google
  const [registroForm, setRegistroForm] = useState({
    nombres: '',
    primerApellido: '',
    segundoApellido: '',
    tipoDocumento: 'DNI',
    numeroDocumento: '',
    celular: '',
    contrasenha: '',
    confirmarContrasenha: '',
    rol: 'CLIENTE' as 'CLIENTE' | 'COMERCIANTE',
    
    // Campos para Vendedor / Comerciante
    idTienda: '',
    ruc: '',
    razonSocial: '',
  });

  const [mostrarRegPassword, setMostrarRegPassword] = useState(false);
  const [mostrarRegConfirmarPassword, setMostrarRegConfirmarPassword] = useState(false);

  // Estado para la pantalla de confirmación (Comerciante en espera de aprobación)
  const [solicitudEnviada, setSolicitudEnviada] = useState(false);
  
  const [cargandoGoogle, setCargandoGoogle] = useState(false);
  const [errorGoogle, setErrorGoogle] = useState<string | null>(null);

  const formularioValido =
    form.email.trim() !== '' && form.contrasena.trim() !== '';

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleRegistroChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setRegistroForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleCheckboxChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const checked = e.target.checked;
    setRegistroForm((prev) => ({
      ...prev,
      rol: checked ? 'COMERCIANTE' : 'CLIENTE',
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (formularioValido && !loginCargando) {
      iniciarSesion(form);
    }
  };

  // Google Login Callback
  const handleGoogleSuccess = async (idToken: string) => {
    try {
      setCargandoGoogle(true);
      setErrorGoogle(null);
      limpiarError();

      const data = await authService.loginConGoogle(idToken);
      
      if (data.registrado) {
        // Ya registrado: iniciar sesión directa
        if (data.token) {
          localStorage.setItem(TOKEN_KEY, data.token);
          navigate(rutaPorRol[data.rol]);
        }
      } else {
        // Primera vez con Google: mostrar formulario
        setGoogleData({ idToken, email: data.googleEmail || '' });
        setRegistroForm((prev) => ({
          ...prev,
          nombres: data.nombres || '',
          primerApellido: data.primerApellido || '',
        }));
        setVista('GOOGLE_REGISTRO');
      }
    } catch (err: any) {
      console.error(err);
      const mensaje = err?.response?.data?.mensaje ?? 'Error al autenticar con Google.';
      setErrorGoogle(mensaje);
    } finally {
      setCargandoGoogle(false);
    }
  };

  const handleGoogleError = (err: string) => {
    setErrorGoogle(err);
  };

  // Google Register Submit
  const handleGoogleRegistroSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!googleData) return;

    if (registroForm.contrasenha !== registroForm.confirmarContrasenha) {
      setErrorGoogle('Las contraseñas no coinciden.');
      return;
    }

    try {
      setCargandoGoogle(true);
      setErrorGoogle(null);

      const requestPayload = {
        idToken: googleData.idToken,
        nombres: registroForm.nombres,
        primerApellido: registroForm.primerApellido,
        segundoApellido: registroForm.segundoApellido || undefined,
        tipoDocumento: registroForm.tipoDocumento,
        numeroDocumento: registroForm.numeroDocumento,
        celular: registroForm.celular,
        contrasenha: registroForm.contrasenha,
        rol: registroForm.rol,
        idTienda: registroForm.rol === 'COMERCIANTE' ? Number(registroForm.idTienda) : undefined,
        ruc: registroForm.rol === 'COMERCIANTE' ? registroForm.ruc : undefined,
        razonSocial: registroForm.rol === 'COMERCIANTE' ? registroForm.razonSocial : undefined,
      };

      const response = await authService.registroConGoogle(requestPayload);
      
      if (registroForm.rol === 'COMERCIANTE' && !response.token) {
        // Vendedor registrado pero inactivo en espera de aprobación
        setSolicitudEnviada(true);
      } else if (response.token) {
        // Cliente registrado y logueado
        localStorage.setItem(TOKEN_KEY, response.token);
        navigate(rutaPorRol[response.rol]);
      }
    } catch (err: any) {
      console.error(err);
      const mensaje = err?.response?.data?.mensaje ?? 'Error al registrar la cuenta.';
      setErrorGoogle(mensaje);
    } finally {
      setCargandoGoogle(false);
    }
  };

  const registroFormValido =
    registroForm.nombres.trim() !== '' &&
    registroForm.primerApellido.trim() !== '' &&
    registroForm.numeroDocumento.trim() !== '' &&
    registroForm.celular.trim() !== '' &&
    registroForm.contrasenha.trim() !== '' &&
    registroForm.contrasenha === registroForm.confirmarContrasenha &&
    (registroForm.rol === 'CLIENTE' ||
      (registroForm.idTienda.trim() !== '' &&
        registroForm.ruc.trim() !== '' &&
        registroForm.razonSocial.trim() !== ''));

  return (
    <div className="min-h-screen flex flex-col font-sans">

      {/* ── Barra de navegación superior ────────────────────────────────── */}
      <header className="fixed top-0 left-0 right-0 z-50 bg-white border-b border-neutro-200 h-14 flex items-center px-6 justify-between">
        <LogoGamarra size="sm" />
        {vista === 'GOOGLE_REGISTRO' ? (
          <button
            onClick={() => {
              setVista('LOGIN');
              setErrorGoogle(null);
            }}
            className="flex items-center gap-1 text-sm font-medium transition-opacity hover:opacity-75"
            style={{ color: COLORES.primario }}
          >
            <MaterialIcon name="arrow_back" style={{ fontSize: '18px' }} />
            Volver a Iniciar Sesión
          </button>
        ) : (
          <Link
            to={RUTAS.INICIO}
            className="flex items-center gap-1 text-sm font-medium transition-opacity hover:opacity-75"
            style={{ color: COLORES.primario }}
          >
            <MaterialIcon name="arrow_back" style={{ fontSize: '18px' }} />
            Volver al Inicio
          </Link>
        )}
      </header>

      {/* ── Contenido principal ──────────────────────────────────────────── */}
      <div className="flex flex-1 pt-14">

        {/* ── Panel izquierdo — imagen hero ─────────────────────────────── */}
        <div
          className="hidden lg:flex flex-col justify-end relative overflow-hidden"
          style={{ width: '58%' }}
        >
          <div
            className="absolute inset-0 bg-cover bg-center"
            style={{ backgroundImage: "url('/login-bg.jpg')" }}
          />
          <div
            className="absolute inset-0"
            style={{
              background:
                'linear-gradient(to top, rgba(0,0,0,0.72) 40%, rgba(0,0,0,0.25) 100%)',
            }}
          />
          <div className="relative z-10 p-12 pb-14">
            <span
              className="inline-block px-4 py-1 rounded-full text-white text-xs font-bold uppercase tracking-widest mb-5"
              style={{ backgroundColor: COLORES.primario }}
            >
              DISTRITO TEXTIL
            </span>
            <h1 className="text-white font-extrabold leading-tight mb-4" style={{ fontSize: '3rem' }}>
              Donde la moda<br />cobra vida
            </h1>
            <p className="text-white/75 text-base max-w-sm leading-relaxed">
              Únete a la red de comerciantes más vibrante de Latinoamérica.
              Gestiona tu stock, conecta con clientes y escala tu marca.
            </p>
          </div>
        </div>

        {/* ── Panel derecho — formulario ─────────────────────────────────── */}
        <div className="flex-1 flex items-center justify-center px-8 py-10 bg-white overflow-y-auto">
          <div className="w-full max-w-sm my-auto">

            <LogoGamarra size="md" className="mb-6" />

            {/* ── ESCENARIO 0: Solicitud de Comerciante Enviada con éxito ──── */}
            {solicitudEnviada ? (
              <div className="text-center space-y-6 py-6 animate-fade-in">
                <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-success-claro text-success border border-success/15 shadow-sm">
                  <MaterialIcon name="storefront" style={{ fontSize: '36px' }} />
                </div>
                <div className="space-y-2">
                  <h3 className="text-2xl font-extrabold text-neutro-900">
                    ¡Solicitud Enviada!
                  </h3>
                  <p className="text-neutro-500 text-sm leading-relaxed">
                    Tu registro como **Comerciante/Vendedor** fue exitoso. Por seguridad, tu cuenta ha sido enviada al administrador para su revisión y aprobación.
                  </p>
                  <p className="text-neutro-400 text-xs">
                    Te enviaremos un correo electrónico una vez que tu tienda haya sido aprobada.
                  </p>
                </div>
                <div className="pt-4">
                  <BotonPrimario onClick={() => setSolicitudEnviada(false) || setVista('LOGIN')}>
                    Volver a Inicio
                  </BotonPrimario>
                </div>
              </div>
            ) : vista === 'LOGIN' ? (
              /* ── ESCENARIO 1: Vista de Inicio de Sesión Normal ──────────── */
              <div className="animate-fade-in">
                <h2 className="text-3xl font-extrabold text-neutro-900 mb-1">
                  Bienvenido de nuevo
                </h2>
                <p className="text-neutro-400 text-sm mb-6">
                  Ingresa tus credenciales para acceder.
                </p>

                {(loginError || errorGoogle) && (
                  <div className="mb-5 flex items-start gap-2 px-4 py-3 rounded-xl bg-error-claro border border-error/20 text-error text-sm">
                    <MaterialIcon name="error_outline" style={{ fontSize: '18px', marginTop: '1px' }} />
                    <span>{loginError || errorGoogle}</span>
                  </div>
                )}

                <form onSubmit={handleSubmit} noValidate className="space-y-4">
                  <InputTexto
                    tipo="email"
                    nombre="email"
                    placeholder="Correo electrónico"
                    valor={form.email}
                    onChange={handleChange}
                    autoComplete="email"
                    disabled={loginCargando || cargandoGoogle}
                  />

                  <InputTexto
                    tipo={mostrarPassword ? 'text' : 'password'}
                    nombre="contrasena"
                    placeholder="Contraseña"
                    valor={form.contrasena}
                    onChange={handleChange}
                    autoComplete="current-password"
                    disabled={loginCargando || cargandoGoogle}
                    sufijo={
                      <button
                        type="button"
                        onClick={() => setMostrarPassword((v) => !v)}
                        className="text-neutro-400 hover:text-neutro-500 transition-colors"
                        tabIndex={-1}
                        aria-label={mostrarPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                      >
                        <MaterialIcon
                          name={mostrarPassword ? 'visibility_off' : 'visibility'}
                          style={{ fontSize: '20px' }}
                        />
                      </button>
                    }
                  />

                  <BotonPrimario
                    type="submit"
                    disabled={!formularioValido}
                    cargando={loginCargando}
                  >
                    Ingresar
                  </BotonPrimario>
                </form>

                <p className="text-center text-sm text-neutro-400 mt-5">
                  ¿Olvidaste tu contraseña?{' '}
                  <Link
                    to={RUTAS.RECUPERAR_PASSWORD}
                    className="font-semibold hover:underline"
                    style={{ color: COLORES.primario }}
                  >
                    Haz clic aquí
                  </Link>
                </p>

                <div className="flex items-center gap-3 my-6">
                  <div className="flex-1 h-px bg-neutro-100" />
                  <span className="text-xs text-neutro-400 uppercase tracking-widest font-medium whitespace-nowrap">
                    O continúa con
                  </span>
                  <div className="flex-1 h-px bg-neutro-100" />
                </div>

                <div className="w-full relative">
                  {cargandoGoogle && (
                    <div className="absolute inset-0 bg-white/60 flex items-center justify-center z-10 rounded-xl">
                      <svg className="animate-spin h-5 w-5 text-primario" viewBox="0 0 24 24" fill="none">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                      </svg>
                    </div>
                  )}
                  <BotonGoogle
                    onSuccess={handleGoogleSuccess}
                    onError={handleGoogleError}
                  />
                </div>

                <p className="text-center text-sm text-neutro-400 mt-6">
                  ¿No tienes una cuenta?{' '}
                  <Link
                    to={RUTAS.REGISTRO}
                    className="font-semibold hover:underline"
                    style={{ color: COLORES.primario }}
                  >
                    Regístrate ahora
                  </Link>
                </p>
              </div>
            ) : (
              /* ── ESCENARIO 2: Formulario dinámico de Registro con Google ── */
              <div className="animate-fade-in space-y-5">
                <div>
                  <h2 className="text-2xl font-extrabold text-neutro-900 mb-1">
                    Completa tu Perfil
                  </h2>
                  <p className="text-neutro-400 text-sm">
                    Es tu primera vez con Google. Añade estos datos para continuar.
                  </p>
                </div>

                {errorGoogle && (
                  <div className="flex items-start gap-2 px-4 py-3 rounded-xl bg-error-claro border border-error/20 text-error text-sm">
                    <MaterialIcon name="error_outline" style={{ fontSize: '18px', marginTop: '1px' }} />
                    <span>{errorGoogle}</span>
                  </div>
                )}

                <form onSubmit={handleGoogleRegistroSubmit} className="space-y-4">
                  
                  {/* Correo Electrónico — Pre-completado y No Modificable */}
                  <div className="space-y-1">
                    <label className="text-xs font-semibold text-neutro-400 uppercase tracking-wider block">
                      Correo Electrónico
                    </label>
                    <InputTexto
                      tipo="email"
                      nombre="googleEmail"
                      placeholder="Correo de Google"
                      valor={googleData?.email || ''}
                      onChange={() => {}}
                      disabled={true}
                    />
                  </div>

                  {/* Nombres y Apellidos */}
                  <div className="grid grid-cols-2 gap-3">
                    <div className="space-y-1">
                      <label className="text-xs font-semibold text-neutro-400 uppercase tracking-wider block">
                        Nombres
                      </label>
                      <InputTexto
                        tipo="text"
                        nombre="nombres"
                        placeholder="Nombres"
                        valor={registroForm.nombres}
                        onChange={handleRegistroChange}
                        disabled={cargandoGoogle}
                      />
                    </div>
                    <div className="space-y-1">
                      <label className="text-xs font-semibold text-neutro-400 uppercase tracking-wider block">
                        Apellido Paterno
                      </label>
                      <InputTexto
                        tipo="text"
                        nombre="primerApellido"
                        placeholder="Apellido Paterno"
                        valor={registroForm.primerApellido}
                        onChange={handleRegistroChange}
                        disabled={cargandoGoogle}
                      />
                    </div>
                  </div>

                  {/* Apellido Materno */}
                  <div className="space-y-1">
                    <label className="text-xs font-semibold text-neutro-400 uppercase tracking-wider block">
                      Apellido Materno (Opcional)
                    </label>
                    <InputTexto
                      tipo="text"
                      nombre="segundoApellido"
                      placeholder="Apellido Materno"
                      valor={registroForm.segundoApellido}
                      onChange={handleRegistroChange}
                      disabled={cargandoGoogle}
                    />
                  </div>

                  {/* Tipo de Documento y Número */}
                  <div className="grid grid-cols-5 gap-3">
                    <div className="col-span-2 space-y-1">
                      <label className="text-xs font-semibold text-neutro-400 uppercase tracking-wider block">
                        Tipo Doc.
                      </label>
                      <select
                        name="tipoDocumento"
                        value={registroForm.tipoDocumento}
                        onChange={handleRegistroChange}
                        disabled={cargandoGoogle}
                        className="
                          w-full px-3 py-3 text-sm
                          rounded-input border border-neutro-200
                          text-neutro-900 bg-white
                          focus:outline-none focus:border-primario focus:ring-2 focus:ring-primario-claro
                          disabled:bg-neutro-50 disabled:text-neutro-400 disabled:cursor-not-allowed
                          transition-all duration-150
                        "
                      >
                        <option value="DNI">DNI</option>
                        <option value="PASAPORTE">PASAPORTE</option>
                      </select>
                    </div>
                    <div className="col-span-3 space-y-1">
                      <label className="text-xs font-semibold text-neutro-400 uppercase tracking-wider block">
                        Número de Documento
                      </label>
                      <InputTexto
                        tipo="text"
                        nombre="numeroDocumento"
                        placeholder="Número de documento"
                        valor={registroForm.numeroDocumento}
                        onChange={handleRegistroChange}
                        disabled={cargandoGoogle}
                      />
                    </div>
                  </div>

                  {/* Celular */}
                  <div className="space-y-1">
                    <label className="text-xs font-semibold text-neutro-400 uppercase tracking-wider block">
                      Celular / Teléfono
                    </label>
                    <InputTexto
                      tipo="tel"
                      nombre="celular"
                      placeholder="Ej. +51 987654321"
                      valor={registroForm.celular}
                      onChange={handleRegistroChange}
                      disabled={cargandoGoogle}
                    />
                  </div>

                  {/* Contraseñas */}
                  <div className="grid grid-cols-2 gap-3">
                    <div className="space-y-1">
                      <label className="text-xs font-semibold text-neutro-400 uppercase tracking-wider block">
                        Contraseña
                      </label>
                      <InputTexto
                        tipo={mostrarRegPassword ? 'text' : 'password'}
                        nombre="contrasenha"
                        placeholder="Contraseña"
                        valor={registroForm.contrasenha}
                        onChange={handleRegistroChange}
                        disabled={cargandoGoogle}
                        sufijo={
                          <button
                            type="button"
                            onClick={() => setMostrarRegPassword((v) => !v)}
                            className="text-neutro-400 hover:text-neutro-500 transition-colors"
                            tabIndex={-1}
                          >
                            <MaterialIcon
                              name={mostrarRegPassword ? 'visibility_off' : 'visibility'}
                              style={{ fontSize: '18px' }}
                            />
                          </button>
                        }
                      />
                    </div>
                    <div className="space-y-1">
                      <label className="text-xs font-semibold text-neutro-400 uppercase tracking-wider block">
                        Confirmar
                      </label>
                      <InputTexto
                        tipo={mostrarRegConfirmarPassword ? 'text' : 'password'}
                        nombre="confirmarContrasenha"
                        placeholder="Confirmar"
                        valor={registroForm.confirmarContrasenha}
                        onChange={handleRegistroChange}
                        disabled={cargandoGoogle}
                        sufijo={
                          <button
                            type="button"
                            onClick={() => setMostrarRegConfirmarPassword((v) => !v)}
                            className="text-neutro-400 hover:text-neutro-500 transition-colors"
                            tabIndex={-1}
                          >
                            <MaterialIcon
                              name={mostrarRegConfirmarPassword ? 'visibility_off' : 'visibility'}
                              style={{ fontSize: '18px' }}
                            />
                          </button>
                        }
                      />
                    </div>
                  </div>

                  {/* Pregunta "¿Quieres vender?" (Checkbox Estilizado) */}
                  <div className="flex items-center gap-2 py-2 border-t border-b border-neutro-100">
                    <input
                      type="checkbox"
                      id="quieresVender"
                      checked={registroForm.rol === 'COMERCIANTE'}
                      onChange={handleCheckboxChange}
                      disabled={cargandoGoogle}
                      className="w-4 h-4 text-primario border-neutro-300 rounded focus:ring-primario cursor-pointer"
                    />
                    <label htmlFor="quieresVender" className="text-sm font-semibold text-neutro-800 cursor-pointer">
                      ¿Quieres vender? (Registrar Tienda)
                    </label>
                  </div>

                  {/* Campos adicionales para COMERCIANTE / VENDEDOR */}
                  {registroForm.rol === 'COMERCIANTE' && (
                    <div className="space-y-3 p-4 rounded-2xl bg-neutro-50 border border-neutro-100 animate-slide-down">
                      
                      <div className="space-y-1">
                        <label className="text-xs font-semibold text-neutro-500 uppercase tracking-wider block">
                          Número de Tienda
                        </label>
                        <InputTexto
                          tipo="number"
                          nombre="idTienda"
                          placeholder="Ej. 104"
                          valor={registroForm.idTienda}
                          onChange={handleRegistroChange}
                          disabled={cargandoGoogle}
                        />
                      </div>

                      <div className="space-y-1">
                        <label className="text-xs font-semibold text-neutro-500 uppercase tracking-wider block">
                          RUC
                        </label>
                        <InputTexto
                          tipo="text"
                          nombre="ruc"
                          placeholder="RUC de 11 dígitos"
                          valor={registroForm.ruc}
                          onChange={handleRegistroChange}
                          disabled={cargandoGoogle}
                        />
                      </div>

                      <div className="space-y-1">
                        <label className="text-xs font-semibold text-neutro-500 uppercase tracking-wider block">
                          Razón Social
                        </label>
                        <InputTexto
                          tipo="text"
                          nombre="razonSocial"
                          placeholder="Nombre comercial o jurídico"
                          valor={registroForm.razonSocial}
                          onChange={handleRegistroChange}
                          disabled={cargandoGoogle}
                        />
                      </div>

                    </div>
                  )}

                  <BotonPrimario
                    type="submit"
                    disabled={!registroFormValido}
                    cargando={cargandoGoogle}
                  >
                    {registroForm.rol === 'COMERCIANTE' ? 'Enviar Solicitud' : 'Completar Registro'}
                  </BotonPrimario>
                </form>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
