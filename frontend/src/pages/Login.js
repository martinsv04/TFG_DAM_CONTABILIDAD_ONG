import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Logo from '../components/Logo';
import Icon from '../components/Icons';
import { guardarSesion } from '../services/authService';
import '../styles/public.css';

const Login = () => {
  const navigate = useNavigate();
  const [credentials, setCredentials] = useState({ email: '', password: '' });
  const [verPassword, setVerPassword] = useState(false);
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  const handleChange = (e) => {
    setCredentials({ ...credentials, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setEnviando(true);
    try {
      const response = await axios.post('http://localhost:8080/api/auth/login', credentials);

      guardarSesion({
        token: response.data.token,
        rol: response.data.rol,
        id: response.data.id,
      });
      navigate('/dashboard');
    } catch (err) {
      console.error('Error en login:', err.response ? err.response.data : err.message);
      setError(
        err.response && err.response.status === 401
          ? 'Correo o contraseña incorrectos.'
          : 'No se pudo conectar con el servidor. Inténtalo de nuevo en unos segundos.'
      );
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="auth">
      <aside className="auth-aside">
        <Logo claro />
        <div>
          <h2>Las cuentas de tu ONG, en un solo lugar</h2>
          <p>Accede para gestionar ingresos, gastos, facturas e informes de tu organización.</p>
        </div>
        <ul className="auth-puntos">
          <li><span className="punto-icono"><Icon name="check" size={15} /></span>Facturas en PDF al instante</li>
          <li><span className="punto-icono"><Icon name="check" size={15} /></span>Balance y estado de resultados</li>
          <li><span className="punto-icono"><Icon name="check" size={15} /></span>Equipos con roles y permisos</li>
        </ul>
      </aside>

      <main className="auth-main">
        <div className="auth-form-wrap fade-in">
          <Link to="/" className="auth-volver"><Icon name="arrow-left" size={16} /> Volver al inicio</Link>
          <h1>Iniciar sesión</h1>
          <p className="auth-sub">Introduce tus datos para entrar en tu panel.</p>

          <form className="form" onSubmit={handleSubmit}>
            {error && (
              <div className="alert alert-error" role="alert">
                <Icon name="alert-circle" size={18} />
                <span>{error}</span>
              </div>
            )}

            <div className="field">
              <label htmlFor="email">Correo electrónico</label>
              <input
                id="email"
                type="email"
                name="email"
                placeholder="tucorreo@ejemplo.com"
                autoComplete="email"
                value={credentials.email}
                onChange={handleChange}
                required
              />
            </div>

            <div className="field">
              <label htmlFor="password">Contraseña</label>
              <div className="campo-password">
                <input
                  id="password"
                  type={verPassword ? 'text' : 'password'}
                  name="password"
                  placeholder="Tu contraseña"
                  autoComplete="current-password"
                  value={credentials.password}
                  onChange={handleChange}
                  required
                />
                <button
                  type="button"
                  className="ver-password"
                  onClick={() => setVerPassword(!verPassword)}
                  aria-label={verPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                >
                  <Icon name={verPassword ? 'eye-off' : 'eye'} size={18} />
                </button>
              </div>
            </div>

            <button type="submit" className="btn btn-primary btn-lg btn-block" disabled={enviando}>
              {enviando ? 'Entrando...' : 'Entrar'}
            </button>
          </form>

          <p className="auth-pie">¿Aún no tienes acceso? <Link to="/registrarse">Contáctanos</Link></p>
        </div>
      </main>
    </div>
  );
};

export default Login;
