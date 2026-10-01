import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import PageHeader from '../components/PageHeader';
import { Cargando, EstadoVacio } from '../components/Estados';
import { formatoEuro, formatoFecha } from '../utils/format';

const porFechaDesc = (a, b) => String(b.fecha || '').localeCompare(String(a.fecha || ''));

const EconomicArea = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [ong, setOng] = useState(null);
  const [ingresos, setIngresos] = useState([]);
  const [gastos, setGastos] = useState([]);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    setCargando(true);
    const peticion = (url, guardar, mensaje) =>
      axios.get(url).then((res) => guardar(res.data)).catch((err) => console.error(mensaje, err));

    Promise.all([
      peticion(`http://localhost:8080/api/ongs/${id}`, setOng, 'Error cargando ONG:'),
      peticion(`http://localhost:8080/api/ingresos/ong/${id}`, setIngresos, 'Error cargando ingresos'),
      peticion(`http://localhost:8080/api/gastos/ong/${id}`, setGastos, 'Error cargando gastos'),
    ]).finally(() => setCargando(false));
  }, [id]);

  const totalIngresos = ingresos.reduce((sum, i) => sum + parseFloat(i.monto || 0), 0);
  const totalGastos = gastos.reduce((sum, g) => sum + parseFloat(g.monto || 0), 0);
  const balance = totalIngresos - totalGastos;
  const porcentajeGastado = totalIngresos > 0 ? Math.min(100, Math.round((totalGastos / totalIngresos) * 100)) : 0;

  if (cargando) return <Cargando texto="Cargando datos económicos..." />;

  return (
    <div className="fade-in">
      <PageHeader
        eyebrow={ong ? ong.nombre : 'Área económica'}
        titulo="Área económica"
        subtitulo="Resumen de los ingresos y gastos registrados."
        acciones={
          <>
            <button type="button" className="btn btn-secondary" onClick={() => navigate(`/ongs/${id}/gastos/nuevo`)}>
              <Icon name="plus" size={16} /> Registrar gasto
            </button>
            <button type="button" className="btn btn-primary" onClick={() => navigate(`/ongs/${id}/ingresos/nuevo`)}>
              <Icon name="plus" size={16} /> Registrar ingreso
            </button>
          </>
        }
      />

      {/* ---------- Indicadores ---------- */}
      <div className="kpi-grid">
        <div className="kpi card">
          <span className="kpi-icono kpi-ingresos"><Icon name="trending-up" size={22} /></span>
          <p className="kpi-label">Total ingresos</p>
          <p className="kpi-valor">{formatoEuro(totalIngresos)}</p>
          <p className="kpi-nota">{ingresos.length} {ingresos.length === 1 ? 'movimiento' : 'movimientos'}</p>
        </div>

        <div className="kpi card">
          <span className="kpi-icono kpi-gastos"><Icon name="trending-down" size={22} /></span>
          <p className="kpi-label">Total gastos</p>
          <p className="kpi-valor">{formatoEuro(totalGastos)}</p>
          <p className="kpi-nota">{gastos.length} {gastos.length === 1 ? 'movimiento' : 'movimientos'}</p>
        </div>

        <div className={`kpi kpi-balance card ${balance >= 0 ? 'balance-positivo' : 'balance-negativo'}`}>
          <span className="kpi-icono kpi-saldo"><Icon name="wallet" size={22} /></span>
          <p className="kpi-label">Balance actual</p>
          <p className="kpi-valor">{balance >= 0 ? '+' : ''}{formatoEuro(balance)}</p>
          {totalIngresos > 0 ? (
            <>
              <div className="barra" role="progressbar" aria-valuenow={porcentajeGastado} aria-valuemin="0" aria-valuemax="100">
                <span style={{ width: `${porcentajeGastado}%` }} />
              </div>
              <p className="kpi-nota">Se ha gastado el {porcentajeGastado}% de los ingresos</p>
            </>
          ) : (
            <p className="kpi-nota">Registra un ingreso para ver la evolución</p>
          )}
        </div>
      </div>

      {/* ---------- Accesos rápidos ---------- */}
      <h2 className="seccion-h">Informes y facturas</h2>
      <div className="atajos">
        <button type="button" className="atajo card card-hover" onClick={() => navigate(`/informe/balance/${id}`)}>
          <span className="atajo-icono"><Icon name="pie-chart" size={22} /></span>
          <span className="atajo-texto"><strong>Balance general</strong><small>Activos, pasivos y fondos netos</small></span>
          <Icon name="arrow-right" size={18} className="atajo-flecha" />
        </button>
        <button type="button" className="atajo card card-hover" onClick={() => navigate(`/informe/resultados/${id}`)}>
          <span className="atajo-icono"><Icon name="bar-chart" size={22} /></span>
          <span className="atajo-texto"><strong>Estado de resultados</strong><small>Ingresos y gastos por categoría</small></span>
          <Icon name="arrow-right" size={18} className="atajo-flecha" />
        </button>
        <button type="button" className="atajo card card-hover" onClick={() => navigate(`/ongs/${id}/facturas`)}>
          <span className="atajo-icono"><Icon name="file-text" size={22} /></span>
          <span className="atajo-texto"><strong>Historial de facturas</strong><small>Consulta y descarga en PDF</small></span>
          <Icon name="arrow-right" size={18} className="atajo-flecha" />
        </button>
      </div>

      {/* ---------- Movimientos ---------- */}
      <div className="movimientos">
        <section className="card">
          <div className="card-header">
            <h3><Icon name="trending-up" size={20} className="text-positive" /> Ingresos</h3>
            <span className="badge badge-success">{ingresos.length}</span>
          </div>
          {ingresos.length === 0 ? (
            <EstadoVacio
              icono="trending-up"
              titulo="Sin ingresos todavía"
              texto="Registra el primer ingreso para empezar a ver el resumen."
              accion={<button type="button" className="btn btn-primary btn-sm" onClick={() => navigate(`/ongs/${id}/ingresos/nuevo`)}>Registrar ingreso</button>}
            />
          ) : (
            <div className="table-wrap lista-scroll">
              <table className="table">
                <thead><tr><th>Descripción</th><th>Fecha</th><th className="num">Importe</th></tr></thead>
                <tbody>
                  {[...ingresos].sort(porFechaDesc).map((ingreso) => (
                    <tr key={ingreso.id}>
                      <td>
                        <span className="mov-desc">{ingreso.descripcion || 'Sin descripción'}</span>
                        {ingreso.tipo && <span className="badge badge-success">{ingreso.tipo}</span>}
                      </td>
                      <td className="text-muted">{formatoFecha(ingreso.fecha)}</td>
                      <td className="num text-positive">+{formatoEuro(ingreso.monto)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <section className="card">
          <div className="card-header">
            <h3><Icon name="trending-down" size={20} className="text-negative" /> Gastos</h3>
            <span className="badge badge-accent">{gastos.length}</span>
          </div>
          {gastos.length === 0 ? (
            <EstadoVacio
              icono="trending-down"
              titulo="Sin gastos todavía"
              texto="Cuando registres un gasto aparecerá aquí."
              accion={<button type="button" className="btn btn-secondary btn-sm" onClick={() => navigate(`/ongs/${id}/gastos/nuevo`)}>Registrar gasto</button>}
            />
          ) : (
            <div className="table-wrap lista-scroll">
              <table className="table">
                <thead><tr><th>Descripción</th><th>Fecha</th><th className="num">Importe</th></tr></thead>
                <tbody>
                  {[...gastos].sort(porFechaDesc).map((gasto) => (
                    <tr key={gasto.id}>
                      <td>
                        <span className="mov-desc">{gasto.descripcion || 'Sin descripción'}</span>
                        {gasto.categoria && <span className="badge badge-accent">{gasto.categoria}</span>}
                      </td>
                      <td className="text-muted">{formatoFecha(gasto.fecha)}</td>
                      <td className="num text-negative">-{formatoEuro(gasto.monto)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </div>
  );
};

export default EconomicArea;
