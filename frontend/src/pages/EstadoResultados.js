import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import './EstadoResultados.css';

const EstadoResultados = () => {
  const { id } = useParams();
  const [anio, setAnio] = useState(new Date().getFullYear());
  const [datos, setDatos] = useState(null);

  useEffect(() => {
    axios.get(`http://localhost:8080/api/reportes/estado-resultados/${id}?anio=${anio}`)
      .then(res => setDatos(res.data))
      .catch(err => console.error('Error al cargar el estado de resultados:', err));
  }, [id, anio]);

  const handleChangeAnio = (e) => {
    setAnio(e.target.value);
  };

  const [mensaje, setMensaje] = useState('');

const handleGuardarReporte = async () => {
  try {
    const contenido = {
      ingresos: datos.ingresos,
      gastos: datos.gastos,
      totalIngresos: datos.totalIngresos,
      totalGastos: datos.totalGastos,
      resultadoNeto: datos.resultadoNeto
    };

    await axios.post('http://localhost:8080/api/reportes', {
      id_ong: id,
      tipo: 'ESTADO_RESULTADOS',
      fechaGeneracion: new Date().toISOString(),
      contenido: JSON.stringify(contenido)
    });

    setMensaje('Informe guardado correctamente en la base de datos.');
  } catch (error) {
    console.error('Error al guardar el reporte:', error);
    setMensaje('Hubo un error al guardar el reporte.');
  }
};


  if (!datos) return <p className="loading">Cargando informe...</p>;

  return (
    <div className="estado-resultados-container">
      <h2>Estado de Resultados</h2>

      <div className="selector-anio">
        <label htmlFor="anio">Seleccionar año:</label>
        <select id="anio" value={anio} onChange={handleChangeAnio}>
          {Array.from({ length: 6 }, (_, i) => 2020 + i).map(y => (
            <option key={y} value={y}>{y}</option>
          ))}
        </select>
      </div>

      <div className="bloque">
        <h3>Ingresos</h3>
        <ul>
          {Object.entries(datos.ingresos).map(([tipo, monto]) => (
            <li key={tipo}>{tipo}: <strong>{monto.toFixed(2)} €</strong></li>
          ))}
        </ul>
        <p><strong>Total Ingresos:</strong> {datos.totalIngresos.toFixed(2)} €</p>
      </div>

      <div className="bloque">
        <h3>Gastos</h3>
        <ul>
          {Object.entries(datos.gastos).map(([categoria, monto]) => (
            <li key={categoria}>{categoria}: <strong>{monto.toFixed(2)} €</strong></li>
          ))}
        </ul>
        <p><strong>Total Gastos:</strong> {datos.totalGastos.toFixed(2)} €</p>
      </div>

      <div className="bloque resultado-final">
        <h3>Resultado Neto</h3>
        <p className={datos.resultadoNeto >= 0 ? 'positivo' : 'negativo'}>
          {datos.resultadoNeto >= 0 ? '+' : ''}{datos.resultadoNeto.toFixed(2)} €
        </p>
      </div>
      <div className="guardar-reporte">
  <button onClick={handleGuardarReporte}>Guardar como Reporte</button>
  {mensaje && <p className="mensaje-confirmacion">{mensaje}</p>}
</div>

    </div>
    
  );
};

export default EstadoResultados;
