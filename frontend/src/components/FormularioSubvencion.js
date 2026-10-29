import React, { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';
import axios from 'axios';
import { ESTADOS_SUBVENCION, numeroONull, textoONull } from '../utils/proyectos';

const API = 'http://localhost:8080/api';

// Ventana para crear o editar una subvención (subvencion = null para crear). proyectos: lista para vincularla.
const FormularioSubvencion = ({ idOng, subvencion, proyectos, alCerrar, alGuardar }) => {
  const [datos, setDatos] = useState({
    nombre: subvencion?.nombre || '',
    financiador: subvencion?.financiador || '',
    expediente: subvencion?.expediente || '',
    estado: subvencion?.estado || 'SOLICITADA',
    importeSolicitado: subvencion?.importeSolicitado ?? '',
    importeConcedido: subvencion?.importeConcedido ?? '',
    fechaSolicitud: subvencion?.fechaSolicitud || '',
    fechaConcesion: subvencion?.fechaConcesion || '',
    fechaJustificacion: subvencion?.fechaJustificacion || '',
    proyectoId: subvencion?.proyectoId ?? '',
  });
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);
  const concedida = datos.estado === 'CONCEDIDA' || datos.estado === 'JUSTIFICADA';

  useEffect(() => {
    const cerrarConEscape = (e) => e.key === 'Escape' && alCerrar();
    window.addEventListener('keydown', cerrarConEscape);
    return () => window.removeEventListener('keydown', cerrarConEscape);
  }, [alCerrar]);

  const cambiar = (e) => setDatos({ ...datos, [e.target.name]: e.target.value });

  const guardar = async (e) => {
    e.preventDefault();
    setError('');
    setEnviando(true);
    const cuerpo = {
      nombre: datos.nombre,
      financiador: textoONull(datos.financiador),
      expediente: textoONull(datos.expediente),
      estado: datos.estado,
      importeSolicitado: numeroONull(datos.importeSolicitado),
      importeConcedido: concedida ? numeroONull(datos.importeConcedido) : null,
      fechaSolicitud: textoONull(datos.fechaSolicitud),
      fechaConcesion: textoONull(datos.fechaConcesion),
      fechaJustificacion: textoONull(datos.fechaJustificacion),
      proyectoId: numeroONull(datos.proyectoId),
    };
    try {
      if (subvencion) {
        await axios.put(`${API}/subvenciones/ong/${idOng}/${subvencion.id}`, cuerpo);
      } else {
        await axios.post(`${API}/subvenciones/ong/${idOng}`, cuerpo);
      }
      alGuardar();
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudo guardar la subvención.');
    } finally {
      setEnviando(false);
    }
  };

  return createPortal(
    <div className="modal-fondo" onClick={alCerrar}>
      <form className="modal modal-ancho card card-pad form" role="dialog" aria-modal="true" aria-labelledby="modal-titulo" onClick={(e) => e.stopPropagation()} onSubmit={guardar}>
        <h2 id="modal-titulo">{subvencion ? 'Editar subvención' : 'Nueva subvención'}</h2>

        <div className="field">
          <label htmlFor="s-nombre">Nombre</label>
          <input id="s-nombre" name="nombre" type="text" value={datos.nombre} onChange={cambiar} maxLength={150} required autoFocus />
        </div>
        <div className="form-grid">
          <div className="field">
            <label htmlFor="s-fin">Financiador</label>
            <input id="s-fin" name="financiador" type="text" value={datos.financiador} onChange={cambiar} maxLength={150} placeholder="Ayuntamiento, fundación..." />
          </div>
          <div className="field">
            <label htmlFor="s-exp">Nº de expediente</label>
            <input id="s-exp" name="expediente" type="text" value={datos.expediente} onChange={cambiar} maxLength={60} />
          </div>
          <div className="field">
            <label htmlFor="s-estado">Estado</label>
            <select id="s-estado" name="estado" value={datos.estado} onChange={cambiar}>
              {Object.entries(ESTADOS_SUBVENCION).map(([valor, e]) => <option key={valor} value={valor}>{e.texto}</option>)}
            </select>
          </div>
          <div className="field">
            <label htmlFor="s-proyecto">Proyecto que financia</label>
            <select id="s-proyecto" name="proyectoId" value={datos.proyectoId} onChange={cambiar}>
              <option value="">Ninguno</option>
              {proyectos.map((p) => <option key={p.id} value={p.id}>{p.nombre}</option>)}
            </select>
          </div>
          <div className="field">
            <label htmlFor="s-solicitado">Importe solicitado (€)</label>
            <input id="s-solicitado" name="importeSolicitado" type="number" step="0.01" min="0" value={datos.importeSolicitado} onChange={cambiar} />
          </div>
          <div className="field">
            <label htmlFor="s-concedido">Importe concedido (€)</label>
            <input id="s-concedido" name="importeConcedido" type="number" step="0.01" min="0" value={concedida ? datos.importeConcedido : ''} onChange={cambiar} disabled={!concedida} required={concedida} />
          </div>
          <div className="field">
            <label htmlFor="s-fsol">Fecha de solicitud</label>
            <input id="s-fsol" name="fechaSolicitud" type="date" value={datos.fechaSolicitud} onChange={cambiar} />
          </div>
          <div className="field">
            <label htmlFor="s-fcon">Fecha de concesión</label>
            <input id="s-fcon" name="fechaConcesion" type="date" value={datos.fechaConcesion} onChange={cambiar} />
          </div>
          <div className="field span-2">
            <label htmlFor="s-fjus">Plazo para justificar</label>
            <input id="s-fjus" name="fechaJustificacion" type="date" value={datos.fechaJustificacion} onChange={cambiar} />
            <span className="hint">Te avisamos en el listado cuando se acerque o haya pasado.</span>
          </div>
        </div>

        {error && <div className="alert alert-error" role="alert">{error}</div>}

        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={enviando}>{enviando ? 'Guardando...' : 'Guardar'}</button>
          <button type="button" className="btn btn-ghost" onClick={alCerrar}>Cancelar</button>
        </div>
      </form>
    </div>,
    document.body
  );
};

export default FormularioSubvencion;
