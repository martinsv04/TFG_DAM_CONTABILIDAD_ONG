import React from 'react';
import { Link, useNavigate } from 'react-router-dom'; // 👈 También importamos useNavigate
import './Navbar.css';

function Navbar() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('rol'); // 👈 Eliminamos el rol guardado
    navigate('/iniciar-sesion');    // 👈 Redirigimos al login
  };

  return (
    <nav className="navbar">
      <div className="navbar-logo">
        <Link to="/">ONGestión</Link>
      </div>
      <ul className="navbar-links">
        <li><a href="#">Conócenos</a></li>
        <li><a href="#">Ayuda</a></li>
        <li><a href="#">Contacto</a></li>
        <li>
          <button onClick={handleLogout} className="logout-button">
            Cerrar Sesión
          </button>
        </li>
      </ul>
    </nav>
  );
}

export default Navbar;
