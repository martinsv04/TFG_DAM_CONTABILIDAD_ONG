import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { leerConsentimiento, guardarConsentimiento, EVENTO_ABRIR } from '../utils/cookies';

// Aviso de cookies y almacenamiento local. Se abre solo si no hay una elección guardada,
// o desde el pie de página ("Configurar cookies").
function BannerCookies() {
  const [visible, setVisible] = useState(() => leerConsentimiento() === null);
  const [preferencias, setPreferencias] = useState(false);
  const [analitica, setAnalitica] = useState(() => Boolean(leerConsentimiento()?.analitica));

  useEffect(() => {
    const abrir = () => {
      setAnalitica(Boolean(leerConsentimiento()?.analitica));
      setPreferencias(true);
      setVisible(true);
    };
    window.addEventListener(EVENTO_ABRIR, abrir);
    return () => window.removeEventListener(EVENTO_ABRIR, abrir);
  }, []);

  if (!visible) return null;

  const decidir = (acepta) => {
    guardarConsentimiento({ analitica: acepta });
    setVisible(false);
    setPreferencias(false);
  };

  return (
    <section className="cookies" role="region" aria-label="Aviso de cookies">
      <div className="cookies-cuerpo">
        <h2>Tu privacidad</h2>
        <p>
          ONGestión solo usa almacenamiento técnico necesario para mantener tu sesión iniciada y recordar esta elección.
          No usamos cookies de publicidad ni de seguimiento. Más información en la{' '}
          <Link to="/privacidad#cookies">política de privacidad</Link>.
        </p>

        {preferencias && (
          <div className="cookies-opciones">
            <label className="cookies-opcion">
              <input type="checkbox" checked disabled />
              <span>
                <strong>Necesarias</strong>
                <small>Sesión de usuario y preferencias de este aviso. Siempre activas.</small>
              </span>
            </label>
            <label className="cookies-opcion">
              <input type="checkbox" checked={analitica} onChange={(e) => setAnalitica(e.target.checked)} />
              <span>
                <strong>Analítica</strong>
                <small>Hoy no se utiliza. Si algún día se añade, solo se activará si la aceptas.</small>
              </span>
            </label>
          </div>
        )}

        <div className="cookies-acciones">
          {preferencias ? (
            <button type="button" className="btn btn-primary btn-sm" onClick={() => decidir(analitica)}>Guardar preferencias</button>
          ) : (
            <>
              <button type="button" className="btn btn-primary btn-sm" onClick={() => decidir(true)}>Aceptar todas</button>
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => decidir(false)}>Rechazar opcionales</button>
              <button type="button" className="btn btn-ghost btn-sm" onClick={() => setPreferencias(true)}>Configurar</button>
            </>
          )}
        </div>
      </div>
    </section>
  );
}

export default BannerCookies;
