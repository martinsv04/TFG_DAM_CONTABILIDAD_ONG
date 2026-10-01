import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import PageHeader from '../components/PageHeader';
import { Cargando, EstadoVacio } from '../components/Estados';
import { iniciales, ETIQUETA_ROL } from '../utils/format';

const Dashboard = () => {
  const navigate = useNavigate();
  const [rol, setRol] = useState('');
  const [ongs, setOngs] = useState([]);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    setRol(localStorage.getItem('rol'));

    const fetchOngs = async () => {
      try {
        const userId = localStorage.getItem('userId');
        const response = await axios.get(`http://localhost:8080/api/ongs/usuario/${userId}`);
        setOngs(response.data);
      } catch (error) {
        console.error('Error cargando ONGs:', error);
      } finally {
        setCargando(false);
      }
    };

    fetchOngs();
  }, []);

  const puedeVerEconomia = rol === 'ADMIN' || rol === 'CONTABLE';

  return (
    <div className="fade-in">
      <PageHeader
        eyebrow={ETIQUETA_ROL[rol] || 'Panel'}
        titulo="Tus organizaciones"
        subtitulo="Elige una ONG para ver su equipo, sus cuentas y sus informes."
      />

      {cargando ? (
        <Cargando texto="Cargando tus ONGs..." />
      ) : ongs.length === 0 ? (
        <div className="card">
          <EstadoVacio
            icono="briefcase"
            titulo="Todavía no tienes ninguna ONG"
            texto="Cuando un administrador te añada a una organización aparecerá aquí."
          />
        </div>
      ) : (
        <div className="ong-grid">
          {ongs.map((ong) => (
            <article key={ong.id} className="ong-card card card-hover">
              <div className="ong-card-top">
                <span className="avatar avatar-lg">{iniciales(ong.nombre)}</span>
                <div className="ong-card-titulo">
                  <h3><Link to={`/ong/${ong.id}`}>{ong.nombre}</Link></h3>
                  {ong.direccion && (
                    <p className="ong-meta"><Icon name="map-pin" size={15} /> {ong.direccion}</p>
                  )}
                </div>
              </div>

              {ong.descripcion && <p className="ong-desc">{ong.descripcion}</p>}

              <div className="ong-card-acciones">
                <button type="button" className="btn btn-secondary btn-sm" onClick={() => navigate(`/ong/${ong.id}`)}>
                  Ver ONG
                </button>
                {puedeVerEconomia && (
                  <button type="button" className="btn btn-primary btn-sm" onClick={() => navigate(`/area-economica/${ong.id}`)}>
                    <Icon name="wallet" size={16} /> Área económica
                  </button>
                )}
                {rol === 'DONANTE' && (
                  <button type="button" className="btn btn-accent btn-sm" onClick={() => navigate(`/donar/${ong.id}`)}>
                    <Icon name="heart" size={16} /> Donar
                  </button>
                )}
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
};

export default Dashboard;
