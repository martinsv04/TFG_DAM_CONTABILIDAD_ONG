import React, { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import PageHeader from '../components/PageHeader';
import CertificadoPapel from '../components/CertificadoPapel';
import { Cargando, EstadoVacio } from '../components/Estados';
import { toast } from '../components/Toast';
import { descargarPdf } from '../utils/pdf';
import { ultimosAnios } from '../utils/format';

// Certificado anual de las donaciones del propio donante.
const MisCertificados = () => {
  const { id } = useParams(); // id de la ONG (solo para la navegación)
  const [anio, setAnio] = useState(new Date().getFullYear());
  const [certificado, setCertificado] = useState(null);
  const [error, setError] = useState('');
  const [cargando, setCargando] = useState(true);
  const [descargando, setDescargando] = useState(false);

  useEffect(() => {
    setCargando(true);
    setError('');
    setCertificado(null);
    axios
      .get(`http://localhost:8080/api/certificados/mi-anual?anio=${anio}`)
      .then((res) => setCertificado(res.data))
      .catch((err) => setError(err.response?.data?.message || 'No se pudo cargar el certificado.'))
      .finally(() => setCargando(false));
  }, [anio]);

  const descargar = async () => {
    setDescargando(true);
    try {
      await descargarPdf('certificado-pdf', `Certificado-${certificado.numero}.pdf`);
    } catch (e) {
      console.error('Error al generar el PDF:', e);
      toast('No se pudo generar el PDF', 'error');
    } finally {
      setDescargando(false);
    }
  };

  return (
    <div className="fade-in">
      <PageHeader
        eyebrow="Donaciones"
        titulo="Mis certificados"
        subtitulo="Descarga el certificado anual de tus donaciones para desgravarlas en la declaración de la renta."
        acciones={
          <div className="field field-inline">
            <label htmlFor="anio">Año</label>
            <select id="anio" value={anio} onChange={(e) => setAnio(Number(e.target.value))}>
              {ultimosAnios(6).map((a) => <option key={a} value={a}>{a}</option>)}
            </select>
          </div>
        }
      />

      {cargando ? (
        <Cargando texto="Buscando tus donaciones..." />
      ) : error ? (
        <div className="card">
          <EstadoVacio
            icono="file-text"
            titulo={`Sin certificado para ${anio}`}
            texto={error}
            accion={
              error.includes('datos fiscales') ? (
                <Link to="/perfil" className="btn btn-primary btn-sm">Completar mi perfil</Link>
              ) : (
                <Link to={`/donar/${id}`} className="btn btn-accent btn-sm"><Icon name="heart" size={16} /> Hacer una donación</Link>
              )
            }
          />
        </div>
      ) : (
        <div className="factura-pagina">
          <div className="factura-barra">
            <span className="text-muted">{certificado.donaciones.length} donación(es) en {anio}</span>
            <button type="button" className="btn btn-primary" onClick={descargar} disabled={descargando}>
              <Icon name="download" size={16} /> {descargando ? 'Generando...' : 'Descargar PDF'}
            </button>
          </div>
          <CertificadoPapel certificado={certificado} />
        </div>
      )}
    </div>
  );
};

export default MisCertificados;
