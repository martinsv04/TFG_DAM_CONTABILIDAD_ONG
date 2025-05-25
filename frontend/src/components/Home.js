import React from 'react';
import { useNavigate } from 'react-router-dom';
import './Home.css';

function Home() {
  const navigate = useNavigate();

  return (
    <div className="home-container">
      <h1>Bienvenido a ONGestión</h1>
      <div className="button-group">
        <button className="btn" onClick={() => navigate('/registrarse')}>Registrarse</button>
        <button className="btn" onClick={() => navigate('/iniciar-sesion')}>Iniciar Sesión</button>
      </div>
    </div>
  );
}

export default Home;
