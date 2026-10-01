import axios from 'axios';

// Dirección del backend. El token solo se envía a peticiones que empiecen por esta URL.
const API_URL = 'http://localhost:8080';

export const guardarSesion = ({ token, rol, id }) => {
  localStorage.setItem('token', token);
  localStorage.setItem('rol', rol);
  localStorage.setItem('userId', id);
};

export const cerrarSesion = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('rol');
  localStorage.removeItem('userId');
};

export const getToken = () => localStorage.getItem('token');

export const getRol = () => localStorage.getItem('rol');

// Un JWT lleva la fecha de caducidad ("exp", en segundos) en su segunda parte, codificada en base64
const tokenCaducado = (token) => {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const payload = JSON.parse(atob(base64));
    return payload.exp * 1000 < Date.now();
  } catch (error) {
    return true;
  }
};

export const haySesionValida = () => {
  const token = getToken();
  return Boolean(token) && !tokenCaducado(token);
};

// Se llama una sola vez al arrancar la aplicación (index.js)
export const configurarAxios = () => {
  // Añade "Authorization: Bearer <token>" a todas las peticiones al backend
  axios.interceptors.request.use((config) => {
    const token = getToken();
    if (token && config.url && config.url.startsWith(API_URL)) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });

  // Si el backend responde 401 (token caducado o inválido), se cierra la sesión
  axios.interceptors.response.use(
    (response) => response,
    (error) => {
      const url = error.config && error.config.url ? error.config.url : '';
      const esLogin = url.endsWith('/api/auth/login');
      if (error.response && error.response.status === 401 && !esLogin) {
        cerrarSesion();
        if (window.location.pathname !== '/iniciar-sesion') {
          window.location.href = '/iniciar-sesion';
        }
      }
      return Promise.reject(error);
    }
  );
};
