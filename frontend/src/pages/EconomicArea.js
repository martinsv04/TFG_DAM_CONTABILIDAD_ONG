import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/EconomicArea.css';

const EconomicArea = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [ong, setOng] = useState(null);
  const [ingresos, setIngresos] = useState([]);
  const [gastos, setGastos] = useState([]);

  useEffect(() => {

    axios.get(`http://localhost:8080/api/ongs/${id}`)
      .then(res => setOng(res.data))
      .catch(err => console.error('Error cargando ONG:', err));


    axios.get(`http://localhost:8080/api/ingresos/ong/${id}`)
      .then(res => setIngresos(res.data))
      .catch(err => console.error('Error cargando ingresos', err));


    axios.get(`http://localhost:8080/api/gastos/ong/${id}`)
      .then(res => setGastos(res.data))
      .catch(err => console.error('Error cargando gastos', err));
  }, [id]);

  const totalIngresos = ingresos.reduce((sum, i) => sum + parseFloat(i.monto || 0), 0);
  const totalGastos = gastos.reduce((sum, g) => sum + parseFloat(g.monto || 0), 0);
  const balance = totalIngresos - totalGastos;

  return (
    <div className="economic-area-container">
      {ong && <h2 className="ong-nombre-titulo">{ong.nombre}</h2>}

      <h1>Área Económica</h1>

      <div className="resumen-financiero">
        <h2>Resumen Financiero</h2>
        <p><strong>Total Ingresos:</strong> {totalIngresos.toFixed(2)} €</p>
        <p><strong>Total Gastos:</strong> {totalGastos.toFixed(2)} €</p>
        <p>
          <strong>Balance Actual:</strong>
          <span style={{ color: balance >= 0 ? 'green' : 'red' }}>
            {balance >= 0 ? '+' : ''}{balance.toFixed(2)} €
          </span>
        </p>
      </div>

      <div className="acciones">
        <button onClick={() => navigate(`/ongs/${id}/ingresos/nuevo`)}>Añadir Ingreso</button>
        <button onClick={() => navigate(`/ongs/${id}/gastos/nuevo`)}>Añadir Gasto</button>
      </div>


      <div className="informes-buttons">
        <h3>Informes Financieros</h3>
        <button onClick={() => navigate(`/informe/balance/${id}`)}>Balance General</button>
        <button onClick={() => navigate(`/informe/resultados/${id}`)}>Estado de Resultados</button>
      </div>

      <div className="facturas-buttons">
        <h3>Facturas</h3>
        <button onClick={() => navigate(`/ongs/${id}/facturas`)}>Ver Historial de Facturas</button>
      </div>

      <div className="listados">
        <div className="bloque-ingresos">
          <h3>Ingresos</h3>
          <ul>
            {ingresos.map((ingreso) => (
              <li key={ingreso.id}>
                {ingreso.descripcion} - {parseFloat(ingreso.monto).toFixed(2)} € ({ingreso.tipo})
              </li>
            ))}
          </ul>
        </div>

        <div className="bloque-gastos">
          <h3>Gastos</h3>
          <ul>
            {gastos.map((gasto) => (
              <li key={gasto.id}>
                {gasto.descripcion} - {parseFloat(gasto.monto).toFixed(2)} € ({gasto.categoria})
              </li>
            ))}
          </ul>
        </div>
      </div>
      
      <div className="volver-dashboard">
        <button onClick={() => navigate('/dashboard')}>← Volver al Dashboard</button>
      </div>

    </div>
  );
};

export default EconomicArea;
