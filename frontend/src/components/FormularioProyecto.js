import React, { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';
import axios from 'axios';
import { ESTADOS_PROYECTO, numeroONull, textoONull } from '../utils/proyectos';

const API = 'http://localhost:8080/api';

// Ventana para crear o editar un proyecto (proyecto = null para crear)
const FormularioProyecto = ({ idOng, proyecto, alCerrar, alGuardar }) => {
  const [datos, setDatos] = useState({
    nombre: proyecto?.nombre || '',
    descripcion: proyecto?.descripcion || '',
    estado: proyecto?.estado || 'EN_CURSO',
    fechaInicio: proyecto?.fechaInicio || '',
    fechaFin: proyecto?.fechaFin || '',
    presupuesto: proyecto?.presupuesto ?? '',
  });
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

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
      descripcion: textoONull(datos.descripcion),
      estado: datos.estado,
      fechaInicio: textoONull(datos.fechaInicio),
      fechaFin: textoONull(datos.fechaFin),
      presupuesto: numeroONull(datos.presupuesto),
    };
    try {
      if (proyecto) {
        await axios.put(`${API}/proyectos/ong/${idOng}/${proyecto.id}`, cuerpo);
      } else {
        await axios.post(`${API}/proyectos/ong/${idOng}`, cuerpo);
      }
      alGuardar();
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudo guardar el proyecto.');
    } finally {
      setEnviando(false);
    }
  };

  return createPortal(
    <div className="modal-fondo" onClick={alCerrar}>
      <form className="modal modal-ancho card card-pad form" role="dialog" aria-modal="true" aria-labelledby="modal-titulo" onClick={(e) => e.stopPropagation()} onSubmit={guardar}>
        <h2 id="modal-titulo">{proyecto ? 'Editar proyecto' : 'Nuevo proyecto'}</h2>

        <div className="field">
          <label htmlFor="p-nombre">Nombre</label>
          <input id="p-nombre" name="nombre" type="text" value={datos.nombre} onChange={cambiar} maxLength={150} required autoFocus />
        </div>
        <div className="field">
          <label htmlFor="p-desc">Descripción</label>
          <textarea id="p-desc" name="descripcion" value={datos.descripcion} onChange={cambiar} maxLength={1000} placeholder="Objetivo y a quién va dirigido" />
        </div>
        <div className="form-grid">
          <div className="field">
            <label htmlFor="p-estado">Estado</label>
            <select id="p-estado" name="estado" value={datos.estado} onChange={cambiar}>
              {Object.entries(ESTADOS_PROYECTO).map(([valor, e]) => <option key={valor} value={valor}>{e.texto}</option>)}
            </select>
          </div>
          <div className="field">
            <label htmlFor="p-presupuesto">Presupuesto (€)</label>
            <input id="p-presupuesto" name="presupuesto" type="number" step="0.01" min="0" value={datos.presupuesto} onChange={cambiar} placeholder="0,00" />
          </div>
          <div className="field">
            <label htmlFor="p-inicio">Inicio</label>
            <input id="p-inicio" name="fechaInicio" type="date" value={datos.fechaInicio} onChange={cambiar} />
          </div>
          <div className="field">
            <label htmlFor="p-fin">Fin</label>
            <input id="p-fin" name="fechaFin" type="date" value={datos.fechaFin} onChange={cambiar} />
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

export default FormularioProyecto;
