import React, { useEffect, useMemo, useState } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import PageHeader from '../components/PageHeader';
import { Cargando, EstadoVacio } from '../components/Estados';
import { toast } from '../components/Toast';
import { formatoEuro, formatoFecha } from '../utils/format';

const API = 'http://localhost:8080/api';

const NuevaFactura = () => {
  const { id } = useParams(); // ID de la ONG
  const navigate = useNavigate();
  const [params] = useSearchParams();

  // Se puede llegar con ?tipo=ingreso&movimiento=12 (desde el aviso tras registrar un movimiento)
  const [tipo, setTipo] = useState(params.get('tipo') === 'gasto' ? 'GASTO' : 'INGRESO');
  const [seleccionado, setSeleccionado] = useState(params.get('movimiento') ? Number(params.get('movimiento')) : null);
  const [datos, setDatos] = useState(null);
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    Promise.all([
      axios.get(`${API}/ingresos/ong/${id}`),
      axios.get(`${API}/gastos/ong/${id}`),
      axios.get(`${API}/facturas/ong/${id}`),
    ])
      .then(([ingresos, gastos, facturas]) =>
        setDatos({ ingresos: ingresos.data, gastos: gastos.data, facturas: facturas.data })
      )
      .catch((err) => {
        console.error('Error al cargar los movimientos:', err);
        setError('No se pudieron cargar los ingresos y gastos.');
        setDatos({ ingresos: [], gastos: [], facturas: [] });
      });
  }, [id]);

  // Solo se ofrecen los movimientos que todavía no tienen factura, los más recientes primero
  const disponibles = useMemo(() => {
    if (!datos) return [];
    const facturados = new Set(
      datos.facturas
        .map((f) => (tipo === 'INGRESO' ? f.ingreso && f.ingreso.id : f.gasto && f.gasto.id))
        .filter(Boolean)
    );
    const lista = tipo === 'INGRESO' ? datos.ingresos : datos.gastos;
    return lista
      .filter((m) => !facturados.has(m.id))
      .sort((a, b) => String(b.fecha).localeCompare(String(a.fecha)) || b.id - a.id);
  }, [datos, tipo]);

  const elegido = disponibles.find((m) => m.id === seleccionado) || null;

  const cambiarTipo = (nuevo) => {
    if (nuevo === tipo) return;
    setTipo(nuevo);
    setSeleccionado(null);
    setError('');
  };

  const generar = async () => {
    if (!elegido) return;
    setError('');
    setEnviando(true);
    try {
      const res = await axios.post(`${API}/facturas/ong/${id}/generar`, { tipo, movimientoId: elegido.id });
      toast(`Factura ${res.data.numero} generada`, 'success');
      navigate(`/facturas/${res.data.id}`);
    } catch (err) {
      console.error('Error al generar la factura:', err);
      setError(err.response?.data?.message || 'No se pudo generar la factura. Inténtalo de nuevo.');
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="form-pagina form-pagina-ancha fade-in">
      <PageHeader
        eyebrow="Facturas"
        titulo="Nueva factura"
        subtitulo="Elige si la factura es de un ingreso o de un gasto y selecciona cuál. El número y el importe se toman del propio movimiento."
      />

      <div className="card card-pad form">
        <div className="field">
          <span className="label">Tipo de factura</span>
          <div className="segmentado" role="group" aria-label="Tipo de factura">
            <button type="button" className={tipo === 'INGRESO' ? 'activo' : ''} onClick={() => cambiarTipo('INGRESO')}>De un ingreso</button>
            <button type="button" className={tipo === 'GASTO' ? 'activo' : ''} onClick={() => cambiarTipo('GASTO')}>De un gasto</button>
          </div>
        </div>

        <div className="field">
          <span className="label">{tipo === 'INGRESO' ? 'Ingreso' : 'Gasto'} a facturar</span>
          {!datos ? (
            <Cargando texto="Cargando movimientos..." />
          ) : disponibles.length === 0 ? (
            <EstadoVacio
              icono="file-text"
              titulo={`No hay ${tipo === 'INGRESO' ? 'ingresos' : 'gastos'} pendientes de factura`}
              texto={`Todos los ${tipo === 'INGRESO' ? 'ingresos' : 'gastos'} registrados ya tienen factura, o aún no has registrado ninguno.`}
            />
          ) : (
            <div className="movimientos-elegir" role="radiogroup">
              {disponibles.map((m) => (
                <button
                  type="button"
                  role="radio"
                  aria-checked={seleccionado === m.id}
                  key={m.id}
                  className={`movimiento-opcion${seleccionado === m.id ? ' activo' : ''}`}
                  onClick={() => setSeleccionado(m.id)}
                >
                  <span className="movimiento-radio" aria-hidden="true" />
                  <span className="movimiento-info">
                    <strong>{m.descripcion || 'Sin descripción'}</strong>
                    <small>
                      {formatoFecha(m.fecha)} · {tipo === 'INGRESO' ? m.tipo : m.categoria}
                    </small>
                  </span>
                  <span className="movimiento-importe">{formatoEuro(m.monto)}</span>
                </button>
              ))}
            </div>
          )}
        </div>

        {error && <div className="alert alert-error" role="alert">{error}</div>}

        <div className="form-actions">
          <button type="button" className="btn btn-primary btn-lg" disabled={!elegido || enviando} onClick={generar}>
            <Icon name="file-text" size={18} /> {enviando ? 'Generando...' : elegido ? `Generar factura de ${formatoEuro(elegido.monto)}` : 'Generar factura'}
          </button>
          <button type="button" className="btn btn-ghost btn-lg" onClick={() => navigate(`/ongs/${id}/facturas`)}>
            Cancelar
          </button>
        </div>
      </div>
    </div>
  );
};

export default NuevaFactura;
