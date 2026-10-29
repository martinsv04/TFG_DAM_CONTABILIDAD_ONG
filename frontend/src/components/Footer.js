import React from 'react';
import { Link } from 'react-router-dom';
import Logo from './Logo';
import { abrirPreferenciasCookies } from '../utils/cookies';

function Footer() {
  return (
    <footer className="public-footer">
      <div className="public-footer-inner">
        <Logo />
        <p>Plataforma de gestión contable para ONGs.</p>
        <nav className="public-footer-legal" aria-label="Información legal">
          <Link to="/privacidad">Privacidad</Link>
          <Link to="/terminos">Términos y condiciones</Link>
          <button type="button" className="enlace-boton" onClick={abrirPreferenciasCookies}>Configurar cookies</button>
        </nav>
        <p className="text-muted">© {new Date().getFullYear()} ONGestión · Trabajo de fin de grado</p>
      </div>
    </footer>
  );
}

export default Footer;
