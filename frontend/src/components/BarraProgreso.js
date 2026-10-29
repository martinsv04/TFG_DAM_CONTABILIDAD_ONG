import React from 'react';

// Barra de ejecución. porcentaje: número (puede pasar de 100) o null si no hay base de comparación.
const BarraProgreso = ({ porcentaje, etiqueta }) => {
  if (porcentaje === null || porcentaje === undefined) {
    return <span className="text-muted">Sin importe de referencia</span>;
  }
  const valor = Number(porcentaje);
  const clase = valor > 100 ? 'progreso-excedida' : valor >= 85 ? 'progreso-alta' : '';
  return (
    <div className="progreso-wrap">
      <div className={`progreso ${clase}`} role="progressbar" aria-valuemin={0} aria-valuemax={100} aria-valuenow={Math.min(valor, 100)} aria-label={etiqueta}>
        <div className="progreso-relleno" style={{ width: `${Math.min(valor, 100)}%` }} />
      </div>
      <span className="progreso-texto">{valor.toLocaleString('es-ES', { maximumFractionDigits: 1 })} %</span>
    </div>
  );
};

export default BarraProgreso;
