import React, { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import axios from 'axios';
import InformeTransparencia from '../components/InformeTransparencia';
import Organigrama from '../components/Organigrama';
import { Cargando, EstadoVacio } from '../components/Estados';
import { formatoFechaLarga } from '../utils/format';

const API = 'http://localhost:8080/api';

// Página abierta a cualquiera: cuentas, origen de la financiación y gobierno de una ONG que ha decidido publicarlos
const TransparenciaPublica = () => {
  const { id } = useParams();
  const [pagina, setPagina] = useState(null);
  const [noDisponible, setNoDisponible] = useState(false);

  const cargar = (anio) => {
    axios
      .get(`${API}/publico/transparencia/${id}`, { params: anio ? { anio } : {} })
      .then((res) => setPagina(res.data))
      .catch(() => setNoDisponible(true));
  };

  useEffect(() => {
    cargar(null);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  if (noDisponible) {
    return (
      <main className="publica-pagina">
        <div className="publica-inner">
          <EstadoVacio
            icono="eye-off"
            titulo="Esta página no está disponible"
            texto="La entidad no ha publicado su información de transparencia o el enlace no es correcto."
            accion={<Link to="/" className="btn btn-secondary">Ir al inicio</Link>}
          />
        </div>
      </main>
    );
  }
  if (!pagina) {
    return <main className="publica-pagina"><div className="publica-inner"><Cargando texto="Cargando..." /></div></main>;
  }

  const { datos } = pagina;
  return (
    <main className="publica-pagina">
      <div className="publica-inner">
        <header className="publica-cabecera">
          <p className="page-eyebrow">Transparencia</p>
          <h1>{pagina.nombre}</h1>
          {pagina.descripcion && <p className="publica-desc">{pagina.descripcion}</p>}
          <dl className="publica-ficha">
            {pagina.nif && <div><dt>NIF</dt><dd>{pagina.nif}</dd></div>}
            {pagina.fechaCreacion && <div><dt>Constituida</dt><dd>{formatoFechaLarga(pagina.fechaCreacion)}</dd></div>}
            {pagina.direccion && <div><dt>Dirección</dt><dd>{pagina.direccion}</dd></div>}
            {pagina.email && <div><dt>Contacto</dt><dd>{pagina.email}</dd></div>}
          </dl>
        </header>

        <div className="publica-anio">
          <h2>Cuentas del año</h2>
          <label className="tr-anio">
            <span className="text-muted">Año</span>
            <select value={datos.anio} onChange={(e) => cargar(Number(e.target.value))}>
              {datos.anios.map((a) => <option key={a} value={a}>{a}</option>)}
            </select>
          </label>
        </div>
        <InformeTransparencia datos={datos} />

        <h2 className="publica-h2">Órgano de gobierno y dirección</h2>
        {pagina.gobierno.length === 0 ? (
          <p className="text-muted">La entidad todavía no ha publicado la composición de su órgano de gobierno.</p>
        ) : (
          <Organigrama personas={pagina.gobierno} />
        )}

        <p className="publica-pie text-muted">
          Información generada con ONGestión a partir de la contabilidad de la entidad. Los datos los publica y es responsable de ellos la propia entidad.
        </p>
      </div>
    </main>
  );
};

export default TransparenciaPublica;
