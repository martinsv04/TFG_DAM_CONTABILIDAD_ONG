import React from 'react';
import { Link } from 'react-router-dom';
import Logo from './Logo';
import { haySesionValida } from '../services/authService';
import '../styles/public.css';

// Barra superior de las páginas públicas (inicio)
function Navbar() {
  const conSesion = haySesionValida();

  return (
    <header className="public-nav">
      <div className="public-nav-inner">
        <Logo />

        <nav className="public-nav-links" aria-label="Secciones">
          <a href="#funciones">Funciones</a>
          <a href="#como-funciona">Cómo funciona</a>
        </nav>

        <div className="public-nav-actions">
          {conSesion ? (
            <Link to="/dashboard" className="btn btn-primary btn-sm">Ir al panel</Link>
          ) : (
            <>
              <Link to="/iniciar-sesion" className="btn btn-ghost btn-sm">Iniciar sesión</Link>
              <Link to="/registrarse" className="btn btn-primary btn-sm">Contáctanos</Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}

export default Navbar;
