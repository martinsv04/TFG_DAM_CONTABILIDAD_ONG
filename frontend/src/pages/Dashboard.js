import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/Dashboard.css';

const Dashboard = () => {
  const navigate = useNavigate();
  const [rol, setRol] = useState('');
  const [ongs, setOngs] = useState([]);

  useEffect(() => {
    const storedRol = localStorage.getItem('rol');
    setRol(storedRol);

    const fetchOngs = async () => {
      try {
        const userId = localStorage.getItem('userId');
        const response = await axios.get(`http://localhost:8080/api/ongs/usuario/${userId}`);
        setOngs(response.data);
      } catch (error) {
        console.error('Error cargando ONGs:', error);
      }
    };

    fetchOngs();
  }, []);

  const irAreaEconomica = (ongId) => {
    navigate(`/area-economica/${ongId}`);
  };

  const irVistaOng = (ongId) => {
    navigate(`/ong/${ongId}`);
  };

  return (
    <div className="dashboard-container">
      <h1>Bienvenido al Dashboard</h1>

      <div className="dashboard-ongs-section">
        <h2>Tus ONGs</h2>

        {ongs.map(ong => (
          <div 
            key={ong.id} 
            className={rol === 'ADMIN' ? 'ong-card-admin' : 'ong-card-user'}
          >
            <h3 
              className="ong-name"
              onClick={() => irVistaOng(ong.id)}
            >
              {ong.nombre}
            </h3>
            <p>Dirección: {ong.direccion}</p>

            {(rol === 'ADMIN' || rol === 'CONTABLE') && (
                <button
                    onClick={() => irAreaEconomica(ong.id)}
                    className="area-economica-button"
                >
                  Área Económica
                </button>
            )}

          </div>
        ))}
      </div>
    </div>
  );
};

export default Dashboard;
