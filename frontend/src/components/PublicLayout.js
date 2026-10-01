import React from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Navbar from './Navbar';
import Footer from './Footer';

// Páginas públicas. Inicio lleva barra superior y pie; login y contacto tienen su propio diseño a pantalla completa.
const PANTALLA_COMPLETA = ['/iniciar-sesion', '/registrarse'];

function PublicLayout() {
  const { pathname } = useLocation();
  const completa = PANTALLA_COMPLETA.includes(pathname);

  return (
    <>
      {!completa && <Navbar />}
      <Outlet />
      {!completa && <Footer />}
    </>
  );
}

export default PublicLayout;
