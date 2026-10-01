import React, { useContext, useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import axios from 'axios';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';
import Icon from '../components/Icons';
import { Cargando, EstadoVacio } from '../components/Estados';
import { formatoEuro, formatoFecha, formatoNumero } from '../utils/format';
import { OngContext } from '../components/OngContext';

const FacturaDetalle = () => {
  const { id } = useParams();
  const [factura, setFactura] = useState(null);
  const [error, setError] = useState(false);
  const { fijarOng } = useContext(OngContext);

  useEffect(() => {
    axios
      .get(`http://localhost:8080/api/facturas/${id}`)
      .then((res) => setFactura(res.data))
      .catch((err) => {
        console.error('Error al cargar la factura:', err);
        setError(true);
      });
  }, [id]);

  // La barra lateral muestra el menú de la ONG a la que pertenece la factura
  useEffect(() => {
    if (factura && factura.ong && factura.ong.id) {
      fijarOng(String(factura.ong.id));
    }
  }, [factura, fijarOng]);

  if (error) {
    return (
      <div className="card">
        <EstadoVacio icono="alert-circle" titulo="No se pudo cargar la factura" texto="Puede que no exista o que no tengas acceso a ella." />
      </div>
    );
  }
  if (!factura) return <Cargando texto="Cargando factura..." />;

  // Una factura puede no tener líneas de detalle (por ejemplo, las creadas al registrar un ingreso o un gasto)
  const detalles = factura.detalles || [];
  const formatear = (valor) => (valor === null || valor === undefined || valor === '' ? '-' : formatoNumero(valor));

  const handleDownloadPDF = () => {
    const input = document.getElementById('factura-pdf');
    html2canvas(input, { scale: 2, backgroundColor: '#ffffff' }).then((canvas) => {
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
    <div className="factura-pagina fade-in">
      <div className="factura-barra">
        {factura.ong && (
          <Link to={`/ongs/${factura.ong.id}/facturas`} className="btn btn-ghost btn-sm">
            <Icon name="arrow-left" size={16} /> Volver a facturas
          </Link>
        )}
        <button type="button" className="btn btn-primary" onClick={handleDownloadPDF}>
          <Icon name="download" size={16} /> Descargar PDF
        </button>
      </div>

      <div id="factura-pdf" className="factura-papel">
        <div className="factura-cabecera">
          <div>
            <p className="factura-etiqueta">Factura</p>
            <h2>Nº {factura.numero}</h2>
          </div>
          <div className="factura-ong">
            <strong>{factura.ong?.nombre}</strong>
            <span>{formatoFecha(factura.fecha)}</span>
          </div>
        </div>

        <table className="factura-tabla">
          <thead>
            <tr>
              <th>Descripción</th>
              <th className="num">Cantidad</th>
              <th className="num">Precio (€)</th>
              <th className="num">IVA (%)</th>
            </tr>
          </thead>
          <tbody>
            {detalles.length === 0 ? (
              <tr>
                <td colSpan="4" className="factura-sin-detalle">Esta factura no tiene líneas de detalle.</td>
              </tr>
            ) : (
              detalles.map((detalle) => (
                <tr key={detalle.id}>
                  <td>{detalle.descripcion}</td>
                  <td className="num">{detalle.cantidad}</td>
                  <td className="num">{formatear(detalle.precio)}</td>
                  <td className="num">{formatear(detalle.iva)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>

        <div className="factura-total">
          <span>Total</span>
          <strong>{formatoEuro(factura.total)}</strong>
        </div>
      </div>
    </div>
  );
};

export default FacturaDetalle;
