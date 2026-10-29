import React from 'react';
import { formatoEuro } from '../utils/format';

const RADIO = 70;
const LONGITUD = 2 * Math.PI * RADIO;
const HUECO = 3; // separación entre porciones, en unidades del trazo

// Gráfico de anillo. partes: [{ clave, nombre, importe, porcentaje }]; colores: { clave: '#hex' }.
// La leyenda lleva siempre nombre, importe y porcentaje, así que el color nunca es lo único que identifica.
const GraficoDonut = ({ titulo, partes, colores, centro }) => {
  const total = partes.reduce((suma, p) => suma + Number(p.importe || 0), 0);
  const resumen = `${titulo}. ` + (total > 0
    ? partes.map((p) => `${p.nombre}: ${formatoEuro(p.importe)} (${String(p.porcentaje).replace('.', ',')} %)`).join('; ')
    : 'Sin datos');

  let acumulado = 0;
  return (
    <figure className="donut">
      <svg viewBox="0 0 200 200" role="img" aria-label={resumen} className="donut-svg">
        <circle cx="100" cy="100" r={RADIO} fill="none" stroke="var(--surface-2)" strokeWidth="26" />
        {total > 0 && partes.map((p) => {
          const largo = (Number(p.importe) / total) * LONGITUD;
          if (largo <= 0) return null;
          const visible = partes.filter((x) => Number(x.importe) > 0).length > 1 ? Math.max(largo - HUECO, 0.5) : largo;
          const desplazamiento = -acumulado;
          acumulado += largo;
          return (
            <circle
              key={p.clave}
              cx="100" cy="100" r={RADIO} fill="none"
              stroke={colores[p.clave]} strokeWidth="26"
              strokeDasharray={`${visible} ${LONGITUD - visible}`}
              strokeDashoffset={desplazamiento}
              transform="rotate(-90 100 100)"
            >
              <title>{`${p.nombre}: ${formatoEuro(p.importe)} (${String(p.porcentaje).replace('.', ',')} %)`}</title>
            </circle>
          );
        })}
        <text x="100" y="96" textAnchor="middle" className="donut-centro">{total > 0 ? formatoEuro(total) : 'Sin datos'}</text>
        {centro && <text x="100" y="116" textAnchor="middle" className="donut-centro-sub">{centro}</text>}
      </svg>
      <figcaption>
        <ul className="donut-leyenda">
          {partes.map((p) => (
            <li key={p.clave}>
              <i className="punto" style={{ background: colores[p.clave] }} />
              <span className="donut-nombre">{p.nombre}</span>
              <span className="donut-importe">{formatoEuro(p.importe)}</span>
              <strong className="donut-pct">{String(p.porcentaje).replace('.', ',')} %</strong>
            </li>
          ))}
        </ul>
      </figcaption>
    </figure>
  );
};

export default GraficoDonut;
