import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Icon from './Icons';
import PageHeader from './PageHeader';
import { toast } from './Toast';
import { Cargando, EstadoVacio } from './Estados';
import CampoCuenta from './CampoCuenta';
import { CUENTAS_INGRESO, CUENTAS_GASTO } from '../utils/cuentas';

// Formulario común para registrar un ingreso o un gasto.
// campo: 'tipo' (ingresos) o 'categoria' (gastos); ambos se envían tal cual al backend.
const FormularioMovimiento = ({ titulo, subtitulo, url, campo, etiquetaCampo, opciones, textoBoton, mensajeOk, botonClase, tipoMovimiento }) => {
  const { id, movimientoId: idEdicion } = useParams(); // ID de la ONG y, al editar, del movimiento
  const editando = Boolean(idEdicion);
  const navigate = useNavigate();

  const inicial = () => ({
    descripcion: '',
    monto: '',
    [campo]: '',
    fecha: new Date().toISOString().split('T')[0],
    id_proyecto: '',
    id_subvencion: '',
    cuenta: '',
    origen: '',
    financiador: '',
    area: '',
  });

  const [formData, setFormData] = useState(inicial);
  const [mensaje, setMensaje] = useState('');
  const [movimientoId, setMovimientoId] = useState(null);
  const [enviando, setEnviando] = useState(false);
  const [proyectos, setProyectos] = useState([]);
  const [subvenciones, setSubvenciones] = useState([]);
  const [financiadoresPrevios, setFinanciadoresPrevios] = useState([]);
  const [cargando, setCargando] = useState(editando);
  const [errorCarga, setErrorCarga] = useState('');

  // Proyecto (y subvención, en los ingresos) son opcionales: si la ONG no tiene ninguno, no se muestran los selectores
  useEffect(() => {
    axios.get(`http://localhost:8080/api/proyectos/ong/${id}`)
      .then((res) => setProyectos(res.data))
      .catch(() => {});
    if (tipoMovimiento === 'ingreso') {
      axios.get(`http://localhost:8080/api/subvenciones/ong/${id}`)
        .then((res) => setSubvenciones(res.data))
        .catch(() => {});
      // Financiadores ya usados en otros ingresos, para sugerirlos al escribir
      axios.get(`http://localhost:8080/api/ingresos/ong/${id}`)
        .then((res) => setFinanciadoresPrevios([...new Set(res.data.map((i) => i.financiador).filter(Boolean))]))
        .catch(() => {});
    }
  }, [id, tipoMovimiento]);

  // Al editar se cargan los datos actuales del movimiento
  useEffect(() => {
    if (!editando) return;
    axios.get(`${url}/${idEdicion}`)
      .then((res) => {
        const m = res.data;
        setFormData({
          descripcion: m.descripcion || '',
          monto: m.monto ?? '',
          [campo]: m[campo] || '',
          fecha: m.fecha || '',
          id_proyecto: m.proyecto ? String(m.proyecto.id) : '',
          id_subvencion: m.subvencion ? String(m.subvencion.id) : '',
          cuenta: m.cuenta || '',
          origen: m.origen || '',
          financiador: m.financiador || '',
          area: m.area || '',
        });
      })
      .catch(() => setErrorCarga('No se pudo cargar el movimiento. Puede que no exista o que no tengas acceso.'))
      .finally(() => setCargando(false));
  }, [editando, url, idEdicion, campo]);

  // Solo se ofrecen los proyectos y subvenciones vigentes (más el que ya tenga asignado el movimiento al editar)
  const proyectosOfrecidos = proyectos.filter((p) => p.estado !== 'FINALIZADO' || String(p.id) === String(formData.id_proyecto));
  const subvencionesOfrecidas = subvenciones.filter((s) => s.estado === 'CONCEDIDA' || String(s.id) === String(formData.id_subvencion));
  // Si el valor guardado ya no está entre las opciones actuales (datos antiguos), se mantiene para poder guardar
  const opcionesCampo = formData[campo] && !opciones.some((o) => o.valor === formData[campo])
    ? [{ valor: formData[campo], texto: formData[campo] }, ...opciones]
    : opciones;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setEnviando(true);

    const payload = { ...formData, monto: parseFloat(formData.monto), id_ong: id };
    if (!payload.id_proyecto) delete payload.id_proyecto;
    if (!payload.id_subvencion) delete payload.id_subvencion;
    if (!payload.cuenta) delete payload.cuenta;
    if (!payload.origen) delete payload.origen;
    if (!payload.financiador) delete payload.financiador;
    if (!payload.area) delete payload.area;

    try {
      if (editando) {
        // La edición envía el movimiento con sus datos nuevos; proyecto y subvención a null los quitan
        const cambios = {
          descripcion: formData.descripcion,
          monto: parseFloat(formData.monto),
          [campo]: formData[campo],
          fecha: formData.fecha,
          cuenta: formData.cuenta || null,
          proyecto: formData.id_proyecto ? { id: Number(formData.id_proyecto) } : null,
        };
        if (tipoMovimiento === 'ingreso') {
          cambios.subvencion = formData.id_subvencion ? { id: Number(formData.id_subvencion) } : null;
          cambios.origen = formData.origen || null;
          cambios.financiador = formData.financiador || null;
        } else {
          cambios.area = formData.area || null;
        }
        await axios.put(`${url}/${idEdicion}`, cambios);
        toast('Cambios guardados', 'success');
        navigate(`/area-economica/${id}`);
        return;
      }
      const response = await axios.post(url, payload);
      const data = response.data;
      setMensaje(data.message || mensajeOk);
      setMovimientoId(data.id || null);
    } catch (error) {
      console.error('Error al guardar:', error);
      toast(error.response?.data?.message || 'Ocurrió un error al guardar el movimiento', 'error');
    } finally {
      setEnviando(false);
    }
  };

  const registrarOtro = () => {
    setFormData(inicial());
    setMensaje('');
    setMovimientoId(null);
  };

  if (cargando) return <Cargando texto="Cargando movimiento..." />;
  if (errorCarga) {
    return (
      <div className="form-pagina fade-in">
        <EstadoVacio icono="alert-circle" titulo="No se pudo abrir el movimiento" texto={errorCarga} />
      </div>
    );
  }

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
      <PageHeader
        eyebrow="Área económica"
        titulo={editando ? `Editar ${tipoMovimiento}` : titulo}
        subtitulo={editando ? 'Corrige los datos del movimiento. El cambio quedará anotado en el registro de actividad.' : subtitulo}
      />

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
              {opcionesCampo.map((opcion) => (
                <option key={opcion.valor} value={opcion.valor}>{opcion.texto}</option>
              ))}
            </select>
          </div>

          <div className="field span-2">
            <label htmlFor="fecha">Fecha</label>
            <input id="fecha" type="date" name="fecha" value={formData.fecha} onChange={handleChange} required />
          </div>

          <div className="field span-2">
            <label htmlFor="cuenta">Cuenta contable <span className="text-muted">(opcional)</span></label>
            <CampoCuenta
              id="cuenta"
              value={formData.cuenta}
              onChange={(c) => setFormData((d) => ({ ...d, cuenta: c }))}
              lista={tipoMovimiento === 'ingreso' ? CUENTAS_INGRESO : CUENTAS_GASTO}
            />
          </div>

          {tipoMovimiento === 'ingreso' ? (
            <>
              <div className="field">
                <label htmlFor="origen">Origen de la financiación <span className="text-muted">(opcional)</span></label>
                <select id="origen" name="origen" value={formData.origen} onChange={handleChange}>
                  <option value="">Automático (según el tipo)</option>
                  <option value="PUBLICO">Pública</option>
                  <option value="PRIVADO">Privada</option>
                </select>
              </div>
              <div className="field">
                <label htmlFor="financiador">Financiador <span className="text-muted">(opcional)</span></label>
                <input id="financiador" name="financiador" type="text" list="lista-financiadores" maxLength={120}
                  placeholder="Ej.: Ayuntamiento de Sevilla" value={formData.financiador} onChange={handleChange} />
                <datalist id="lista-financiadores">
                  {[...new Set([...subvenciones.map((s) => s.financiador).filter(Boolean), ...financiadoresPrevios])]
                    .map((f) => <option key={f} value={f} />)}
                </datalist>
              </div>
              <p className="text-muted span-2 ayuda-campo">
                Sirven para el informe de transparencia. Si los dejas vacíos se deducen del tipo de ingreso y de la subvención.
                No escribas nombres de particulares.
              </p>
            </>
          ) : (
            <div className="field span-2">
              <label htmlFor="area">Destino del gasto <span className="text-muted">(opcional)</span></label>
              <select id="area" name="area" value={formData.area} onChange={handleChange}>
                <option value="">Automático (con proyecto: misión; sin proyecto: administración)</option>
                <option value="MISION">Misión: actividades y proyectos de la ONG</option>
                <option value="ADMINISTRACION">Administración: gestión y funcionamiento</option>
                <option value="CAPTACION">Captación de fondos</option>
              </select>
            </div>
          )}

          {proyectosOfrecidos.length > 0 && (
            <div className="field">
              <label htmlFor="id_proyecto">Proyecto <span className="text-muted">(opcional)</span></label>
              <select id="id_proyecto" name="id_proyecto" value={formData.id_proyecto} onChange={handleChange}>
                <option value="">Sin proyecto</option>
                {proyectosOfrecidos.map((p) => <option key={p.id} value={p.id}>{p.nombre}</option>)}
              </select>
            </div>
          )}

          {subvencionesOfrecidas.length > 0 && (
            <div className="field">
              <label htmlFor="id_subvencion">Subvención <span className="text-muted">(opcional)</span></label>
              <select id="id_subvencion" name="id_subvencion" value={formData.id_subvencion} onChange={handleChange}>
                <option value="">Ninguna</option>
                {subvencionesOfrecidas.map((s) => <option key={s.id} value={s.id}>{s.nombre}</option>)}
              </select>
            </div>
          )}
        </div>

        <div className="form-actions">
          <button type="submit" className={`btn ${botonClase || 'btn-primary'} btn-lg`} disabled={enviando}>
            {enviando ? 'Guardando...' : editando ? 'Guardar cambios' : textoBoton}
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
