// Estados de proyectos y subvenciones: texto que se muestra y color de la etiqueta
export const ESTADOS_PROYECTO = {
  PLANIFICADO: { texto: 'Planificado', badge: 'badge-info' },
  EN_CURSO: { texto: 'En curso', badge: 'badge-primary' },
  FINALIZADO: { texto: 'Finalizado', badge: '' },
};

export const ESTADOS_SUBVENCION = {
  SOLICITADA: { texto: 'Solicitada', badge: 'badge-info' },
  CONCEDIDA: { texto: 'Concedida', badge: 'badge-success' },
  JUSTIFICADA: { texto: 'Justificada', badge: 'badge-primary' },
  DENEGADA: { texto: 'Denegada', badge: 'badge-danger' },
};

// "" -> null, para enviar al backend solo lo que se ha rellenado
export const numeroONull = (valor) => (valor === '' || valor === null || valor === undefined ? null : Number(valor));
export const textoONull = (valor) => (valor === '' || valor === undefined ? null : valor);
