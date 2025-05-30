import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/Facturas.css';

const FacturasList = () => {
  const { id } = useParams(); // ID de la ONG
  const navigate = useNavigate();
  const verDetalleFactura = (idFactura) => {
    navigate(`/facturas/${idFactura}`);
  };
  const [facturas, setFacturas] = useState([]);

  useEffect(() => {
    axios.get(`http://localhost:8080/api/facturas/ong/${id}`)
      .then(res => setFacturas(res.data))
      .catch(err => console.error('Error al cargar facturas:', err));
  }, [id]);

  return (
    <div className="facturas-container">
      <h2>Historial de Facturas</h2>

      {facturas.length === 0 ? (
        <p>No hay facturas registradas.</p>
      ) : (
        <table className="facturas-table">
          <thead>
            <tr>
              <th>Número</th>
              <th>Fecha</th>
              <th>Total</th>
              <th>Emitida por</th>
            </tr>
          </thead>
          <tbody>
            {facturas.map(factura => (
              <tr 
              key={factura.id} 
              onClick={() => verDetalleFactura(factura.id)} 
              style={{ cursor: 'pointer' }}
            >
              <td>{factura.numero}</td>
              <td>{factura.fecha}</td>
              <td>{parseFloat(factura.total).toFixed(2)} €</td>
              <td>{factura.usuario ? factura.usuario.nombre : 'Anónimo'}</td>
            </tr>
            ))}
          </tbody>
        </table>
      )}

      <button className="volver-btn" onClick={() => navigate(`/area-economica/${id}`)}>Volver</button>
    </div>
  );
};

export default FacturasList;
