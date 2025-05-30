import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import '../styles/BalanceGeneral.css';

const BalanceGeneral = () => {
  const { id } = useParams();
  const [anio, setAnio] = useState(new Date().getFullYear());
  const [modo, setModo] = useState('anual'); // "mensual", "trimestral" o "anual"
  const [periodo, setPeriodo] = useState('1');
  const [datos, setDatos] = useState(null);

  useEffect(() => {
    let url = `http://localhost:8080/api/reportes/balance-general/${id}?anio=${anio}&modo=${modo}&periodo=${periodo}`;
    axios.get(url)
      .then(res => setDatos(res.data))
      .catch(err => console.error('Error al cargar balance general:', err));
  }, [id, anio, modo, periodo]);

  const handleGuardarReporte = () => {
    const url = `http://localhost:8080/api/reportes/balance-general/${id}/guardar?anio=${anio}&modo=${modo}&periodo=${periodo}`;

    axios.post(url)
        .then(() => {
          alert('Reporte guardado correctamente.');
        })
        .catch((err) => {
          console.error('Error al guardar el reporte:', err);
          alert('Error al guardar el reporte.');
        });
  };


  const renderPeriodoSelector = () => {
    if (modo === 'mensual') {
      return (
        <select value={periodo} onChange={(e) => setPeriodo(e.target.value)}>
          {[...Array(12)].map((_, i) => (
            <option key={i + 1} value={i + 1}>Mes {i + 1}</option>
          ))}
        </select>
      );
    } else if (modo === 'trimestral') {
      return (
        <select value={periodo} onChange={(e) => setPeriodo(e.target.value)}>
          {[1, 2, 3, 4].map(q => (
            <option key={q} value={q}>Trimestre {q}</option>
          ))}
        </select>
      );
    }
    return null;
  };

  if (!datos) return <p className="loading">Cargando balance...</p>;

  return (
    <div className="balance-general-container">
      <h2>Balance General</h2>

      <div className="filtros-balance">
        <label>Año:</label>
        <select value={anio} onChange={(e) => setAnio(e.target.value)}>
          {Array.from({ length: 6 }, (_, i) => 2020 + i).map(y => (
            <option key={y} value={y}>{y}</option>
          ))}
        </select>

        <label>Modo:</label>
        <select value={modo} onChange={(e) => setModo(e.target.value)}>
          <option value="anual">Anual</option>
          <option value="trimestral">Trimestral</option>
          <option value="mensual">Mensual</option>
        </select>

        {renderPeriodoSelector()}
      </div>

      <div className="bloque">
        <h3>Activos</h3>
        <p>{datos.activos.toFixed(2)} €</p>
      </div>
      <div className="bloque">
        <h3>Pasivos</h3>
        <p>{datos.pasivos.toFixed(2)} €</p>
      </div>
      <div className="bloque resultado-final">
        <h3>Fondos Netos</h3>
        <p className={datos.fondosNetos >= 0 ? 'positivo' : 'negativo'}>
          {datos.fondosNetos >= 0 ? '+' : ''}{datos.fondosNetos.toFixed(2)} €
        </p>
      </div>
      <div className="acciones-balance">
      <button onClick={handleGuardarReporte}>Guardar como Reporte</button>
      </div>

    </div>
  );
};

export default BalanceGeneral;