import React, { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import PageHeader from '../components/PageHeader';
import { Cargando, EstadoVacio } from '../components/Estados';
import { toast } from '../components/Toast';
import { getRol } from '../services/authService';
import { formatoEuro, ultimosAnios } from '../utils/format';

const API = 'http://localhost:8080/api';

// Modelo 182: declaración informativa anual de donativos que la ONG presenta a la AEAT.
const Modelo182 = () => {
  const { id } = useParams();
  const esAdmin = getRol() === 'ADMIN';
  const [anio, setAnio] = useState(new Date().getFullYear() - 1);
  const [datos, setDatos] = useState(null);
  const [descargando, setDescargando] = useState(false);

  const cargar = useCallback(() => {
    setDatos(null);
    axios
      .get(`${API}/modelo182/ong/${id}?anio=${anio}`)
      .then((res) => setDatos(res.data))
      .catch(() => {
        toast('No se pudo preparar el Modelo 182', 'error');
        setDatos({ error: true });
      });
  }, [id, anio]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const descargar = async () => {
    setDescargando(true);
    try {
      const res = await axios.get(`${API}/modelo182/ong/${id}/fichero?anio=${anio}`, { responseType: 'blob' });
      const url = URL.createObjectURL(res.data);
      const a = document.createElement('a');
      a.href = url;
      a.download = `modelo182_${anio}.txt`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
      toast('Fichero generado', 'success');
    } catch (err) {
      toast('No se pudo generar el fichero. Revisa los datos pendientes.', 'error');
    } finally {
      setDescargando(false);
    }
  };

  const registros = datos && !datos.error ? datos.registros : [];

  return (
    <div className="fade-in">
      <PageHeader
        eyebrow="Hacienda"
        titulo="Modelo 182"
        subtitulo="Declaración informativa de donativos, donaciones y aportaciones recibidas."
        acciones={
          <div className="field field-inline">
            <label htmlFor="anio">Ejercicio</label>
            <select id="anio" value={anio} onChange={(e) => setAnio(Number(e.target.value))}>
              {ultimosAnios(6).map((a) => <option key={a} value={a}>{a}</option>)}
            </select>
          </div>
        }
      />

      {!datos ? (
        <Cargando texto="Preparando la declaración..." />
      ) : datos.error ? (
        <section className="card"><EstadoVacio icono="alert-circle" titulo="No se pudo cargar" texto="Inténtalo de nuevo en unos segundos." /></section>
      ) : (
        <>
          {datos.problemas.length > 0 && (
            <div className="alert alert-warn" role="alert">
              <Icon name="alert-circle" size={18} />
              <span>
                <strong>Datos de la ONG por completar:</strong>
                <ul className="lista-problemas">
                  {datos.problemas.map((p) => <li key={p}>{p}</li>)}
                </ul>
                {esAdmin ? <Link to={`/editar-ong/${id}`}>Editar ONG</Link> : 'Pídeselo al administrador.'}
              </span>
            </div>
          )}

          <div className="resumen-182">
            <div className="card card-pad"><span className="text-muted">Declarante</span><strong>{datos.declarante.nombre}</strong><small>NIF {datos.declarante.nif || '—'}</small></div>
            <div className="card card-pad"><span className="text-muted">Donantes</span><strong>{registros.length}</strong><small>con NIF identificado</small></div>
            <div className="card card-pad"><span className="text-muted">Importe total {anio}</span><strong>{formatoEuro(datos.importeTotal)}</strong><small>clave A · en dinero</small></div>
          </div>

          <section className="card">
            {registros.length === 0 ? (
              <EstadoVacio icono="file-text" titulo={`Sin donaciones identificadas en ${anio}`} texto="Solo se declaran las donaciones hechas a nombre de una persona con NIF; las anónimas no se incluyen." />
            ) : (
              <div className="table-wrap">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Donante</th>
                      <th>NIF</th>
                      <th>Prov.</th>
                      <th className="num">Importe</th>
                      <th className="num">Deducción</th>
                      <th>Recurrente</th>
                    </tr>
                  </thead>
                  <tbody>
                    {registros.map((r) => (
                      <tr key={r.usuarioId}>
                        <td>
                          <strong>{r.nombre}</strong>
                          {r.problemas.length > 0 && <div className="problema-fila">{r.problemas.join(' · ')}</div>}
                        </td>
                        <td>{r.nif || '—'}</td>
                        <td>{r.provincia || '—'}</td>
                        <td className="num">{formatoEuro(r.importe)}</td>
                        <td className="num">{r.porcentaje != null ? `${Number(r.porcentaje)} %` : '—'}</td>
                        <td>{r.recurrente ? 'Sí' : 'No'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>

          <div className="form-actions" style={{ marginTop: '1.2rem' }}>
            <button type="button" className="btn btn-primary btn-lg" onClick={descargar} disabled={!datos.listo || descargando}>
              <Icon name="download" size={18} /> {descargando ? 'Generando...' : 'Descargar fichero 182'}
            </button>
            {!datos.listo && <span className="text-muted">Disponible cuando no queden datos pendientes.</span>}
          </div>

          <p className="certificado-aviso" style={{ textAlign: 'left' }}>
            Antes de presentarlo, comprueba el fichero con el validador de la Sede Electrónica de la AEAT y revisa su contenido con tu gestor.
            El nombre del donante debe constar como «Apellidos Nombre». El porcentaje se calcula con la normativa vigente desde 2024 y la recurrencia
            con las donaciones de los dos años anteriores.
          </p>
        </>
      )}
    </div>
  );
};

export default Modelo182;
