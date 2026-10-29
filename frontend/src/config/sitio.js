// Datos del sitio y del titular. Edita solo este archivo para cambiar el dominio y los datos legales
// (también hay que poner el dominio en public/sitemap.xml y public/robots.txt).
export const SITIO = {
  nombre: 'ONGestión',
  url: 'https://ongestion.example', // [COMPLETAR] dominio definitivo, sin barra final
  descripcion: 'ONGestión: plataforma de gestión contable para ONGs. Ingresos, gastos, facturas, certificados de donación e informes en un solo lugar.',
};

// Titular del servicio: los textos de privacidad y términos se rellenan con estos datos
export const TITULAR = {
  nombre: '[COMPLETAR: nombre o razón social del titular]',
  nif: '[COMPLETAR: NIF/CIF]',
  direccion: '[COMPLETAR: dirección postal]',
  email: '[COMPLETAR: correo de contacto]',
};

export const ULTIMA_ACTUALIZACION = '5 de octubre de 2026';

// Un valor sin completar se resalta en las páginas legales
export const sinCompletar = (valor) => String(valor).startsWith('[COMPLETAR');
