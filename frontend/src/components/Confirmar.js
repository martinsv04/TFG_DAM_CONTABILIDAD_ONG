import React, { useEffect } from 'react';
import { createPortal } from 'react-dom';

// Ventana de confirmación (p. ej. antes de borrar). Se pinta en el <body> por la animación con transform de las páginas.
const Confirmar = ({ titulo, texto, textoBoton = 'Eliminar', enviando, alConfirmar, alCerrar }) => {
  useEffect(() => {
    const cerrarConEscape = (e) => e.key === 'Escape' && alCerrar();
    window.addEventListener('keydown', cerrarConEscape);
    return () => window.removeEventListener('keydown', cerrarConEscape);
  }, [alCerrar]);

  return createPortal(
    <div className="modal-fondo" onClick={alCerrar}>
      <div className="modal card card-pad" role="alertdialog" aria-modal="true" aria-labelledby="confirmar-titulo" onClick={(e) => e.stopPropagation()}>
        <h2 id="confirmar-titulo">{titulo}</h2>
        <p className="text-muted">{texto}</p>
        <div className="form-actions">
          <button type="button" className="btn btn-danger" onClick={alConfirmar} disabled={enviando}>
            {enviando ? 'Eliminando...' : textoBoton}
          </button>
          <button type="button" className="btn btn-ghost" onClick={alCerrar}>Cancelar</button>
        </div>
      </div>
    </div>,
    document.body
  );
};

export default Confirmar;
