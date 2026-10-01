import React from 'react';
import { Navigate } from 'react-router-dom';
import { haySesionValida, cerrarSesion, getRol } from '../services/authService';

// Uso: <PrivateRoute>...</PrivateRoute>               -> cualquier usuario con sesión
//      <PrivateRoute roles={['ADMIN']}>...</PrivateRoute> -> solo esos roles
// La comprobación real de permisos la hace el backend; esto solo evita mostrar pantallas que no corresponden.
const PrivateRoute = ({ children, roles }) => {
  if (!haySesionValida()) {
    cerrarSesion();
    return <Navigate to="/iniciar-sesion" replace />;
  }

  if (roles && !roles.includes(getRol())) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};

export default PrivateRoute;
