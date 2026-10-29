import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import PageHeader from '../components/PageHeader';
import BarraProgreso from '../components/BarraProgreso';
import FormularioProyecto from '../components/FormularioProyecto';
import { Cargando, EstadoVacio } from '../components/Estados';
import { toast } from '../components/Toast';
import { formatoEuro, formatoFecha } from '../utils/format';
import { ESTADOS_PROYECTO } from '../utils/proyectos';

const API = 'http://localhost:8080/api';

// Proyectos de una ONG con su presupuesto y lo gastado hasta ahora
const Proyectos = () => {
  const { id } = useParams(); // ID de la ONG
  const navigate = useNavigate();
  const [proyectos, setProyectos] = useState(null);
  const [creando, setCreando] = useState(false);

  const cargar = useCallback(() => {
    axios
      .get(`${API}/proyectos/ong/${id}`)
      .then((res) => setProyectos(res.data))
      .catch(() => {
        toast('No se pudieron cargar los proyectos', 'error');
        setProyectos([]);
      });
  }, [id]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const nuevo = (
    <button type="button" className="btn btn-primary" onClick={() => setCreando(true)}>
      <Icon name="plus" size={16} /> Nuevo proyecto
    </button>
  );

  return (
    <div className="fade-in">
      <PageHeader
        eyebrow="Gestión"
        titulo="Proyectos"
        subtitulo="Agrupa los gastos e ingresos de cada iniciativa y controla su presupuesto."
        acciones={nuevo}
      />

      {!proyectos ? (
        <Cargando texto="Cargando proyectos..." />
      ) : proyectos.length === 0 ? (
        <section className="card">
          <EstadoVacio
            icono="folder"
            titulo="Todavía no hay proyectos"
            texto="Crea un proyecto y asígnale gastos e ingresos al registrarlos para ver cuánto lleváis ejecutado."
            accion={nuevo}
          />
        </section>
      ) : (
        <div className="proyectos-grid">
          {proyectos.map((p) => {
            const estado = ESTADOS_PROYECTO[p.estado] || { texto: p.estado, badge: '' };
            return (
              <article key={p.id} className="card card-hover proyecto-card" tabIndex={0} role="link"
                onClick={() => navigate(`/ongs/${id}/proyectos/${p.id}`)}
                onKeyDown={(e) => e.key === 'Enter' && navigate(`/ongs/${id}/proyectos/${p.id}`)}>
                <div className="proyecto-cabecera">
                  <h3>{p.nombre}</h3>
                  <span className={`badge ${estado.badge}`}>{estado.texto}</span>
                </div>
                {p.descripcion && <p className="text-muted proyecto-desc">{p.descripcion}</p>}
                <p className="text-muted proyecto-fechas">
                  <Icon name="calendar" size={14} />{' '}
                  {p.fechaInicio || p.fechaFin
                    ? `${p.fechaInicio ? formatoFecha(p.fechaInicio) : '…'} – ${p.fechaFin ? formatoFecha(p.fechaFin) : '…'}`
                    : 'Sin fechas'}
                </p>
                <BarraProgreso porcentaje={p.porcentajeEjecutado} etiqueta={`Ejecución de ${p.nombre}`} />
                <dl className="proyecto-cifras">
                  <div><dt>Presupuesto</dt><dd>{p.presupuesto != null ? formatoEuro(p.presupuesto) : '—'}</dd></div>
                  <div><dt>Gastado</dt><dd className={p.superaPresupuesto ? 'text-negative' : ''}>{formatoEuro(p.gastado)}</dd></div>
                  <div><dt>Ingresado</dt><dd>{formatoEuro(p.ingresado)}</dd></div>
                </dl>
                {p.superaPresupuesto && <span className="badge badge-danger">Presupuesto superado</span>}
              </article>
            );
          })}
        </div>
      )}

      {creando && (
        <FormularioProyecto
          idOng={id}
          alCerrar={() => setCreando(false)}
          alGuardar={() => {
            setCreando(false);
            toast('Proyecto creado', 'success');
            cargar();
          }}
        />
      )}
    </div>
  );
};

export default Proyectos;
