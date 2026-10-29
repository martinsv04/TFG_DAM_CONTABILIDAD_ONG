import React, { useCallback, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import PageHeader from '../components/PageHeader';
import { Cargando, EstadoVacio } from '../components/Estados';
import { toast } from '../components/Toast';
import { ETIQUETA_ROL } from '../utils/format';

const API = 'http://localhost:8080/api';
const TAMANO = 20;

const ENTIDADES = {
  INGRESO: 'Ingreso',
  GASTO: 'Gasto',
  FACTURA: 'Factura',
  PROYECTO: 'Proyecto',
  SUBVENCION: 'Subvención',
};

const ACCIONES = {
  CREAR: { texto: 'Creó', badge: 'badge-success' },
  EDITAR: { texto: 'Editó', badge: 'badge-info' },
  ELIMINAR: { texto: 'Eliminó', badge: 'badge-danger' },
};

const formatoFechaHora = (valor) => {
  const fecha = new Date(valor);
  if (Number.isNaN(fecha.getTime())) return String(valor || '');
  return fecha.toLocaleString('es-ES', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
};

// Registro de actividad: quién ha creado, editado o eliminado datos de la ONG (solo administrador)
const Auditoria = () => {
  const { id } = useParams(); // ID de la ONG
  const [filtros, setFiltros] = useState({ entidad: '', accion: '', desde: '', hasta: '' });
  const [pagina, setPagina] = useState(0);
  const [datos, setDatos] = useState(null);

  const cargar = useCallback(() => {
    const params = new URLSearchParams({ pagina, tamano: TAMANO });
    Object.entries(filtros).forEach(([clave, valor]) => valor && params.set(clave, valor));
    axios
      .get(`${API}/auditoria/ong/${id}?${params.toString()}`)
      .then((res) => setDatos(res.data))
      .catch(() => {
        toast('No se pudo cargar el registro de actividad', 'error');
        setDatos({ contenido: [], total: 0, pagina: 0, paginas: 0 });
      });
  }, [id, filtros, pagina]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const cambiarFiltro = (e) => {
    setPagina(0);
    setDatos(null);
    setFiltros({ ...filtros, [e.target.name]: e.target.value });
  };

  const hayFiltros = Object.values(filtros).some(Boolean);
  const limpiar = () => {
    setPagina(0);
    setDatos(null);
    setFiltros({ entidad: '', accion: '', desde: '', hasta: '' });
  };

  return (
    <div className="fade-in">
      <PageHeader
        eyebrow="Seguridad"
        titulo="Registro de actividad"
        subtitulo="Historial de altas, cambios y borrados de ingresos, gastos, facturas, proyectos y subvenciones."
      />

      <section className="card card-pad auditoria-filtros">
        <div className="field">
          <label htmlFor="f-entidad">Elemento</label>
          <select id="f-entidad" name="entidad" value={filtros.entidad} onChange={cambiarFiltro}>
            <option value="">Todos</option>
            {Object.entries(ENTIDADES).map(([valor, texto]) => <option key={valor} value={valor}>{texto}</option>)}
          </select>
        </div>
        <div className="field">
          <label htmlFor="f-accion">Acción</label>
          <select id="f-accion" name="accion" value={filtros.accion} onChange={cambiarFiltro}>
            <option value="">Todas</option>
            {Object.entries(ACCIONES).map(([valor, a]) => <option key={valor} value={valor}>{a.texto}</option>)}
          </select>
        </div>
        <div className="field">
          <label htmlFor="f-desde">Desde</label>
          <input id="f-desde" name="desde" type="date" value={filtros.desde} max={filtros.hasta || undefined} onChange={cambiarFiltro} />
        </div>
        <div className="field">
          <label htmlFor="f-hasta">Hasta</label>
          <input id="f-hasta" name="hasta" type="date" value={filtros.hasta} min={filtros.desde || undefined} onChange={cambiarFiltro} />
        </div>
        {hayFiltros && (
          <button type="button" className="btn btn-ghost" onClick={limpiar}>Quitar filtros</button>
        )}
      </section>

      <section className="card" style={{ marginTop: '1.25rem' }}>
        {!datos ? (
          <Cargando texto="Cargando actividad..." />
        ) : datos.contenido.length === 0 ? (
          <EstadoVacio
            icono="shield"
            titulo={hayFiltros ? 'Sin resultados con esos filtros' : 'Todavía no hay actividad registrada'}
            texto={hayFiltros ? 'Prueba a ampliar las fechas o a quitar algún filtro.' : 'A partir de ahora quedará anotado quién crea, edita o elimina datos económicos.'}
          />
        ) : (
          <>
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Usuario</th>
                    <th>Acción</th>
                    <th>Elemento</th>
                    <th>Detalle</th>
                  </tr>
                </thead>
                <tbody>
                  {datos.contenido.map((r) => {
                    const accion = ACCIONES[r.accion] || { texto: r.accion, badge: '' };
                    return (
                      <tr key={r.id}>
                        <td className="text-muted nowrap">{formatoFechaHora(r.fecha)}</td>
                        <td>
                          <strong>{r.usuarioNombre || '—'}</strong>
                          {r.usuarioRol && <div className="text-muted celda-sub">{ETIQUETA_ROL[r.usuarioRol] || r.usuarioRol}</div>}
                        </td>
                        <td><span className={`badge ${accion.badge}`}>{accion.texto}</span></td>
                        <td className="nowrap">{ENTIDADES[r.entidad] || r.entidad}{r.entidadId ? <span className="text-muted"> #{r.entidadId}</span> : null}</td>
                        <td className="auditoria-detalle">{r.descripcion}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
            <div className="paginacion">
              <span className="text-muted">
                {datos.total} {datos.total === 1 ? 'registro' : 'registros'} · página {datos.pagina + 1} de {Math.max(datos.paginas, 1)}
              </span>
              <div className="paginacion-botones">
                <button type="button" className="btn btn-secondary btn-sm" disabled={pagina === 0} onClick={() => { setDatos(null); setPagina(pagina - 1); }}>
                  <Icon name="arrow-left" size={14} /> Anterior
                </button>
                <button type="button" className="btn btn-secondary btn-sm" disabled={pagina + 1 >= datos.paginas} onClick={() => { setDatos(null); setPagina(pagina + 1); }}>
                  Siguiente <Icon name="arrow-right" size={14} />
                </button>
              </div>
            </div>
          </>
        )}
      </section>
    </div>
  );
};

export default Auditoria;
