import React from 'react';
import { Link } from 'react-router-dom';

// Marca de ONGestión: un corazón sobre un cuadrado redondeado
export const LogoMark = ({ size = 36 }) => (
  <svg width={size} height={size} viewBox="0 0 32 32" aria-hidden="true" focusable="false">
    <rect width="32" height="32" rx="10" fill="var(--primary)" />
    <path
      transform="translate(5 5.2) scale(0.92)"
      fill="#fff"
      d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"
    />
  </svg>
);

const Logo = ({ to = '/', claro = false, onClick }) => (
  <Link to={to} className={`logo${claro ? ' logo-claro' : ''}`} onClick={onClick} aria-label="ONGestión, ir al inicio">
    <LogoMark />
    <span className="logo-texto">ONGestión</span>
  </Link>
);

export default Logo;
