import React, { useEffect, useState } from 'react';
import Icon from './Icons';

const EVENTO = 'ongestion:toast';

// Uso desde cualquier sitio: toast('Guardado correctamente', 'success')  (tipos: success, error, info)
export const toast = (mensaje, tipo = 'info') => {
  window.dispatchEvent(new CustomEvent(EVENTO, { detail: { mensaje, tipo, id: Date.now() + Math.random() } }));
};

const ICONO = { success: 'check-circle', error: 'alert-circle', info: 'info' };

// Se coloca una sola vez en App.js
export const Toaster = () => {
  const [avisos, setAvisos] = useState([]);

  useEffect(() => {
    const alRecibir = (evento) => {
      const aviso = evento.detail;
      setAvisos((previos) => [...previos, aviso]);
      setTimeout(() => setAvisos((previos) => previos.filter((a) => a.id !== aviso.id)), 4200);
    };
    window.addEventListener(EVENTO, alRecibir);
    return () => window.removeEventListener(EVENTO, alRecibir);
  }, []);

  return (
    <div className="toaster" role="status" aria-live="polite">
      {avisos.map((aviso) => (
        <div key={aviso.id} className={`toast toast-${aviso.tipo}`}>
          <Icon name={ICONO[aviso.tipo] || 'info'} size={18} />
          <span>{aviso.mensaje}</span>
        </div>
      ))}
    </div>
  );
};
