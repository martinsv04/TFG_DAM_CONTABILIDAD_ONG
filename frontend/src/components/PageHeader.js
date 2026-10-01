import React from 'react';

// Cabecera común de las páginas internas: título, texto de apoyo y acciones a la derecha
const PageHeader = ({ eyebrow, titulo, subtitulo, acciones }) => (
  <div className="page-header">
    <div>
      {eyebrow && <p className="page-eyebrow">{eyebrow}</p>}
      <h1>{titulo}</h1>
      {subtitulo && <p className="page-sub">{subtitulo}</p>}
    </div>
    {acciones && <div className="page-actions">{acciones}</div>}
  </div>
);

export default PageHeader;
