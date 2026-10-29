import React from 'react';
import { formatoEuro } from '../utils/format';

const MESES = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];

// Elige un paso "bonito" (1, 2, 2.5 o 5 x 10^n) para que el eje tenga como mucho 5 tramos
const escala = (maximo) => {
  if (maximo <= 0) return { tope: 100, marcas: [0, 25, 50, 75, 100] };
  const exp = Math.pow(10, Math.floor(Math.log10(maximo)) - 1);
  const paso = [1, 2, 2.5, 5, 10, 20, 25, 50].map((m) => m * exp).find((p) => Math.ceil(maximo / p) <= 5);
  const tramos = Math.ceil(maximo / paso);
  return { tope: paso * tramos, marcas: Array.from({ length: tramos + 1 }, (_, i) => paso * i) };
};

const abreviar = (valor) => (valor >= 1000 ? `${(valor / 1000).toLocaleString('es-ES', { maximumFractionDigits: 1 })}k` : String(valor));

// Barras agrupadas (ingresos y gastos) por mes. datos: [{ ingresos, gastos }] con 12 elementos.
const GraficoMensual = ({ datos, anio }) => {
  const ancho = 720;
  const alto = 280;
  const m = { arriba: 16, derecha: 12, abajo: 30, izquierda: 44 };
  const wInt = ancho - m.izquierda - m.derecha;
  const hInt = alto - m.arriba - m.abajo;
  const maximo = Math.max(0, ...datos.flatMap((d) => [d.ingresos, d.gastos]));
  const { tope, marcas } = escala(maximo);
  const paso = wInt / 12;
  const barra = Math.min(18, paso * 0.32);
  const y = (v) => m.arriba + hInt - (v / tope) * hInt;

  const resumen = `Ingresos y gastos por mes de ${anio}. ` + datos
    .map((d, i) => `${MESES[i]}: ingresos ${formatoEuro(d.ingresos)}, gastos ${formatoEuro(d.gastos)}`)
    .join('; ');

  return (
    <figure className="grafico">
      <svg viewBox={`0 0 ${ancho} ${alto}`} role="img" aria-label={resumen} className="grafico-svg">
        {marcas.map((v) => (
          <g key={v}>
            <line x1={m.izquierda} x2={ancho - m.derecha} y1={y(v)} y2={y(v)} className="grafico-rejilla" />
            <text x={m.izquierda - 8} y={y(v) + 4} textAnchor="end" className="grafico-eje">{abreviar(v)}</text>
          </g>
        ))}
        {datos.map((d, i) => {
          const centro = m.izquierda + paso * i + paso / 2;
          return (
            <g key={MESES[i]}>
              <rect x={centro - barra - 1} y={y(d.ingresos)} width={barra} height={Math.max(0, m.arriba + hInt - y(d.ingresos))} rx="3" className="grafico-ingresos">
                <title>{`${MESES[i]} ${anio} · Ingresos: ${formatoEuro(d.ingresos)}`}</title>
              </rect>
              <rect x={centro + 1} y={y(d.gastos)} width={barra} height={Math.max(0, m.arriba + hInt - y(d.gastos))} rx="3" className="grafico-gastos">
                <title>{`${MESES[i]} ${anio} · Gastos: ${formatoEuro(d.gastos)}`}</title>
              </rect>
              <text x={centro} y={alto - 10} textAnchor="middle" className="grafico-eje">{MESES[i]}</text>
            </g>
          );
        })}
      </svg>
      <figcaption className="grafico-leyenda">
        <span><i className="punto punto-ingresos" /> Ingresos</span>
        <span><i className="punto punto-gastos" /> Gastos</span>
      </figcaption>
    </figure>
  );
};

export default GraficoMensual;
