import React from 'react';
import Icon from './Icons';

export const Cargando = ({ texto = 'Cargando...' }) => (
  <div className="loading-state" role="status">
    <span className="spinner" />
    <span>{texto}</span>
  </div>
);

export const EstadoVacio = ({ icono = 'inbox', titulo, texto, accion }) => (
  <div className="empty">
    <span className="empty-icon"><Icon name={icono} size={26} /></span>
    <h3>{titulo}</h3>
    {texto && <p>{texto}</p>}
    {accion}
  </div>
);
