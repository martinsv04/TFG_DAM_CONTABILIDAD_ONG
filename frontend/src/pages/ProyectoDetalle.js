import React, { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import PageHeader from '../components/PageHeader';
import BarraProgreso from '../components/BarraProgreso';
import Confirmar from '../components/Confirmar';
import FormularioProyecto from '../components/FormularioProyecto';
import { Cargando, EstadoVacio } from '../components/Estados';
import { toast } from '../components/Toast';
import { getRol } from '../services/authService';
import { formatoEuro, formatoFecha } from '../utils/format';
import { ESTADOS_PROYECTO, ESTADOS_SUBVENCION } from '../utils/proyectos';

const API = 'http://localhost:8080/api';

const TablaMovimientos = ({ titulo, icono, claseIcono, movimientos, vacio }) => (
  <section className="card">
    <div className="card-header">
      <h3><Icon name={icono} size={20} className={claseIcono} /> {titulo}</h3>
      <span className="badge">{movimientos.length}</span>
    </div>
    {movimientos.length === 0 ? (
      <p className="text-muted card-pad">{vacio}</p>
    ) : (
      <div className="table-wrap">
        <table className="table">
          <thead>
            <tr><th>Fecha</th><th>Descripción</th><th>Concepto</th><th className="num">Importe</th></tr>
          </thead>
          <tbody>
            {movimientos.map((m) => (
              <tr key={m.id}>
                <td className="text-muted">{formatoFecha(m.fecha)}</td>
                <td>{m.descripcion}</td>
                <td><span className="badge">{m.concepto}</span></td>
                <td className="num">{formatoEuro(m.monto)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    )}
  </section>
);

// Detalle de un proyecto: presupuesto, ejecución y los gastos e ingresos que se le han asignado
const ProyectoDetalle = () => {
  const { id, proyectoId } = useParams();
  const navigate = useNavigate();
  const esAdmin = getRol() === 'ADMIN';
  const [detalle, setDetalle] = useState(null);
  const [editando, setEditando] = useState(false);
  const [borrando, setBorrando] = useState(false);
  const [enviando, setEnviando] = useState(false);

  const cargar = useCallback(() => {
    axios
      .get(`${API}/proyectos/ong/${id}/${proyectoId}`)
      .then((res) => setDetalle(res.data))
      .catch(() => {
        toast('No se pudo cargar el proyecto', 'error');
        navigate(`/ongs/${id}/proyectos`);
      });
  }, [id, proyectoId, navigate]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const eliminar = async () => {
    setEnviando(true);
    try {
      await axios.delete(`${API}/proyectos/ong/${id}/${proyectoId}`);
      toast('Proyecto eliminado', 'success');
      navigate(`/ongs/${id}/proyectos`);
    } catch (err) {
      toast(err.response?.data?.message || 'No se pudo eliminar el proyecto', 'error');
      setEnviando(false);
      setBorrando(false);
    }
  };

  if (!detalle) return <Cargando texto="Cargando proyecto..." />;

  const p = detalle.proyecto;
  const estado = ESTADOS_PROYECTO[p.estado] || { texto: p.estado, badge: '' };

  return (
    <div className="fade-in">
      <PageHeader
        eyebrow={<Link to={`/ongs/${id}/proyectos`}>Proyectos</Link>}
        titulo={p.nombre}
        subtitulo={p.descripcion}
        acciones={
          esAdmin && (
            <>
              <button type="button" className="btn btn-secondary" onClick={() => setEditando(true)}>
                <Icon name="edit" size={16} /> Editar
              </button>
              <button type="button" className="btn btn-danger" onClick={() => setBorrando(true)}>
                <Icon name="trash" size={16} /> Eliminar
              </button>
            </>
          )
        }
      />

      <div className="proyecto-meta">
        <span className={`badge ${estado.badge}`}>{estado.texto}</span>
        <span className="text-muted">
          <Icon name="calendar" size={14} />{' '}
          {p.fechaInicio || p.fechaFin
            ? `${p.fechaInicio ? formatoFecha(p.fechaInicio) : '…'} – ${p.fechaFin ? formatoFecha(p.fechaFin) : '…'}`
            : 'Sin fechas'}
        </span>
      </div>

      <section className="card card-pad proyecto-ejecucion">
        <div className="proyecto-ejecucion-barra">
          <strong>Ejecución del presupuesto</strong>
          <BarraProgreso porcentaje={p.porcentajeEjecutado} etiqueta="Ejecución del presupuesto" />
        </div>
        {p.superaPresupuesto && (
          <div className="alert alert-warn" role="alert">
            <Icon name="alert-circle" size={18} />
            <span>Los gastos superan el presupuesto en {formatoEuro(Number(p.gastado) - Number(p.presupuesto))}.</span>
          </div>
        )}
        <div className="kpi-grid kpi-grid-4">
          <div className="kpi-mini"><span className="kpi-label">Presupuesto</span><strong>{p.presupuesto != null ? formatoEuro(p.presupuesto) : '—'}</strong></div>
          <div className="kpi-mini"><span className="kpi-label">Gastado</span><strong>{formatoEuro(p.gastado)}</strong></div>
          <div className="kpi-mini"><span className="kpi-label">Ingresado</span><strong>{formatoEuro(p.ingresado)}</strong></div>
          <div className={`kpi-mini ${Number(p.resultado) >= 0 ? 'balance-positivo' : 'balance-negativo'}`}>
            <span className="kpi-label">Resultado</span><strong className="resultado-valor">{formatoEuro(p.resultado)}</strong>
          </div>
        </div>
      </section>

      {detalle.subvenciones.length > 0 && (
        <section className="card card-pad" style={{ marginTop: '1.25rem' }}>
          <h3 className="seccion-sub">Subvenciones que lo financian</h3>
          <ul className="lista-simple">
            {detalle.subvenciones.map((s) => {
              const e = ESTADOS_SUBVENCION[s.estado] || { texto: s.estado, badge: '' };
              return (
                <li key={s.id}>
                  <span><strong>{s.nombre}</strong> <span className="text-muted">{s.financiador}</span></span>
                  <span><span className={`badge ${e.badge}`}>{e.texto}</span> <strong>{s.importeConcedido != null ? formatoEuro(s.importeConcedido) : '—'}</strong></span>
                </li>
              );
            })}
          </ul>
        </section>
      )}

      <div className="movimientos" style={{ marginTop: '1.25rem' }}>
        <TablaMovimientos titulo="Gastos" icono="trending-down" claseIcono="text-negative" movimientos={detalle.gastos}
          vacio="Aún no hay gastos asignados. Elige este proyecto al registrar un gasto." />
        <TablaMovimientos titulo="Ingresos" icono="trending-up" claseIcono="text-positive" movimientos={detalle.ingresos}
          vacio="Aún no hay ingresos asignados. Elige este proyecto al registrar un ingreso." />
      </div>

      {editando && (
        <FormularioProyecto
          idOng={id}
          proyecto={p}
          alCerrar={() => setEditando(false)}
          alGuardar={() => {
            setEditando(false);
            toast('Proyecto actualizado', 'success');
            cargar();
          }}
        />
      )}
      {borrando && (
        <Confirmar
          titulo="¿Eliminar este proyecto?"
          texto="Los gastos e ingresos asignados no se borran: solo dejan de estar asociados al proyecto."
          enviando={enviando}
          alConfirmar={eliminar}
          alCerrar={() => setBorrando(false)}
        />
      )}
    </div>
  );
};

export default ProyectoDetalle;
