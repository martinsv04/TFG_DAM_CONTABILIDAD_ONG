import React, { useCallback, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import PageHeader from '../components/PageHeader';
import BarraProgreso from '../components/BarraProgreso';
import Confirmar from '../components/Confirmar';
import FormularioSubvencion from '../components/FormularioSubvencion';
import { Cargando, EstadoVacio } from '../components/Estados';
import { toast } from '../components/Toast';
import { getRol } from '../services/authService';
import { formatoEuro, formatoFecha } from '../utils/format';
import { ESTADOS_SUBVENCION } from '../utils/proyectos';

const API = 'http://localhost:8080/api';

// Aviso sobre el plazo de justificación de una subvención concedida
const Plazo = ({ s }) => {
  if (s.fechaJustificacion == null) return <span className="text-muted">—</span>;
  if (s.diasParaJustificar == null) return <span className="text-muted">{formatoFecha(s.fechaJustificacion)}</span>;
  if (s.justificacionVencida) {
    return <span className="badge badge-danger" title={formatoFecha(s.fechaJustificacion)}>Vencido hace {Math.abs(s.diasParaJustificar)} d</span>;
  }
  const clase = s.diasParaJustificar <= 30 ? 'badge-accent' : '';
  return <span className={`badge ${clase}`} title={formatoFecha(s.fechaJustificacion)}>{s.diasParaJustificar === 0 ? 'Vence hoy' : `Faltan ${s.diasParaJustificar} d`}</span>;
};

// Subvenciones de una ONG: estado, importes y cuánto se ha ejecutado de lo concedido
const Subvenciones = () => {
  const { id } = useParams(); // ID de la ONG
  const esAdmin = getRol() === 'ADMIN';
  const [subvenciones, setSubvenciones] = useState(null);
  const [proyectos, setProyectos] = useState([]);
  const [formulario, setFormulario] = useState(null); // 'nueva' o la subvención que se edita
  const [borrando, setBorrando] = useState(null);
  const [enviando, setEnviando] = useState(false);

  const cargar = useCallback(() => {
    Promise.all([axios.get(`${API}/subvenciones/ong/${id}`), axios.get(`${API}/proyectos/ong/${id}`)])
      .then(([s, p]) => {
        setSubvenciones(s.data);
        setProyectos(p.data);
      })
      .catch(() => {
        toast('No se pudieron cargar las subvenciones', 'error');
        setSubvenciones([]);
      });
  }, [id]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const eliminar = async () => {
    setEnviando(true);
    try {
      await axios.delete(`${API}/subvenciones/ong/${id}/${borrando.id}`);
      toast('Subvención eliminada', 'success');
      setBorrando(null);
      cargar();
    } catch (err) {
      toast(err.response?.data?.message || 'No se pudo eliminar la subvención', 'error');
    } finally {
      setEnviando(false);
    }
  };

  const lista = subvenciones || [];
  const concedidas = lista.filter((s) => s.estado === 'CONCEDIDA' || s.estado === 'JUSTIFICADA');
  const totalConcedido = concedidas.reduce((suma, s) => suma + Number(s.importeConcedido || 0), 0);
  const totalCobrado = lista.reduce((suma, s) => suma + Number(s.cobrado || 0), 0);
  const pendientes = lista.filter((s) => s.estado === 'CONCEDIDA').length;

  const nueva = (
    <button type="button" className="btn btn-primary" onClick={() => setFormulario('nueva')}>
      <Icon name="plus" size={16} /> Nueva subvención
    </button>
  );

  return (
    <div className="fade-in">
      <PageHeader
        eyebrow="Gestión"
        titulo="Subvenciones"
        subtitulo="Seguimiento de lo solicitado, lo concedido y su justificación ante el financiador."
        acciones={nueva}
      />

      {!subvenciones ? (
        <Cargando texto="Cargando subvenciones..." />
      ) : (
        <>
          {lista.length > 0 && (
            <div className="resumen-182">
              <div className="card card-pad"><span className="text-muted">Concedido</span><strong>{formatoEuro(totalConcedido)}</strong><small>{concedidas.length} subvención(es)</small></div>
              <div className="card card-pad"><span className="text-muted">Cobrado</span><strong>{formatoEuro(totalCobrado)}</strong><small>ingresos asignados a subvenciones</small></div>
              <div className="card card-pad"><span className="text-muted">Pendientes de justificar</span><strong>{pendientes}</strong><small>estado «Concedida»</small></div>
            </div>
          )}

          <section className="card">
            {lista.length === 0 ? (
              <EstadoVacio
                icono="landmark"
                titulo="Todavía no hay subvenciones"
                texto="Registra las que solicitéis y, cuando os las concedan, asigna los ingresos y el proyecto que financian."
                accion={nueva}
              />
            ) : (
              <div className="table-wrap">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Subvención</th>
                      <th>Estado</th>
                      <th className="num">Concedido</th>
                      <th className="num">Cobrado</th>
                      <th className="col-ejecucion">Ejecución</th>
                      <th>Justificar</th>
                      <th aria-label="Acciones" />
                    </tr>
                  </thead>
                  <tbody>
                    {lista.map((s) => {
                      const e = ESTADOS_SUBVENCION[s.estado] || { texto: s.estado, badge: '' };
                      return (
                        <tr key={s.id}>
                          <td>
                            <strong>{s.nombre}</strong>
                            <div className="text-muted celda-sub">
                              {[s.financiador, s.expediente && `Exp. ${s.expediente}`].filter(Boolean).join(' · ') || '—'}
                            </div>
                            {s.proyectoNombre && (
                              <div className="text-muted celda-sub celda-proyecto"><Icon name="folder" size={13} /> {s.proyectoNombre}</div>
                            )}
                          </td>
                          <td><span className={`badge ${e.badge}`}>{e.texto}</span></td>
                          <td className="num">{s.importeConcedido != null ? formatoEuro(s.importeConcedido) : <span className="text-muted">{s.importeSolicitado != null ? `Pedido ${formatoEuro(s.importeSolicitado)}` : '—'}</span>}</td>
                          <td className="num">{formatoEuro(s.cobrado)}</td>
                          <td className="col-ejecucion">
                            {s.proyectoId ? (
                              <>
                                <BarraProgreso porcentaje={s.porcentajeEjecutado} etiqueta={`Ejecución de ${s.nombre}`} />
                                <small className="text-muted">Gastado {formatoEuro(s.gastado)}</small>
                              </>
                            ) : (
                              <span className="text-muted">Sin proyecto vinculado</span>
                            )}
                          </td>
                          <td><Plazo s={s} /></td>
                          <td className="acciones">
                            {esAdmin && (
                              <>
                                <button type="button" className="btn btn-ghost btn-icon" aria-label={`Editar ${s.nombre}`} onClick={() => setFormulario(s)}><Icon name="edit" size={16} /></button>
                                <button type="button" className="btn btn-ghost btn-icon" aria-label={`Eliminar ${s.nombre}`} onClick={() => setBorrando(s)}><Icon name="trash" size={16} /></button>
                              </>
                            )}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </>
      )}

      {formulario && (
        <FormularioSubvencion
          idOng={id}
          subvencion={formulario === 'nueva' ? null : formulario}
          proyectos={proyectos}
          alCerrar={() => setFormulario(null)}
          alGuardar={() => {
            toast(formulario === 'nueva' ? 'Subvención creada' : 'Subvención actualizada', 'success');
            setFormulario(null);
            cargar();
          }}
        />
      )}
      {borrando && (
        <Confirmar
          titulo="¿Eliminar esta subvención?"
          texto="Los ingresos asignados a ella no se borran: solo dejan de estar asociados."
          enviando={enviando}
          alConfirmar={eliminar}
          alCerrar={() => setBorrando(null)}
        />
      )}
    </div>
  );
};

export default Subvenciones;
