import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import axios from 'axios';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';
import '../styles/FacturaDetalle.css';

const FacturaDetalle = () => {
  const { id } = useParams();
  const [factura, setFactura] = useState(null);

  useEffect(() => {
    axios.get(`http://localhost:8080/api/facturas/${id}`)
      .then(res => setFactura(res.data))
      .catch(err => console.error('Error al cargar la factura:', err));
  }, [id]);

  if (!factura) return <p className="loading">Cargando factura...</p>;

  const handleDownloadPDF = () => {
    const input = document.getElementById('factura-pdf');
    html2canvas(input).then((canvas) => {
      const imgData = canvas.toDataURL('image/png');
      const pdf = new jsPDF('p', 'mm', 'a4');
      const imgProps = pdf.getImageProperties(imgData);
      const pdfWidth = pdf.internal.pageSize.getWidth();
      const pdfHeight = (imgProps.height * pdfWidth) / imgProps.width;

      pdf.addImage(imgData, 'PNG', 0, 0, pdfWidth, pdfHeight);
      pdf.save(`Factura-${factura.numero}.pdf`);
    });
};


  return (
  <div className="factura-detalle-container">
    <div id="factura-pdf">
      <h2>Factura Nº {factura.numero}</h2>
      <p><strong>ONG:</strong> {factura.ong?.nombre}</p>
      <p><strong>Fecha:</strong> {factura.fecha}</p>
      <p><strong>Total:</strong> {parseFloat(factura.total).toFixed(2)} €</p>

      <h3>Detalles</h3>
      <table className="tabla-detalles">
        <thead>
          <tr>
            <th>Descripción</th>
            <th>Cantidad</th>
            <th>Precio (€)</th>
            <th>IVA (%)</th>
          </tr>
        </thead>
        <tbody>
          {factura.detalles.map(detalle => (
            <tr key={detalle.id}>
              <td>{detalle.descripcion}</td>
              <td>{detalle.cantidad}</td>
              <td>{parseFloat(detalle.precio).toFixed(2)}</td>
              <td>{parseFloat(detalle.iva).toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>

    <div className="descarga-pdf">
      <button onClick={handleDownloadPDF}>Descargar PDF</button>
      {factura.ong && (
  <Link to={`/ongs/${factura.ong.id}/facturas`}>← Volver a facturas</Link>
  )}
    </div>
  </div>
);

};

export default FacturaDetalle;
