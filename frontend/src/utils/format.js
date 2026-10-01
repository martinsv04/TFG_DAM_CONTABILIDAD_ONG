// Formatos comunes para importes y fechas (en castellano)

// useGrouping: 'always' fuerza el separador de miles también en importes de cuatro cifras (6.840,00 €)
const euro = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR', useGrouping: 'always' });
const decimales = new Intl.NumberFormat('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2, useGrouping: 'always' });

export const formatoNumero = (valor) => {
  const numero = parseFloat(valor);
  return Number.isNaN(numero) ? '-' : decimales.format(numero);
};

export const formatoEuro = (valor) => {
  const numero = parseFloat(valor);
  return euro.format(Number.isNaN(numero) ? 0 : numero);
};

export const formatoFecha = (fecha) => {
  if (!fecha) return '-';
  // Las fechas llegan como "2025-04-28"; se construyen a mediodía para evitar desfases de zona horaria
  const partes = String(fecha).split('T')[0].split('-');
  if (partes.length !== 3) return String(fecha);
  const date = new Date(Number(partes[0]), Number(partes[1]) - 1, Number(partes[2]), 12);
  return date.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' });
};

export const iniciales = (nombre) => {
  if (!nombre) return '?';
  const palabras = String(nombre).trim().split(/\s+/);
  return (palabras[0][0] + (palabras[1] ? palabras[1][0] : '')).toUpperCase();
};

// Últimos N años empezando por el actual (para los selectores de los informes)
export const ultimosAnios = (cantidad = 6) => {
  const actual = new Date().getFullYear();
  return Array.from({ length: cantidad }, (_, i) => actual - i);
};

export const ETIQUETA_ROL = {
  ADMIN: 'Administrador',
  CONTABLE: 'Contable',
  DONANTE: 'Donante',
  VOLUNTARIO: 'Voluntario',
};
