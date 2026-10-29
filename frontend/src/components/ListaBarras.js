import React from 'react';
import { formatoEuro } from '../utils/format';

// Barras horizontales con nombre, importe y porcentaje. partes: [{ clave, nombre, importe, porcentaje }]
const ListaBarras = ({ partes, color = 'var(--primary)', etiqueta }) => {
  const maximo = Math.max(0, ...partes.map((p) => Number(p.porcentaje)));
  return (
    <ul className="barras-lista" aria-label={etiqueta}>
      {partes.map((p) => (
        <li key={p.clave}>
          <div className="barras-texto">
            <span className="barras-nombre">{p.nombre}</span>
            <span className="barras-valor">{formatoEuro(p.importe)} · <strong>{String(p.porcentaje).replace('.', ',')} %</strong></span>
          </div>
          <div className="barras-pista">
            <div className="barras-relleno" style={{ width: `${maximo > 0 ? (Number(p.porcentaje) / maximo) * 100 : 0}%`, background: color }} />
          </div>
        </li>
      ))}
    </ul>
  );
};

export default ListaBarras;
