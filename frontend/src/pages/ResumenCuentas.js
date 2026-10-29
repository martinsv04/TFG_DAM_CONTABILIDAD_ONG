import React, { useEffect, useMemo, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import PageHeader from '../components/PageHeader';
import { Cargando, EstadoVacio } from '../components/Estados';
import { toast } from '../components/Toast';
import { formatoEuro, formatoFecha } from '../utils/format';
import { descargarCsv } from '../utils/exportar';
import { nombreCuenta } from '../utils/cuentas';

const API = 'http://localhost:8080/api';
const anioDe = (m) => Number(String(m.fecha || '').slice(0, 4)) || 0;
const num = (v) => Number(v) || 0;

// Agrupa los movimientos por cuenta contable (los que no tienen cuenta van aparte, al final)
const agrupar = (movimientos) => {
  const grupos = new Map();
  movimientos.forEach((m) => {
    const codigo = m.cuenta || '';
    if (!grupos.has(codigo)) grupos.set(codigo, { codigo, nombre: nombreCuenta(codigo), movimientos: [], total: 0 });
    const g = grupos.get(codigo);
    g.movimientos.push(m);
    g.total += num(m.monto);
  });
  return [...grupos.values()].sort((a, b) => {
    if (!a.codigo) return 1;
    if (!b.codigo) return -1;
    return a.codigo.localeCompare(b.codigo, 'es', { numeric: true });
  });
};

const TablaCuentas = ({ titulo, icono, claseTotal, grupos, total, vacio }) => {
  const [abiertas, setAbiertas] = useState({});
  const alternar = (codigo) => setAbiertas((a) => ({ ...a, [codigo]: !a[codigo] }));

  return (
    <section className="card">
      <div className="card-header">
        <h3><Icon name={icono} size={20} className={claseTotal} /> {titulo}</h3>
        <strong className={claseTotal}>{formatoEuro(total)}</strong>
      </div>
      {grupos.length === 0 ? (
        <p className="text-muted resumen-vacio">{vacio}</p>
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr><th>Cuenta</th><th className="num">Movimientos</th><th className="num">Importe</th></tr>
            </thead>
            <tbody>
              {grupos.map((g) => {
                const abierta = Boolean(abiertas[g.codigo]);
                const clave = g.codigo || 'sin-cuenta';
                return (
                  <React.Fragment key={clave}>
                    <tr className="cuenta-fila">
                      <td>
                        <button type="button" className="cuenta-alternar" onClick={() => alternar(g.codigo)} aria-expanded={abierta} aria-controls={`cuenta-${clave}`}>
                          <span className={`cuenta-flecha${abierta ? ' abierta' : ''}`} aria-hidden="true"><Icon name="arrow-right" size={14} /></span>
                          {g.codigo ? (
                            <>
                              <span className="cuenta-codigo">{g.codigo}</span>
                              <span>{g.nombre || 'Cuenta propia'}</span>
                            </>
                          ) : (
                            <span className="text-muted">Sin cuenta asignada</span>
                          )}
                        </button>
                      </td>
                      <td className="num text-muted">{g.movimientos.length}</td>
                      <td className="num"><strong>{formatoEuro(g.total)}</strong></td>
                    </tr>
                    {abierta && (
                      <tr className="cuenta-detalle" id={`cuenta-${clave}`}>
                        <td colSpan="3">
                          <table className="table cuenta-detalle-tabla">
                            <tbody>
                              {[...g.movimientos].sort((a, b) => String(b.fecha).localeCompare(String(a.fecha))).map((m) => (
                                <tr key={m.id}>
                                  <td className="text-muted nowrap">{formatoFecha(m.fecha)}</td>
                                  <td>{m.descripcion || 'Sin descripción'}</td>
                                  <td className="num">{formatoEuro(m.monto)}</td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
};

const ResumenCuentas = () => {
  const { id } = useParams();
  const [datos, setDatos] = useState(null);
  const [error, setError] = useState(false);
  const [anio, setAnio] = useState(null); // null = el último año con datos; 0 = todos

  useEffect(() => {
    Promise.all([axios.get(`${API}/ingresos/ong/${id}`), axios.get(`${API}/gastos/ong/${id}`)])
      .then(([i, g]) => setDatos({ ingresos: i.data, gastos: g.data }))
      .catch((err) => {
        console.error('Error al cargar los movimientos:', err);
        setError(true);
      });
  }, [id]);

  const anios = useMemo(() => {
    if (!datos) return [];
    return [...new Set([...datos.ingresos, ...datos.gastos].map(anioDe).filter(Boolean))].sort((a, b) => b - a);
  }, [datos]);

  const anioActivo = anio === null ? anios[0] || 0 : anio;

  const { gruposIngreso, gruposGasto, totalIngresos, totalGastos, sinCuenta } = useMemo(() => {
    if (!datos) return { gruposIngreso: [], gruposGasto: [], totalIngresos: 0, totalGastos: 0, sinCuenta: { n: 0, importe: 0 } };
    const filtrar = (lista) => (anioActivo ? lista.filter((m) => anioDe(m) === anioActivo) : lista);
    const ingresos = filtrar(datos.ingresos);
    const gastos = filtrar(datos.gastos);
    const sin = [...ingresos, ...gastos].filter((m) => !m.cuenta);
    return {
      gruposIngreso: agrupar(ingresos),
      gruposGasto: agrupar(gastos),
      totalIngresos: ingresos.reduce((s, m) => s + num(m.monto), 0),
      totalGastos: gastos.reduce((s, m) => s + num(m.monto), 0),
      sinCuenta: { n: sin.length, importe: sin.reduce((s, m) => s + num(m.monto), 0) },
    };
  }, [datos, anioActivo]);

  const descargar = () => {
    const filas = [
      ...gruposIngreso.map((g) => ({ tipo: 'Ingreso', ...g })),
      ...gruposGasto.map((g) => ({ tipo: 'Gasto', ...g })),
    ];
    descargarCsv(
      `resumen-por-cuenta${anioActivo ? `_${anioActivo}` : ''}.csv`,
      [
        { titulo: 'Tipo', valor: (f) => f.tipo },
        { titulo: 'Cuenta', valor: (f) => f.codigo },
        { titulo: 'Nombre de la cuenta', valor: (f) => (f.codigo ? f.nombre || 'Cuenta propia' : 'Sin cuenta asignada') },
        { titulo: 'Movimientos', valor: (f) => f.movimientos.length },
        { titulo: 'Importe (€)', valor: (f) => Math.round(f.total * 100) / 100 },
      ],
      filas
    );
    toast('Resumen descargado', 'success');
  };

  if (error) {
    return <div className="card"><EstadoVacio icono="alert-circle" titulo="No se pudo cargar el resumen" texto="Inténtalo de nuevo en unos segundos." /></div>;
  }
  if (!datos) return <Cargando texto="Cargando resumen..." />;

  const hayDatos = datos.ingresos.length + datos.gastos.length > 0;
  const resultado = totalIngresos - totalGastos;

  return (
    <div className="fade-in">
      <PageHeader
        eyebrow="Área económica"
        titulo="Resumen por cuenta"
        subtitulo="Ingresos y gastos agrupados por cuenta contable. Pulsa una cuenta para ver sus movimientos."
        acciones={
          <>
            <div className="field field-inline">
              <label htmlFor="anio-cuentas">Año</label>
              <select id="anio-cuentas" value={anioActivo} onChange={(e) => setAnio(Number(e.target.value))}>
                <option value={0}>Todos los años</option>
                {anios.map((a) => <option key={a} value={a}>{a}</option>)}
              </select>
            </div>
            <button type="button" className="btn btn-secondary" onClick={descargar} disabled={!hayDatos}>
              <Icon name="download" size={16} /> Descargar CSV
            </button>
          </>
        }
      />

      {!hayDatos ? (
        <div className="card">
          <EstadoVacio icono="wallet" titulo="Aún no hay movimientos" texto="Cuando registres ingresos y gastos con su cuenta contable, aparecerán agrupados aquí." />
        </div>
      ) : (
        <>
          {sinCuenta.n > 0 && (
            <div className="alert alert-warn" role="status">
              <Icon name="info" size={18} />
              <span>
                {sinCuenta.n === 1 ? 'Hay 1 movimiento' : `Hay ${sinCuenta.n} movimientos`} sin cuenta contable ({formatoEuro(sinCuenta.importe)}).
                Puedes asignarla desde <Link to={`/area-economica/${id}`}>el Área económica</Link>, con el botón de editar.
              </span>
            </div>
          )}

          <div className="resumen-cuentas">
            <TablaCuentas titulo="Ingresos" icono="trending-up" claseTotal="text-positive" grupos={gruposIngreso} total={totalIngresos} vacio="No hay ingresos en este periodo." />
            <TablaCuentas titulo="Gastos" icono="trending-down" claseTotal="text-negative" grupos={gruposGasto} total={totalGastos} vacio="No hay gastos en este periodo." />
          </div>

          <div className="card card-pad resumen-resultado">
            <span>Resultado del periodo (ingresos − gastos)</span>
            <strong className={resultado >= 0 ? 'text-positive' : 'text-negative'}>{formatoEuro(resultado)}</strong>
          </div>
        </>
      )}
    </div>
  );
};

export default ResumenCuentas;
