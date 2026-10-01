import React, { useCallback, useEffect, useState } from 'react';
import { NavLink, Outlet, matchPath, useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Logo from './Logo';
import Icon from './Icons';
import { cerrarSesion, getRol } from '../services/authService';
import { ETIQUETA_ROL } from '../utils/format';
import { OngContext } from './OngContext';
import '../styles/layout.css';

// Rutas que pertenecen a una ONG concreta (el :id es el de la ONG).
// Sirven para mostrar en la barra lateral el menú de "esta ONG".
const RUTAS_DE_ONG = [
  '/ong/:id',
  '/editar-ong/:id',
  '/add-member/:id',
  '/area-economica/:id',
  '/ongs/:id/*',
  '/informe/resultados/:id',
  '/informe/balance/:id',
  '/donar/:id',
];

const ongIdDeLaRuta = (pathname) => {
  for (const patron of RUTAS_DE_ONG) {
    const coincidencia = matchPath({ path: patron, end: false }, pathname);
    if (coincidencia && coincidencia.params.id) return coincidencia.params.id;
  }
  return null;
};

const Enlace = ({ to, icono, children }) => (
  <NavLink to={to} className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
    <Icon name={icono} size={19} />
    <span>{children}</span>
  </NavLink>
);

function AppLayout() {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const rol = getRol();
  const [ongExtra, setOngExtra] = useState(null);
  const ongId = ongIdDeLaRuta(pathname) || ongExtra;
  const fijarOng = useCallback((valor) => setOngExtra(valor), []);
  const [nombreOng, setNombreOng] = useState('');
  const [menuAbierto, setMenuAbierto] = useState(false);

  // Cierra el menú lateral al cambiar de página (en móvil)
  useEffect(() => {
    setMenuAbierto(false);
  }, [pathname]);

  // Al cambiar de página se olvida la ONG indicada por la página anterior
  useEffect(() => {
    setOngExtra(null);
  }, [pathname]);

  // Nombre de la ONG que se está viendo
  useEffect(() => {
    let activo = true;
    setNombreOng('');
    if (ongId) {
      axios
        .get(`http://localhost:8080/api/ongs/${ongId}`)
        .then((res) => activo && setNombreOng(res.data.nombre || ''))
        .catch(() => {});
    }
    return () => {
      activo = false;
    };
  }, [ongId]);

  const cerrar = () => {
    cerrarSesion();
    navigate('/iniciar-sesion');
  };

  const puedeVerEconomia = rol === 'ADMIN' || rol === 'CONTABLE';

  return (
    <div className="app-shell">
      <aside className={`sidebar${menuAbierto ? ' open' : ''}`} aria-label="Navegación principal">
        <div className="sidebar-top">
          <Logo to="/dashboard" />
        </div>

        <nav className="sidebar-nav">
          <p className="nav-section">General</p>
          <Enlace to="/dashboard" icono="home">Panel</Enlace>

          {ongId && (
            <>
              <p className="nav-section">
                Esta ONG
                {nombreOng && <span className="nav-ong-nombre">{nombreOng}</span>}
              </p>
              <Enlace to={`/ong/${ongId}`} icono="users">Datos y equipo</Enlace>

              {puedeVerEconomia && (
                <>
                  <Enlace to={`/area-economica/${ongId}`} icono="wallet">Área económica</Enlace>
                  <Enlace to={`/ongs/${ongId}/facturas`} icono="file-text">Facturas</Enlace>
                  <Enlace to={`/informe/balance/${ongId}`} icono="pie-chart">Balance general</Enlace>
                  <Enlace to={`/informe/resultados/${ongId}`} icono="bar-chart">Estado de resultados</Enlace>
                </>
              )}

              {rol === 'DONANTE' && <Enlace to={`/donar/${ongId}`} icono="heart">Hacer una donación</Enlace>}

              {rol === 'ADMIN' && (
                <>
                  <Enlace to={`/add-member/${ongId}`} icono="user-plus">Añadir miembro</Enlace>
                  <Enlace to={`/editar-ong/${ongId}`} icono="edit">Editar ONG</Enlace>
                </>
              )}
            </>
          )}
        </nav>

        <div className="sidebar-bottom">
          <div className="usuario-chip">
            <span className="avatar avatar-round">{(rol || '?')[0]}</span>
            <div>
              <p className="usuario-rol">{ETIQUETA_ROL[rol] || 'Usuario'}</p>
              <p className="usuario-sub">Sesión iniciada</p>
            </div>
          </div>
          <button type="button" className="nav-link nav-salir" onClick={cerrar}>
            <Icon name="log-out" size={19} />
            <span>Cerrar sesión</span>
          </button>
        </div>
      </aside>

      {menuAbierto && <div className="sidebar-overlay" onClick={() => setMenuAbierto(false)} aria-hidden="true" />}

      <div className="app-main">
        <header className="topbar">
          <button
            type="button"
            className="btn btn-ghost btn-sm topbar-menu"
            onClick={() => setMenuAbierto(true)}
            aria-label="Abrir menú"
          >
            <Icon name="menu" size={20} />
          </button>

          {pathname !== '/dashboard' && (
            <button type="button" className="btn btn-ghost btn-sm" onClick={() => navigate(-1)}>
              <Icon name="arrow-left" size={16} />
              Volver
            </button>
          )}

          <div className="topbar-space" />

          <span className="badge badge-primary">{ETIQUETA_ROL[rol] || 'Usuario'}</span>
        </header>

        <main className="content">
          <OngContext.Provider value={{ fijarOng }}>
            <Outlet />
          </OngContext.Provider>
        </main>
      </div>
    </div>
  );
}

export default AppLayout;
