// Cargos habituales (sirven de sugerencia al escribir) y orden en el que se muestran
export const CARGOS_GOBIERNO = ['Presidente/a', 'Vicepresidente/a', 'Secretario/a', 'Tesorero/a', 'Vocal'];
export const CARGOS_DIRECCION = ['Director/a', 'Gerente', 'Coordinador/a', 'Responsable de administración'];

export const ORGANOS = {
  GOBIERNO: 'Órgano de gobierno',
  DIRECCION: 'Equipo de dirección',
};

const ORDEN_CARGOS = ['presidente', 'presidenta', 'vicepresidente', 'vicepresidenta', 'secretari', 'tesorer', 'vocal', 'director', 'directora', 'gerente'];

// Presidente primero, luego vice, secretario, tesorero, vocales...; el resto por orden manual y nombre
export const ordenar = (lista) => {
  const rango = (m) => {
    const cargo = (m.cargo || '').toLowerCase();
    const i = ORDEN_CARGOS.findIndex((c) => cargo.startsWith(c));
    return i === -1 ? ORDEN_CARGOS.length : i;
  };
  return [...lista].sort((a, b) => (
    (a.orden ?? 999) - (b.orden ?? 999) || rango(a) - rango(b) || a.nombre.localeCompare(b.nombre, 'es')
  ));
};
