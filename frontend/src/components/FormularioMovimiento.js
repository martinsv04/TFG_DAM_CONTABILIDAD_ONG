import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Icon from './Icons';
import PageHeader from './PageHeader';
import { toast } from './Toast';

// Formulario común para registrar un ingreso o un gasto.
// campo: 'tipo' (ingresos) o 'categoria' (gastos); ambos se envían tal cual al backend.
const FormularioMovimiento = ({ titulo, subtitulo, url, campo, etiquetaCampo, opciones, textoBoton, mensajeOk, botonClase, tipoMovimiento }) => {
  const { id } = useParams(); // ID de la ONG
  const navigate = useNavigate();

  const inicial = () => ({
    descripcion: '',
    monto: '',
    [campo]: '',
    fecha: new Date().toISOString().split('T')[0],
  });

  const [formData, setFormData] = useState(inicial);
  const [mensaje, setMensaje] = useState('');
  const [movimientoId, setMovimientoId] = useState(null);
  const [enviando, setEnviando] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setEnviando(true);

    const payload = { ...formData, monto: parseFloat(formData.monto), id_ong: id };

    try {
      const response = await axios.post(url, payload);
      const data = response.data;
      setMensaje(data.message || mensajeOk);
      setMovimientoId(data.id || null);
    } catch (error) {
      console.error('Error al guardar:', error);
      toast('Ocurrió un error al guardar el movimiento', 'error');
    } finally {
      setEnviando(false);
    }
  };

  const registrarOtro = () => {
    setFormData(inicial());
    setMensaje('');
    setMovimientoId(null);
  };

  if (mensaje) {
    return (
      <div className="form-pagina fade-in">
        <div className="card card-pad exito">
          <span className="exito-icono"><Icon name="check" size={30} /></span>
          <h2>{mensaje}</h2>
          <p className="text-muted">Si lo necesitas, ya puedes generar su factura.</p>
          <div className="form-actions exito-acciones">
            {movimientoId && (
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => navigate(`/ongs/${id}/facturas/nueva?tipo=${tipoMovimiento}&movimiento=${movimientoId}`)}
              >
                <Icon name="file-text" size={16} /> Generar factura
              </button>
            )}
            <button type="button" className="btn btn-secondary" onClick={registrarOtro}>Registrar otro</button>
            <button type="button" className="btn btn-ghost" onClick={() => navigate(`/area-economica/${id}`)}>
              Volver al área económica
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="form-pagina fade-in">
      <PageHeader eyebrow="Área económica" titulo={titulo} subtitulo={subtitulo} />

      <form className="card card-pad form" onSubmit={handleSubmit}>
        <div className="field">
          <label htmlFor="descripcion">Descripción</label>
          <input id="descripcion" type="text" name="descripcion" value={formData.descripcion} onChange={handleChange} required />
        </div>

        <div className="form-grid">
          <div className="field">
            <label htmlFor="monto">Importe (€)</label>
            <input id="monto" type="number" name="monto" step="0.01" min="0" placeholder="0,00" value={formData.monto} onChange={handleChange} required />
          </div>

          <div className="field">
            <label htmlFor={campo}>{etiquetaCampo}</label>
            <select id={campo} name={campo} value={formData[campo]} onChange={handleChange} required>
              <option value="" disabled hidden>Seleccionar</option>
              {opciones.map((opcion) => (
                <option key={opcion.valor} value={opcion.valor}>{opcion.texto}</option>
              ))}
            </select>
          </div>

          <div className="field span-2">
            <label htmlFor="fecha">Fecha</label>
            <input id="fecha" type="date" name="fecha" value={formData.fecha} onChange={handleChange} required />
          </div>
        </div>

        <div className="form-actions">
          <button type="submit" className={`btn ${botonClase || 'btn-primary'} btn-lg`} disabled={enviando}>
            {enviando ? 'Guardando...' : textoBoton}
          </button>
          <button type="button" className="btn btn-ghost btn-lg" onClick={() => navigate(`/area-economica/${id}`)}>
            Cancelar
          </button>
        </div>
      </form>
    </div>
  );
};

export default FormularioMovimiento;
