import React, { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import PageHeader from '../components/PageHeader';
import InformeTransparencia from '../components/InformeTransparencia';
import { Cargando, EstadoVacio } from '../components/Estados';
import { toast } from '../components/Toast';
import { getRol } from '../services/authService';

const API = 'http://localhost:8080/api';

// Informe de transparencia de la ONG: de dónde viene el dinero y a qué se destina, más la página pública
const Transparencia = () => {
  const { id } = useParams();
  const esAdmin = getRol() === 'ADMIN';
  const [anio, setAnio] = useState(null);
  const [datos, setDatos] = useState(null);
  const [error, setError] = useState(false);
  const [guardando, setGuardando] = useState(false);

  const cargar = useCallback((anioElegido) => {
    setError(false);
    axios
      .get(`${API}/transparencia/ong/${id}`, { params: anioElegido ? { anio: anioElegido } : {} })
      .then((res) => {
        setDatos(res.data);
        setAnio(res.data.anio);
      })
      .catch(() => setError(true));
  }, [id]);

  useEffect(() => {
    cargar(null);
  }, [cargar]);

  const enlacePublico = `${window.location.origin}/transparencia/${id}`;

  const publicar = async (publica) => {
    setGuardando(true);
    try {
      await axios.put(`${API}/transparencia/ong/${id}/publicar`, { publica });
      setDatos((d) => ({ ...d, publicada: publica }));
      toast(publica ? 'Página pública publicada' : 'Página pública retirada', 'success');
    } catch (e) {
      toast('No se pudo cambiar la publicación', 'error');
    } finally {
      setGuardando(false);
    }
  };

  const copiar = async () => {
    try {
      await navigator.clipboard.writeText(enlacePublico);
      toast('Enlace copiado', 'success');
    } catch (e) {
      toast('No se pudo copiar; selecciónalo manualmente', 'error');
    }
  };

  if (error) {
    return (
      <div className="fade-in">
        <EstadoVacio icono="alert-circle" titulo="No se pudo cargar el informe" texto="Comprueba que tienes acceso a esta ONG e inténtalo de nuevo." />
      </div>
    );
  }
  if (!datos) return <Cargando texto="Calculando el informe..." />;

  const pendientes = datos.gastosSinClasificar + datos.ingresosSinClasificar;

  return (
    <div className="fade-in">
      <PageHeader
        eyebrow="Rendición de cuentas"
        titulo="Transparencia"
        subtitulo="Origen de la financiación y destino de los gastos, calculados a partir de lo que ya has registrado."
        acciones={(
          <label className="tr-anio">
            <span className="text-muted">Año</span>
            <select value={anio} onChange={(e) => { setDatos(null); cargar(Number(e.target.value)); }}>
              {datos.anios.map((a) => <option key={a} value={a}>{a}</option>)}
            </select>
          </label>
        )}
      />

      {pendientes > 0 && (
        <div className="alert alert-warn" role="status">
          <Icon name="info" size={18} />
          <span>
            {datos.ingresosSinClasificar > 0 && <>{datos.ingresosSinClasificar} ingreso{datos.ingresosSinClasificar === 1 ? '' : 's'} sin origen indicado</>}
            {datos.ingresosSinClasificar > 0 && datos.gastosSinClasificar > 0 && ' y '}
            {datos.gastosSinClasificar > 0 && <>{datos.gastosSinClasificar} gasto{datos.gastosSinClasificar === 1 ? '' : 's'} sin destino indicado</>}
            : el informe los deduce (subvención = público; gasto con proyecto = misión, sin proyecto = administración).
            Puedes precisarlos editando cada movimiento en el <Link to={`/area-economica/${id}`}>área económica</Link>.
          </span>
        </div>
      )}

      <InformeTransparencia datos={datos} />

      <section className="card card-pad tr-publica">
        <div className="tr-publica-texto">
          <h3><Icon name="globe" size={18} /> Página pública de transparencia</h3>
          <p className="text-muted">
            Una página que cualquiera puede ver, sin iniciar sesión, con estas cifras y vuestro órgano de gobierno. Puedes enlazarla desde la web de la entidad.
            Solo se muestran importes agregados y nombres de financiadores; nunca datos de donantes particulares.
          </p>
          {datos.publicada && (
            <p className="tr-enlace"><a href={enlacePublico} target="_blank" rel="noreferrer">{enlacePublico}</a></p>
          )}
        </div>
        {esAdmin ? (
          <div className="tr-publica-acciones">
            {datos.publicada ? (
              <>
                <button type="button" className="btn btn-secondary" onClick={copiar}>Copiar enlace</button>
                <button type="button" className="btn btn-ghost" onClick={() => publicar(false)} disabled={guardando}>Retirar</button>
              </>
            ) : (
              <button type="button" className="btn btn-primary" onClick={() => publicar(true)} disabled={guardando}>Publicar página</button>
            )}
          </div>
        ) : (
          <p className="text-muted">{datos.publicada ? 'Publicada' : 'Sin publicar'} · solo un administrador puede cambiarlo.</p>
        )}
      </section>
    </div>
  );
};

export default Transparencia;
