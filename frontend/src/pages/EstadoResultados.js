import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import PageHeader from '../components/PageHeader';
import { Cargando } from '../components/Estados';
import { toast } from '../components/Toast';
import { formatoEuro, ultimosAnios } from '../utils/format';

// Lista de conceptos con una barra proporcional al total
const Desglose = ({ datos, total, clase }) => {
  const filas = Object.entries(datos);
  if (filas.length === 0) return <p className="text-muted desglose-vacio">Sin movimientos en este año.</p>;

  return (
    <ul className="desglose">
      {filas.map(([concepto, monto]) => (
        <li key={concepto}>
          <div className="desglose-fila">
            <span>{concepto}</span>
            <strong>{formatoEuro(monto)}</strong>
          </div>
          <div className="barra barra-fina"><span className={clase} style={{ width: `${total > 0 ? Math.min(100, (monto / total) * 100) : 0}%` }} /></div>
        </li>
      ))}
    </ul>
  );
};

const EstadoResultados = () => {
  const { id } = useParams();
  const [anio, setAnio] = useState(new Date().getFullYear());
  const [datos, setDatos] = useState(null);
  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    setDatos(null);
    axios
      .get(`http://localhost:8080/api/reportes/estado-resultados/${id}?anio=${anio}`)
      .then((res) => setDatos(res.data))
      .catch((err) => console.error('Error al cargar el estado de resultados:', err));
  }, [id, anio]);

  const handleGuardarReporte = async () => {
    setGuardando(true);
    try {
      await axios.post(`http://localhost:8080/api/reportes/estado-resultados/${id}/guardar?anio=${anio}`);
      toast('Informe guardado correctamente', 'success');
    } catch (error) {
      console.error('Error al guardar el reporte:', error);
      toast('Hubo un error al guardar el reporte', 'error');
    } finally {
      setGuardando(false);
    }
  };

  return (
    <div className="fade-in">
      <PageHeader
        eyebrow="Informes"
        titulo="Estado de resultados"
        subtitulo="Ingresos y gastos del ejercicio, agrupados por concepto."
        acciones={
          <>
            <div className="field field-inline">
              <label htmlFor="anio" className="sr-only">Año</label>
              <select id="anio" className="select-inline" value={anio} onChange={(e) => setAnio(e.target.value)}>
                {ultimosAnios().map((y) => (
                  <option key={y} value={y}>{y}</option>
                ))}
              </select>
            </div>
            <button type="button" className="btn btn-primary" onClick={handleGuardarReporte} disabled={!datos || guardando}>
              <Icon name="download" size={16} /> {guardando ? 'Guardando...' : 'Guardar como reporte'}
            </button>
          </>
        }
      />

      {!datos ? (
        <Cargando texto="Cargando informe..." />
      ) : (
        <>
          <div className={`resultado card ${datos.resultadoNeto >= 0 ? 'balance-positivo' : 'balance-negativo'}`}>
            <p className="kpi-label">Resultado neto de {anio}</p>
            <p className="resultado-valor">{datos.resultadoNeto >= 0 ? '+' : ''}{formatoEuro(datos.resultadoNeto)}</p>
            <p className="kpi-nota">{datos.resultadoNeto >= 0 ? 'Superávit: los ingresos superan a los gastos.' : 'Déficit: los gastos superan a los ingresos.'}</p>
          </div>

          <div className="informe-grid">
            <section className="card card-pad">
              <div className="informe-titulo">
                <h3><Icon name="trending-up" size={20} className="text-positive" /> Ingresos</h3>
                <strong className="text-positive">{formatoEuro(datos.totalIngresos)}</strong>
              </div>
              <Desglose datos={datos.ingresos} total={datos.totalIngresos} clase="barra-verde" />
            </section>

            <section className="card card-pad">
              <div className="informe-titulo">
                <h3><Icon name="trending-down" size={20} className="text-negative" /> Gastos</h3>
                <strong className="text-negative">{formatoEuro(datos.totalGastos)}</strong>
              </div>
              <Desglose datos={datos.gastos} total={datos.totalGastos} clase="barra-naranja" />
            </section>
          </div>
        </>
      )}
    </div>
  );
};

export default EstadoResultados;
