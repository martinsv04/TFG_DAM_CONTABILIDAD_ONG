// Exportación a CSV pensada para abrirse directamente en Excel en español:
// separador ";", decimales con coma, codificación UTF-8 con BOM y fin de línea CRLF.

const comoTexto = (valor) => {
  if (valor === null || valor === undefined) return '';
  if (typeof valor === 'number') return Number.isFinite(valor) ? String(valor).replace('.', ',') : '';
  if (typeof valor === 'boolean') return valor ? 'Sí' : 'No';
  let texto = String(valor);
  // Evita que una hoja de cálculo interprete un texto como fórmula
  if (/^[=+\-@\t\r]/.test(texto)) texto = `'${texto}`;
  return texto;
};

const celda = (valor) => {
  const texto = comoTexto(valor);
  return /[";\r\n]/.test(texto) ? `"${texto.replace(/"/g, '""')}"` : texto;
};

// columnas: [{ titulo: 'Fecha', valor: (fila) => fila.fecha }]
export const generarCsv = (columnas, filas) => {
  const cabecera = columnas.map((c) => celda(c.titulo)).join(';');
  const cuerpo = filas.map((fila) => columnas.map((c) => celda(c.valor(fila))).join(';'));
  return [cabecera, ...cuerpo].join('\r\n') + '\r\n';
};

export const descargarCsv = (nombreFichero, columnas, filas) => {
  const blob = new Blob(['﻿' + generarCsv(columnas, filas)], { type: 'text/csv;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const enlace = document.createElement('a');
  enlace.href = url;
  enlace.download = nombreFichero;
  document.body.appendChild(enlace);
  enlace.click();
  enlace.remove();
  URL.revokeObjectURL(url);
};

// 2026-03-05 -> 05/03/2026 (Excel lo reconoce como fecha)
export const fechaCsv = (fecha) => {
  if (!fecha) return '';
  const partes = String(fecha).split('T')[0].split('-');
  return partes.length === 3 ? `${partes[2]}/${partes[1]}/${partes[0]}` : String(fecha);
};
