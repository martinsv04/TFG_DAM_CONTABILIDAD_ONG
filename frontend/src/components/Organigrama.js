import React from 'react';
import Icon from './Icons';
import { ORGANOS, ordenar } from '../utils/gobierno';
import { iniciales, formatoFecha } from '../utils/format';

const Tarjeta = ({ persona, acciones }) => (
  <article className="org-tarjeta card">
    <span className="avatar avatar-round">{iniciales(persona.nombre)}</span>
    <div className="org-datos">
      <strong>{persona.nombre}</strong>
      <span className="org-cargo">{persona.cargo}</span>
      {persona.fechaNombramiento && <small className="text-muted">Desde {formatoFecha(persona.fechaNombramiento)}</small>}
      {persona.descripcion && <small className="org-desc">{persona.descripcion}</small>}
    </div>
    {acciones && <div className="org-acciones">{acciones(persona)}</div>}
  </article>
);

// Dos niveles: órgano de gobierno arriba y equipo de dirección debajo. acciones(persona) añade botones a cada tarjeta.
const Organigrama = ({ personas, acciones, vacioGobierno, vacioDireccion }) => {
  const nivel = (organo, vacio) => {
    const lista = ordenar(personas.filter((p) => p.organo === organo));
    return (
      <section className="org-nivel" aria-label={ORGANOS[organo]}>
        <h3 className="org-titulo"><Icon name="users" size={16} /> {ORGANOS[organo]}</h3>
        {lista.length === 0 ? (
          <p className="text-muted org-vacio">{vacio || 'Sin personas registradas.'}</p>
        ) : (
          <div className="org-fila">
            {lista.map((p) => <Tarjeta key={p.id} persona={p} acciones={acciones} />)}
          </div>
        )}
      </section>
    );
  };

  return (
    <div className="organigrama">
      {nivel('GOBIERNO', vacioGobierno)}
      <div className="org-conector" aria-hidden="true" />
      {nivel('DIRECCION', vacioDireccion)}
    </div>
  );
};

export default Organigrama;
