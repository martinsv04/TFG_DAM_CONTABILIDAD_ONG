import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { createPortal } from 'react-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import PageHeader from '../components/PageHeader';
import { Cargando, EstadoVacio } from '../components/Estados';
import { toast } from '../components/Toast';
import { getRol } from '../services/authService';
import { formatoEuro, formatoFecha, ultimosAnios } from '../utils/format';

const API = 'http://localhost:8080/api';

const esDonacion = (ingreso) =>
  !!ingreso.tipo && ingreso.tipo.normalize('NFD').replace(/[̀-ͯ]/g, '').trim().toUpperCase() === 'DONACION';

// Certificados de donación de una ONG: uno anual por donante o uno por cada donación.
const Certificados = () => {
  const { id } = useParams(); // ID de la ONG
  const navigate = useNavigate();
  const esAdmin = getRol() === 'ADMIN';

  const [anio, setAnio] = useState(new Date().getFullYear());
  const [vista, setVista] = useState('donantes');
  const [ong, setOng] = useState(null);
  const [donantes, setDonantes] = useState(null);
  const [ingresos, setIngresos] = useState([]);
  const [editando, setEditando] = useState(null); // donante cuyos datos se están completando

  const cargar = useCallback(() => {
    setDonantes(null);
    Promise.all([
      axios.get(`${API}/ongs/${id}`),
      axios.get(`${API}/certificados/ong/${id}/donantes?anio=${anio}`),
      axios.get(`${API}/ingresos/ong/${id}`),
    ])
      .then(([o, d, i]) => {
        setOng(o.data);
        setDonantes(d.data);
        setIngresos(i.data);
      })
      .catch((err) => {
        console.error('Error al cargar los certificados:', err);
        toast('No se pudieron cargar los datos', 'error');
        setDonantes([]);
      });
  }, [id, anio]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const donaciones = useMemo(
    () =>
      ingresos
        .filter((i) => esDonacion(i) && String(i.fecha).startsWith(String(anio)))
        .sort((a, b) => String(b.fecha).localeCompare(String(a.fecha)) || b.id - a.id),
    [ingresos, anio]
  );

  const ongIncompleta = ong && (!ong.nif || !ong.direccion || !ong.representante);
  const totalAnio = (donantes || []).reduce((suma, d) => suma + Number(d.totalAnio || 0), 0);

  return (
    <div className="fade-in">
      <PageHeader
        eyebrow="Donaciones"
        titulo="Certificados de donación"
        subtitulo="Emite los certificados que los donantes necesitan para desgravar sus aportaciones."
        acciones={
          <div className="field field-inline">
            <label htmlFor="anio">Año</label>
            <select id="anio" value={anio} onChange={(e) => setAnio(Number(e.target.value))}>
              {ultimosAnios(6).map((a) => <option key={a} value={a}>{a}</option>)}
            </select>
          </div>
        }
      />

      {ongIncompleta && (
        <div className="alert alert-warn" role="alert">
          <Icon name="alert-circle" size={18} />
          <span>
            Antes de emitir certificados completa los datos de la ONG: NIF, dirección y representante legal.{' '}
            {esAdmin ? <Link to={`/editar-ong/${id}`}>Editar ONG</Link> : 'Pídeselo al administrador.'}
          </span>
        </div>
      )}

      <div className="certificados-barra">
        <div className="segmentado" role="group" aria-label="Tipo de certificado">
          <button type="button" className={vista === 'donantes' ? 'activo' : ''} onClick={() => setVista('donantes')}>Anual por donante</button>
          <button type="button" className={vista === 'donaciones' ? 'activo' : ''} onClick={() => setVista('donaciones')}>Por donación</button>
        </div>
        {donantes && donantes.length > 0 && (
          <span className="text-muted">{donantes.length} donante(s) · {formatoEuro(totalAnio)} en {anio}</span>
        )}
      </div>

      <section className="card">
        {!donantes ? (
          <Cargando texto="Cargando donaciones..." />
        ) : vista === 'donantes' ? (
          donantes.length === 0 ? (
            <EstadoVacio
              icono="heart"
              titulo={`Sin donaciones identificadas en ${anio}`}
              texto="Los certificados solo se pueden emitir para donaciones hechas a nombre de una persona; las anónimas no cuentan."
            />
          ) : (
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>Donante</th>
                    <th>NIF</th>
                    <th className="num">Donaciones</th>
                    <th className="num">Total {anio}</th>
                    <th aria-label="Acciones" />
                  </tr>
                </thead>
                <tbody>
                  {donantes.map((d) => (
                    <tr key={d.usuarioId}>
                      <td>
                        <strong>{d.nombre}</strong>
                        {!d.datosCompletos && <span className="badge badge-accent badge-sep">Faltan datos</span>}
                      </td>
                      <td className="text-muted">{d.nifCif || '—'}</td>
                      <td className="num">{d.numDonaciones}</td>
                      <td className="num"><strong>{formatoEuro(d.totalAnio)}</strong></td>
                      <td className="acciones">
                        {d.datosCompletos ? (
                          <button
                            type="button"
                            className="btn btn-secondary btn-sm"
                            disabled={ongIncompleta}
                            onClick={() => navigate(`/ongs/${id}/certificados/anual/${d.usuarioId}?anio=${anio}`)}
                          >
                            <Icon name="award" size={16} /> Certificado
                          </button>
                        ) : esAdmin ? (
                          <button type="button" className="btn btn-secondary btn-sm" onClick={() => setEditando(d)}>
                            <Icon name="edit" size={16} /> Completar datos
                          </button>
                        ) : (
                          <span className="text-muted">Pendiente del donante</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )
        ) : donaciones.length === 0 ? (
          <EstadoVacio icono="heart" titulo={`Sin donaciones en ${anio}`} texto="Registra un ingreso de tipo donación para poder emitir su certificado." />
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Fecha</th>
                  <th>Concepto</th>
                  <th>Donante</th>
                  <th className="num">Importe</th>
                  <th aria-label="Acciones" />
                </tr>
              </thead>
              <tbody>
                {donaciones.map((d) => (
                  <tr key={d.id}>
                    <td className="text-muted">{formatoFecha(d.fecha)}</td>
                    <td>{d.descripcion || 'Donación'}</td>
                    <td>{d.usuario ? d.usuario.nombre : <span className="badge">Anónima</span>}</td>
                    <td className="num"><strong>{formatoEuro(d.monto)}</strong></td>
                    <td className="acciones">
                      {d.usuario ? (
                        <button
                          type="button"
                          className="btn btn-secondary btn-sm"
                          disabled={ongIncompleta}
                          onClick={() => navigate(`/ongs/${id}/certificados/donacion/${d.id}`)}
                        >
                          <Icon name="award" size={16} /> Certificado
                        </button>
                      ) : (
                        <span className="text-muted">Sin certificado</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {editando && (
        <DatosFiscales
          donante={editando}
          alCerrar={() => setEditando(null)}
          alGuardar={() => {
            setEditando(null);
            toast('Datos del donante guardados', 'success');
            cargar();
          }}
        />
      )}
    </div>
  );
};

// Ventana para que el administrador complete el nombre, NIF y dirección de un donante
const DatosFiscales = ({ donante, alCerrar, alGuardar }) => {
  const [datos, setDatos] = useState({
    nombre: donante.nombre || '',
    nifCif: donante.nifCif || '',
    direccion: donante.direccion || '',
    codigoPostal: donante.codigoPostal || '',
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
    try {
      await axios.patch(`${API}/usuarios/${donante.usuarioId}/datos-fiscales`, datos);
      alGuardar();
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudieron guardar los datos.');
    } finally {
      setEnviando(false);
    }
  };

  // Se pinta en el <body> para que el fondo cubra toda la pantalla (la página tiene una animación con transform)
  return createPortal(
    <div className="modal-fondo" onClick={alCerrar}>
      <form className="modal card card-pad form" role="dialog" aria-modal="true" aria-labelledby="modal-titulo" onClick={(e) => e.stopPropagation()} onSubmit={guardar}>
        <h2 id="modal-titulo">Datos fiscales del donante</h2>
        <p className="text-muted">Necesarios para emitir sus certificados de donación.</p>

        <div className="field">
          <label htmlFor="d-nombre">Nombre completo</label>
          <input id="d-nombre" name="nombre" type="text" value={datos.nombre} onChange={cambiar} required autoFocus />
        </div>
        <div className="field">
          <label htmlFor="d-nif">NIF / NIE</label>
          <input id="d-nif" name="nifCif" type="text" value={datos.nifCif} onChange={cambiar} placeholder="12345678Z" required />
        </div>
        <div className="field">
          <label htmlFor="d-cp">Código postal</label>
          <input id="d-cp" name="codigoPostal" type="text" inputMode="numeric" maxLength={5} value={datos.codigoPostal} onChange={cambiar} placeholder="41001" />
          <span className="hint">Opcional para el certificado, necesario para el Modelo 182.</span>
        </div>
        <div className="field">
          <label htmlFor="d-dir">Dirección postal</label>
          <input id="d-dir" name="direccion" type="text" value={datos.direccion} onChange={cambiar} placeholder="Calle, número, código postal y ciudad" required />
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

export default Certificados;
