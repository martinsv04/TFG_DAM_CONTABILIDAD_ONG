import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import PageHeader from '../components/PageHeader';
import { Cargando, EstadoVacio } from '../components/Estados';
import GraficoMensual from '../components/GraficoMensual';
import BarraProgreso from '../components/BarraProgreso';
import { formatoEuro, formatoFecha } from '../utils/format';
import { nombreCuenta } from '../utils/cuentas';
import Confirmar from '../components/Confirmar';
import { toast } from '../components/Toast';

const Acciones = ({ nombre, alEditar, alEliminar }) => (
  <td className="num acciones-fila">
    <button type="button" className="btn btn-ghost btn-sm btn-icon" onClick={alEditar} aria-label={`Editar ${nombre}`} title="Editar">
      <Icon name="edit" size={16} />
    </button>
    <button type="button" className="btn btn-ghost btn-sm btn-icon text-negative" onClick={alEliminar} aria-label={`Eliminar ${nombre}`} title="Eliminar">
      <Icon name="trash" size={16} />
    </button>
  </td>
);

const porFechaDesc = (a, b) => String(b.fecha || '').localeCompare(String(a.fecha || ''));

const EconomicArea = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [ong, setOng] = useState(null);
  const [ingresosTodos, setIngresos] = useState([]);
  const [gastosTodos, setGastos] = useState([]);
  const [proyectos, setProyectos] = useState([]);
  const [anioElegido, setAnioElegido] = useState(null); // null = el último año con datos; 0 = todos los años
  const [cargando, setCargando] = useState(true);
  const esAdmin = localStorage.getItem('rol') === 'ADMIN'; // solo el administrador edita y borra movimientos
  const [aEliminar, setAEliminar] = useState(null); // { tipo: 'ingreso' | 'gasto', mov }
  const [eliminando, setEliminando] = useState(false);

  useEffect(() => {
    setCargando(true);
    const peticion = (url, guardar, mensaje) =>
      axios.get(url).then((res) => guardar(res.data)).catch((err) => console.error(mensaje, err));

    Promise.all([
      peticion(`http://localhost:8080/api/ongs/${id}`, setOng, 'Error cargando ONG:'),
      peticion(`http://localhost:8080/api/ingresos/ong/${id}`, setIngresos, 'Error cargando ingresos'),
      peticion(`http://localhost:8080/api/gastos/ong/${id}`, setGastos, 'Error cargando gastos'),
      peticion(`http://localhost:8080/api/proyectos/ong/${id}`, setProyectos, 'Error cargando proyectos'),
    ]).finally(() => setCargando(false));
  }, [id]);

  // Filtro de año: afecta a los indicadores, el gráfico y las listas
  const anioDeMovimiento = (m) => Number(String(m.fecha || '').slice(0, 4));
  const aniosConDatos = Array.from(new Set([...ingresosTodos, ...gastosTodos].map(anioDeMovimiento).filter(Boolean))).sort((a, b) => b - a);
  const aniosSelector = Array.from(new Set([new Date().getFullYear(), ...aniosConDatos])).sort((a, b) => b - a);
  const anioActivo = anioElegido === null ? (aniosConDatos[0] || new Date().getFullYear()) : anioElegido;
  const anioGrafico = anioActivo || aniosConDatos[0] || new Date().getFullYear();
  const ingresos = anioActivo ? ingresosTodos.filter((m) => anioDeMovimiento(m) === anioActivo) : ingresosTodos;
  const gastos = anioActivo ? gastosTodos.filter((m) => anioDeMovimiento(m) === anioActivo) : gastosTodos;

  const totalIngresos = ingresos.reduce((sum, i) => sum + parseFloat(i.monto || 0), 0);
  const totalGastos = gastos.reduce((sum, g) => sum + parseFloat(g.monto || 0), 0);
  const balance = totalIngresos - totalGastos;
  const porcentajeGastado = totalIngresos > 0 ? Math.min(100, Math.round((totalGastos / totalIngresos) * 100)) : 0;

  const porMes = Array.from({ length: 12 }, () => ({ ingresos: 0, gastos: 0 }));
  ingresos.forEach((i) => {
    if (String(i.fecha || '').startsWith(String(anioGrafico))) porMes[Number(String(i.fecha).slice(5, 7)) - 1].ingresos += parseFloat(i.monto || 0);
  });
  gastos.forEach((g) => {
    if (String(g.fecha || '').startsWith(String(anioGrafico))) porMes[Number(String(g.fecha).slice(5, 7)) - 1].gastos += parseFloat(g.monto || 0);
  });
  const hayMovimientos = porMes.some((m) => m.ingresos > 0 || m.gastos > 0);
  const proyectosConPresupuesto = proyectos.filter((p) => p.presupuesto != null && Number(p.presupuesto) > 0 && p.estado !== 'FINALIZADO');

  const eliminar = async () => {
    if (!aEliminar) return;
    const { tipo, mov } = aEliminar;
    setEliminando(true);
    try {
      await axios.delete(`http://localhost:8080/api/${tipo === 'ingreso' ? 'ingresos' : 'gastos'}/${mov.id}`);
      if (tipo === 'ingreso') setIngresos((lista) => lista.filter((i) => i.id !== mov.id));
      else setGastos((lista) => lista.filter((g) => g.id !== mov.id));
      toast(tipo === 'ingreso' ? 'Ingreso eliminado' : 'Gasto eliminado', 'success');
      setAEliminar(null);
    } catch (err) {
      console.error('Error al eliminar:', err);
      toast(err.response?.data?.message || 'No se pudo eliminar el movimiento', 'error');
    } finally {
      setEliminando(false);
    }
  };

  if (cargando) return <Cargando texto="Cargando datos económicos..." />;

  return (
    <div className="fade-in">
      <PageHeader
        eyebrow={ong ? ong.nombre : 'Área económica'}
        titulo="Área económica"
        subtitulo={anioActivo ? `Ingresos y gastos registrados en ${anioActivo}.` : 'Ingresos y gastos registrados de todos los años.'}
        acciones={
          <>
            <div className="field field-inline">
              <label htmlFor="anio-area">Año</label>
              <select id="anio-area" value={anioActivo} onChange={(e) => setAnioElegido(Number(e.target.value))}>
                <option value={0}>Todos los años</option>
                {aniosSelector.map((a) => <option key={a} value={a}>{a}</option>)}
              </select>
            </div>
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

      {/* ---------- Gráficos ---------- */}
      <div className={`graficos ${proyectosConPresupuesto.length === 0 ? 'graficos-uno' : ''}`}>
        <section className="card card-pad">
          <div className="grafico-cabecera">
            <h3>Ingresos y gastos por mes · {anioGrafico}</h3>
          </div>
          {hayMovimientos ? (
            <GraficoMensual datos={porMes} anio={anioGrafico} />
          ) : (
            <p className="text-muted">No hay movimientos en {anioGrafico}.</p>
          )}
        </section>

        {proyectosConPresupuesto.length > 0 && (
          <section className="card card-pad">
            <div className="grafico-cabecera">
              <h3>Ejecución de proyectos</h3>
              <button type="button" className="btn btn-ghost btn-sm" onClick={() => navigate(`/ongs/${id}/proyectos`)}>Ver proyectos</button>
            </div>
            <ul className="ejecucion-lista">
              {proyectosConPresupuesto.map((p) => (
                <li key={p.id}>
                  <div className="ejecucion-fila">
                    <strong>{p.nombre}</strong>
                    <span className="text-muted">{formatoEuro(p.gastado)} de {formatoEuro(p.presupuesto)}</span>
                  </div>
                  <BarraProgreso porcentaje={p.porcentajeEjecutado} etiqueta={`Ejecución de ${p.nombre}`} />
                </li>
              ))}
            </ul>
          </section>
        )}
      </div>

      {/* ---------- Accesos rápidos ---------- */}
      <h2 className="seccion-h">Proyectos y subvenciones</h2>
      <div className="atajos">
        <button type="button" className="atajo card card-hover" onClick={() => navigate(`/ongs/${id}/proyectos`)}>
          <span className="atajo-icono"><Icon name="folder" size={22} /></span>
          <span className="atajo-texto"><strong>Proyectos</strong><small>Presupuesto y gastos por iniciativa</small></span>
          <Icon name="arrow-right" size={18} className="atajo-flecha" />
        </button>
        <button type="button" className="atajo card card-hover" onClick={() => navigate(`/ongs/${id}/subvenciones`)}>
          <span className="atajo-icono"><Icon name="landmark" size={22} /></span>
          <span className="atajo-texto"><strong>Subvenciones</strong><small>Concesión, cobro y justificación</small></span>
          <Icon name="arrow-right" size={18} className="atajo-flecha" />
        </button>
      </div>

      <h2 className="seccion-h">Informes, facturas y certificados</h2>
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
        <button type="button" className="atajo card card-hover" onClick={() => navigate(`/ongs/${id}/certificados`)}>
          <span className="atajo-icono"><Icon name="award" size={22} /></span>
          <span className="atajo-texto"><strong>Certificados de donación</strong><small>Para que los donantes desgraven</small></span>
          <Icon name="arrow-right" size={18} className="atajo-flecha" />
        </button>
        <button type="button" className="atajo card card-hover" onClick={() => navigate(`/ongs/${id}/cuentas`)}>
          <span className="atajo-icono"><Icon name="book" size={22} /></span>
          <span className="atajo-texto"><strong>Resumen por cuenta</strong><small>Totales por cuenta contable</small></span>
          <Icon name="arrow-right" size={18} className="atajo-flecha" />
        </button>
        <button type="button" className="atajo card card-hover" onClick={() => navigate(`/ongs/${id}/transparencia`)}>
          <span className="atajo-icono"><Icon name="eye" size={22} /></span>
          <span className="atajo-texto"><strong>Transparencia</strong><small>Financiación, gastos por destino y página pública</small></span>
          <Icon name="arrow-right" size={18} className="atajo-flecha" />
        </button>
        <button type="button" className="atajo card card-hover" onClick={() => navigate(`/ongs/${id}/exportar`)}>
          <span className="atajo-icono"><Icon name="download" size={22} /></span>
          <span className="atajo-texto"><strong>Exportar datos</strong><small>Ingresos, gastos, facturas... en Excel (CSV)</small></span>
          <Icon name="arrow-right" size={18} className="atajo-flecha" />
        </button>
        <button type="button" className="atajo card card-hover" onClick={() => navigate(`/ongs/${id}/modelo-182`)}>
          <span className="atajo-icono"><Icon name="briefcase" size={22} /></span>
          <span className="atajo-texto"><strong>Modelo 182</strong><small>Declaración anual de donativos a Hacienda</small></span>
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
                <thead><tr><th>Descripción</th><th>Fecha</th><th className="num">Importe</th>{esAdmin && <th className="num"><span className="sr-only">Acciones</span></th>}</tr></thead>
                <tbody>
                  {[...ingresos].sort(porFechaDesc).map((ingreso) => (
                    <tr key={ingreso.id}>
                      <td>
                        <span className="mov-desc">{ingreso.descripcion || 'Sin descripción'}</span>
                        {ingreso.tipo && <span className="badge badge-success">{ingreso.tipo}</span>}
                        {ingreso.cuenta && <span className="badge" title={nombreCuenta(ingreso.cuenta) || 'Cuenta contable'}>{ingreso.cuenta}</span>}
                      </td>
                      <td className="text-muted">{formatoFecha(ingreso.fecha)}</td>
                      <td className="num text-positive">+{formatoEuro(ingreso.monto)}</td>
                      {esAdmin && (
                        <Acciones
                          nombre={ingreso.descripcion || 'ingreso'}
                          alEditar={() => navigate(`/ongs/${id}/ingresos/${ingreso.id}/editar`)}
                          alEliminar={() => setAEliminar({ tipo: 'ingreso', mov: ingreso })}
                        />
                      )}
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
                <thead><tr><th>Descripción</th><th>Fecha</th><th className="num">Importe</th>{esAdmin && <th className="num"><span className="sr-only">Acciones</span></th>}</tr></thead>
                <tbody>
                  {[...gastos].sort(porFechaDesc).map((gasto) => (
                    <tr key={gasto.id}>
                      <td>
                        <span className="mov-desc">{gasto.descripcion || 'Sin descripción'}</span>
                        {gasto.categoria && <span className="badge badge-accent">{gasto.categoria}</span>}
                        {gasto.cuenta && <span className="badge" title={nombreCuenta(gasto.cuenta) || 'Cuenta contable'}>{gasto.cuenta}</span>}
                      </td>
                      <td className="text-muted">{formatoFecha(gasto.fecha)}</td>
                      <td className="num text-negative">-{formatoEuro(gasto.monto)}</td>
                      {esAdmin && (
                        <Acciones
                          nombre={gasto.descripcion || 'gasto'}
                          alEditar={() => navigate(`/ongs/${id}/gastos/${gasto.id}/editar`)}
                          alEliminar={() => setAEliminar({ tipo: 'gasto', mov: gasto })}
                        />
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>

      {aEliminar && (
        <Confirmar
          titulo={aEliminar.tipo === 'ingreso' ? '¿Eliminar este ingreso?' : '¿Eliminar este gasto?'}
          texto={`«${aEliminar.mov.descripcion || 'Sin descripción'}» por ${formatoEuro(aEliminar.mov.monto)} dejará de contar en los totales. Si ya tiene una factura, esta se conserva pero queda sin movimiento asociado. La acción quedará anotada en el registro de actividad.`}
          enviando={eliminando}
          alConfirmar={eliminar}
          alCerrar={() => !eliminando && setAEliminar(null)}
        />
      )}
    </div>
  );
};

export default EconomicArea;
