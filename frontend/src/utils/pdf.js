import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

// Convierte un elemento de la página en un PDF A4 (con varias páginas si es largo) y lo descarga
export const descargarPdf = async (idElemento, nombreArchivo) => {
  const elemento = document.getElementById(idElemento);
  if (!elemento) return;

  const canvas = await html2canvas(elemento, { scale: 2, backgroundColor: '#ffffff' });
  const imagen = canvas.toDataURL('image/png');
  const pdf = new jsPDF('p', 'mm', 'a4');
  const ancho = pdf.internal.pageSize.getWidth();
  const altoPagina = pdf.internal.pageSize.getHeight();
  const alto = (canvas.height * ancho) / canvas.width;

  let restante = alto;
  let posicion = 0;
  pdf.addImage(imagen, 'PNG', 0, posicion, ancho, alto);
  restante -= altoPagina;
  while (restante > 0) {
    posicion = restante - alto;
    pdf.addPage();
    pdf.addImage(imagen, 'PNG', 0, posicion, ancho, alto);
    restante -= altoPagina;
  }
  pdf.save(nombreArchivo);
};
