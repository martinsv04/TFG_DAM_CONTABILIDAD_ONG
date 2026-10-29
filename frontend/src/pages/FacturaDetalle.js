import React, { useContext, useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import axios from 'axios';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';
import Icon from '../components/Icons';
import { Cargando, EstadoVacio } from '../components/Estados';
import { formatoEuro, formatoFecha, formatoNumero } from '../utils/format';
import { OngContext } from '../components/OngContext';
import { textoCuenta } from '../utils/cuentas';

const FacturaDetalle = () => {
  const { id } = useParams();
  const [factura, setFactura] = useState(null);
  const [error, setError] = useState(false);
  // Qué datos opcionales aparecen en la factura (y en su PDF)
  const [verEntidad, setVerEntidad] = useState(true);
  const [verContable, setVerContable] = useState(true);
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
  const movimiento = factura.ingreso || factura.gasto || null;
  const esIngreso = Boolean(factura.ingreso);
  const ong = factura.ong || {};
  const contacto = [ong.email, ong.telefono].filter(Boolean).join(' · ');
  const datosContables = movimiento
    ? [
        ['Tipo de movimiento', esIngreso ? 'Ingreso' : 'Gasto'],
        [esIngreso ? 'Tipo de ingreso' : 'Categoría', esIngreso ? movimiento.tipo : movimiento.categoria],
        ['Cuenta contable', textoCuenta(movimiento.cuenta)],
        ['Proyecto', movimiento.proyecto?.nombre],
        ['Subvención', movimiento.subvencion?.nombre],
        ['Fecha del movimiento', movimiento.fecha ? formatoFecha(movimiento.fecha) : ''],
      ].filter(([, valor]) => valor)
    : [];
  // Desglose: la base es la suma de las líneas y la cuota lo que falta hasta el total (el total ya lleva el IVA incluido)
  const base = detalles.reduce((suma, d) => suma + Number(d.precio || 0) * Number(d.cantidad || 1), 0);
  const cuota = Math.round((Number(factura.total) - base + Number.EPSILON) * 100) / 100;
  const tiposIva = [...new Set(detalles.map((d) => Number(d.iva || 0)))];
  const conIva = cuota !== 0 || tiposIva.some((t) => t > 0);
  const etiquetaIva = tiposIva.length === 1 && tiposIva[0] > 0 ? `IVA (${tiposIva[0]} %)` : 'IVA';
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

      <div className="factura-opciones" role="group" aria-label="Datos que se incluyen en la factura">
        <span className="text-muted">Incluir en la factura:</span>
        <label><input type="checkbox" checked={verEntidad} onChange={(e) => setVerEntidad(e.target.checked)} /> Datos de la entidad</label>
        <label><input type="checkbox" checked={verContable} onChange={(e) => setVerContable(e.target.checked)} disabled={datosContables.length === 0} /> Datos contables</label>
      </div>

      <div id="factura-pdf" className="factura-papel">
        <div className="factura-cabecera">
          <div>
            <p className="factura-etiqueta">Factura</p>
            <h2>Nº {factura.numero}</h2>
          </div>
          <div className="factura-ong">
            <strong>{ong.nombre}</strong>
            {verEntidad && ong.nif && <span>NIF/CIF: {ong.nif}</span>}
            {verEntidad && ong.direccion && <span>{ong.direccion}</span>}
            {verEntidad && contacto && <span>{contacto}</span>}
            <span>Fecha: {formatoFecha(factura.fecha)}</span>
          </div>
        </div>

        {factura.usuario && (
          <p className="factura-destinatario">
            <span className="factura-etiqueta">{esIngreso ? 'Recibido de' : 'Emitida a'}</span> {factura.usuario.nombre}
          </p>
        )}

        <table className="factura-tabla">
          <thead>
            <tr>
              <th>Descripción</th>
              <th className="num">Cantidad</th>
              <th className="num">Base (€)</th>
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

        {verContable && datosContables.length > 0 && (
          <dl className="factura-contable">
            {datosContables.map(([etiqueta, valor]) => (
              <div key={etiqueta}><dt>{etiqueta}</dt><dd>{valor}</dd></div>
            ))}
          </dl>
        )}

        {conIva && (
          <dl className="factura-desglose">
            <div><dt>Base imponible</dt><dd>{formatoEuro(base)}</dd></div>
            <div><dt>{etiquetaIva}</dt><dd>{formatoEuro(cuota)}</dd></div>
          </dl>
        )}

        <div className="factura-total">
          <span>{conIva ? 'Total (IVA incluido)' : 'Total'}</span>
          <strong>{formatoEuro(factura.total)}</strong>
        </div>
      </div>
    </div>
  );
};

export default FacturaDetalle;
