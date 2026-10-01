import React from 'react';
import Logo from './Logo';

function Footer() {
  return (
    <footer className="public-footer">
      <div className="public-footer-inner">
        <Logo />
        <p>Plataforma de gestión contable para ONGs.</p>
        <p className="text-muted">© {new Date().getFullYear()} ONGestión · Trabajo de fin de grado</p>
      </div>
    </footer>
  );
}

export default Footer;
