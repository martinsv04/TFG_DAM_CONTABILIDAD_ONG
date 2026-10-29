import React from 'react';
import { Link } from 'react-router-dom';
import Icon from '../components/Icons';
import { haySesionValida } from '../services/authService';
import '../styles/public.css';

// Página 404
function NoEncontrada() {
  return (
    <main className="estado-pagina">
      <div className="estado-inner fade-in">
        <p className="estado-codigo" aria-hidden="true">404</p>
        <h1>No encontramos esta página</h1>
        <p className="estado-texto">
          Puede que el enlace esté mal escrito o que la página ya no exista. Te ayudamos a volver a un sitio conocido.
        </p>
        <div className="estado-acciones">
          <Link to="/" className="btn btn-primary btn-lg"><Icon name="home" size={18} /> Ir al inicio</Link>
          {haySesionValida() ? (
            <Link to="/dashboard" className="btn btn-secondary btn-lg">Ir a mi panel</Link>
          ) : (
            <Link to="/iniciar-sesion" className="btn btn-secondary btn-lg">Iniciar sesión</Link>
          )}
        </div>
      </div>
    </main>
  );
}

export default NoEncontrada;
