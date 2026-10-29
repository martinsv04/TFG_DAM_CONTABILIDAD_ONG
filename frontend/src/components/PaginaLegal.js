import React from 'react';
import { Link } from 'react-router-dom';
import Icon from './Icons';
import { ULTIMA_ACTUALIZACION, sinCompletar } from '../config/sitio';
import '../styles/public.css';

// Texto de un dato del titular: si aún no está completado se resalta
export const Dato = ({ valor }) => (sinCompletar(valor) ? <mark className="legal-pendiente">{valor}</mark> : <strong>{valor}</strong>);

// Estructura común de las páginas legales
function PaginaLegal({ titulo, intro, children }) {
  return (
    <main className="legal-pagina">
      <div className="legal-inner fade-in">
        <Link to="/" className="auth-volver"><Icon name="arrow-left" size={16} /> Volver al inicio</Link>
        <h1>{titulo}</h1>
        <p className="legal-fecha">Última actualización: {ULTIMA_ACTUALIZACION}</p>
        {intro && <p className="legal-intro">{intro}</p>}
        <article className="legal">{children}</article>
      </div>
    </main>
  );
}

export default PaginaLegal;
