import React, { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';
import axios from 'axios';
import { CARGOS_GOBIERNO, CARGOS_DIRECCION, ORGANOS } from '../utils/gobierno';

const API = 'http://localhost:8080/api';

// Ventana para añadir o editar una persona del órgano de gobierno o del equipo de dirección
const FormularioGobierno = ({ idOng, persona, organoInicial, alCerrar, alGuardar }) => {
  const [datos, setDatos] = useState({
    nombre: persona?.nombre || '',
    cargo: persona?.cargo || '',
    organo: persona?.organo || organoInicial || 'GOBIERNO',
    fechaNombramiento: persona?.fechaNombramiento || '',
    descripcion: persona?.descripcion || '',
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
      cargo: datos.cargo,
      organo: datos.organo,
      fechaNombramiento: datos.fechaNombramiento || null,
      descripcion: datos.descripcion.trim() || null,
    };
    try {
      if (persona) {
        await axios.put(`${API}/gobierno/${persona.id}`, cuerpo);
      } else {
        await axios.post(`${API}/gobierno/ong/${idOng}`, cuerpo);
      }
      alGuardar();
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudo guardar.');
    } finally {
      setEnviando(false);
    }
  };

  const sugerencias = datos.organo === 'DIRECCION' ? CARGOS_DIRECCION : CARGOS_GOBIERNO;

  return createPortal(
    <div className="modal-fondo" onClick={alCerrar}>
      <form className="modal modal-ancho card card-pad form" role="dialog" aria-modal="true" aria-labelledby="modal-titulo" onClick={(e) => e.stopPropagation()} onSubmit={guardar}>
        <h2 id="modal-titulo">{persona ? 'Editar persona' : 'Añadir persona'}</h2>

        <div className="form-grid">
          <div className="field">
            <label htmlFor="g-nombre">Nombre y apellidos</label>
            <input id="g-nombre" name="nombre" type="text" value={datos.nombre} onChange={cambiar} maxLength={120} required autoFocus />
          </div>
          <div className="field">
            <label htmlFor="g-organo">Pertenece a</label>
            <select id="g-organo" name="organo" value={datos.organo} onChange={cambiar}>
              {Object.entries(ORGANOS).map(([valor, texto]) => <option key={valor} value={valor}>{texto}</option>)}
            </select>
          </div>
          <div className="field">
            <label htmlFor="g-cargo">Cargo</label>
            <input id="g-cargo" name="cargo" type="text" list="g-cargos" value={datos.cargo} onChange={cambiar} maxLength={80} required />
            <datalist id="g-cargos">{sugerencias.map((c) => <option key={c} value={c} />)}</datalist>
          </div>
          <div className="field">
            <label htmlFor="g-fecha">Fecha de nombramiento <span className="text-muted">(opcional)</span></label>
            <input id="g-fecha" name="fechaNombramiento" type="date" value={datos.fechaNombramiento} onChange={cambiar} />
          </div>
        </div>
        <div className="field">
          <label htmlFor="g-desc">Presentación breve <span className="text-muted">(opcional)</span></label>
          <textarea id="g-desc" name="descripcion" value={datos.descripcion} onChange={cambiar} maxLength={500} placeholder="Profesión, trayectoria o vinculación con la entidad" />
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

export default FormularioGobierno;
