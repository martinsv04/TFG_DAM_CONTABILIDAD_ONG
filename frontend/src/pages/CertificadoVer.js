import React, { useEffect, useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import CertificadoPapel from '../components/CertificadoPapel';
import { Cargando, EstadoVacio } from '../components/Estados';
import { toast } from '../components/Toast';
import { descargarPdf } from '../utils/pdf';

const API = 'http://localhost:8080/api/certificados';

// Vista previa y descarga de un certificado (de una donación o anual) desde el área de la ONG.
const CertificadoVer = () => {
  const { id, ingresoId, usuarioId } = useParams(); // id = ONG
  const [params] = useSearchParams();
  const anio = params.get('anio');
  const [certificado, setCertificado] = useState(null);
  const [error, setError] = useState('');
  const [descargando, setDescargando] = useState(false);

  useEffect(() => {
    const url = ingresoId
      ? `${API}/ong/${id}/donacion/${ingresoId}`
      : `${API}/ong/${id}/anual/${usuarioId}${anio ? `?anio=${anio}` : ''}`;
    axios
      .get(url)
      .then((res) => setCertificado(res.data))
      .catch((err) => setError(err.response?.data?.message || 'No se pudo generar el certificado.'));
  }, [id, ingresoId, usuarioId, anio]);

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

  const volver = (
    <Link to={`/ongs/${id}/certificados`} className="btn btn-ghost btn-sm">
      <Icon name="arrow-left" size={16} /> Volver a certificados
    </Link>
  );

  if (error) {
    return (
      <div className="fade-in">
        <div className="factura-barra">{volver}</div>
        <div className="card">
          <EstadoVacio icono="alert-circle" titulo="No se puede emitir este certificado" texto={error} />
        </div>
      </div>
    );
  }
  if (!certificado) return <Cargando texto="Preparando certificado..." />;

  return (
    <div className="factura-pagina fade-in">
      <div className="factura-barra">
        {volver}
        <button type="button" className="btn btn-primary" onClick={descargar} disabled={descargando}>
          <Icon name="download" size={16} /> {descargando ? 'Generando...' : 'Descargar PDF'}
        </button>
      </div>
      <CertificadoPapel certificado={certificado} />
      <p className="certificado-aviso">Revisa el texto con tu gestor o asesor fiscal antes de usarlo oficialmente.</p>
    </div>
  );
};

export default CertificadoVer;
