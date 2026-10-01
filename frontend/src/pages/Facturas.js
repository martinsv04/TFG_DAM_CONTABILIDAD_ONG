import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import PageHeader from '../components/PageHeader';
import { Cargando, EstadoVacio } from '../components/Estados';
import Icon from '../components/Icons';
import { formatoEuro, formatoFecha } from '../utils/format';

const FacturasList = () => {
  const { id } = useParams(); // ID de la ONG
  const navigate = useNavigate();
  const [facturas, setFacturas] = useState([]);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    axios
      .get(`http://localhost:8080/api/facturas/ong/${id}`)
      .then((res) => setFacturas(res.data))
      .catch((err) => console.error('Error al cargar facturas:', err))
      .finally(() => setCargando(false));
  }, [id]);

  return (
    <div className="fade-in">
      <PageHeader
        eyebrow="Área económica"
        titulo="Historial de facturas"
        subtitulo="Pulsa una factura para ver su detalle y descargarla en PDF."
        acciones={
          <>
            <button type="button" className="btn btn-secondary" onClick={() => navigate(`/area-economica/${id}`)}>
              Volver al área económica
            </button>
            <button type="button" className="btn btn-primary" onClick={() => navigate(`/ongs/${id}/facturas/nueva`)}>
              <Icon name="plus" size={16} /> Nueva factura
            </button>
          </>
        }
      />

      <section className="card">
        {cargando ? (
          <Cargando texto="Cargando facturas..." />
        ) : facturas.length === 0 ? (
          <EstadoVacio
            icono="file-text"
            titulo="No hay facturas registradas"
            texto="Genera la factura de cualquier ingreso o gasto registrado con el botón «Nueva factura»."
          />
        ) : (
          <div className="table-wrap">
            <table className="table table-click">
              <thead>
                <tr>
                  <th>Número</th>
                  <th>Origen</th>
                  <th>Fecha</th>
                  <th>Emitida por</th>
                  <th className="num">Total</th>
                </tr>
              </thead>
              <tbody>
                {facturas.map((factura) => (
                  <tr key={factura.id} onClick={() => navigate(`/facturas/${factura.id}`)}>
                    <td><strong>{factura.numero}</strong></td>
                    <td>
                      {factura.ingreso ? (
                        <><span className="badge badge-success">Ingreso</span> <span className="text-muted">{factura.ingreso.descripcion}</span></>
                      ) : factura.gasto ? (
                        <><span className="badge badge-accent">Gasto</span> <span className="text-muted">{factura.gasto.descripcion}</span></>
                      ) : (
                        <span className="text-muted">—</span>
                      )}
                    </td>
                    <td className="text-muted">{formatoFecha(factura.fecha)}</td>
                    <td>{factura.usuario ? factura.usuario.nombre : <span className="badge">Anónimo</span>}</td>
                    <td className="num"><strong>{formatoEuro(factura.total)}</strong></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
};

export default FacturasList;
