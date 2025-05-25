import React from 'react';
import { Navigate } from 'react-router-dom';

const PrivateRoute = ({ children }) => {
  const rol = localStorage.getItem('rol');

  if (!rol) {
    return <Navigate to="/iniciar-sesion" />;
  }

  return children;
};

export default PrivateRoute;
