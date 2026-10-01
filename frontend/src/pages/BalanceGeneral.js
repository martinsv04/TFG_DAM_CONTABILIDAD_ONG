import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import PageHeader from '../components/PageHeader';
import { Cargando } from '../components/Estados';
import { toast } from '../components/Toast';
import { formatoEuro, ultimosAnios } from '../utils/format';

const BalanceGeneral = () => {
  const { id } = useParams();
  const [anio, setAnio] = useState(new Date().getFullYear());
  const [modo, setModo] = useState('anual'); // "mensual", "trimestral" o "anual"
  const [periodo, setPeriodo] = useState('1');
  const [datos, setDatos] = useState(null);
  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    setDatos(null);
    const url = `http://localhost:8080/api/reportes/balance-general/${id}?anio=${anio}&modo=${modo}&periodo=${periodo}`;
    axios
      .get(url)
      .then((res) => setDatos(res.data))
      .catch((err) => console.error('Error al cargar balance general:', err));
  }, [id, anio, modo, periodo]);

  const handleGuardarReporte = () => {
    setGuardando(true);
    const url = `http://localhost:8080/api/reportes/balance-general/${id}/guardar?anio=${anio}&modo=${modo}&periodo=${periodo}`;

    axios
      .post(url)
      .then(() => toast('Reporte guardado correctamente', 'success'))
      .catch((err) => {
        console.error('Error al guardar el reporte:', err);
        toast('Error al guardar el reporte', 'error');
      })
      .finally(() => setGuardando(false));
  };

  const cambiarModo = (nuevoModo) => {
    setModo(nuevoModo);
    setPeriodo('1');
  };

  const renderPeriodoSelector = () => {
    if (modo === 'mensual') {
      return (
        <select className="select-inline" aria-label="Mes" value={periodo} onChange={(e) => setPeriodo(e.target.value)}>
          {[...Array(12)].map((_, i) => (
            <option key={i + 1} value={i + 1}>Mes {i + 1}</option>
          ))}
        </select>
      );
    }
    if (modo === 'trimestral') {
      return (
        <select className="select-inline" aria-label="Trimestre" value={periodo} onChange={(e) => setPeriodo(e.target.value)}>
          {[1, 2, 3, 4].map((q) => (
            <option key={q} value={q}>Trimestre {q}</option>
          ))}
        </select>
      );
    }
    return null;
  };

  return (
    <div className="fade-in">
      <PageHeader
        eyebrow="Informes"
        titulo="Balance general"
        subtitulo="Situación económica de la organización en el periodo elegido."
        acciones={
          <button type="button" className="btn btn-primary" onClick={handleGuardarReporte} disabled={!datos || guardando}>
            <Icon name="download" size={16} /> {guardando ? 'Guardando...' : 'Guardar como reporte'}
          </button>
        }
      />

      <div className="filtros card">
        <div className="segmentado" role="group" aria-label="Periodo">
          {[['anual', 'Anual'], ['trimestral', 'Trimestral'], ['mensual', 'Mensual']].map(([valor, texto]) => (
            <button key={valor} type="button" className={modo === valor ? 'activo' : ''} onClick={() => cambiarModo(valor)}>
              {texto}
            </button>
          ))}
        </div>

        <select className="select-inline" aria-label="Año" value={anio} onChange={(e) => setAnio(e.target.value)}>
          {ultimosAnios().map((y) => (
            <option key={y} value={y}>{y}</option>
          ))}
        </select>

        {renderPeriodoSelector()}
      </div>

      {!datos ? (
        <Cargando texto="Cargando balance..." />
      ) : (
        <>
          <div className="kpi-grid kpi-grid-2">
            <div className="kpi card">
              <span className="kpi-icono kpi-ingresos"><Icon name="trending-up" size={22} /></span>
              <p className="kpi-label">Activos</p>
              <p className="kpi-valor">{formatoEuro(datos.activos)}</p>
              <p className="kpi-nota">Lo que posee la organización</p>
            </div>
            <div className="kpi card">
              <span className="kpi-icono kpi-gastos"><Icon name="trending-down" size={22} /></span>
              <p className="kpi-label">Pasivos</p>
              <p className="kpi-valor">{formatoEuro(datos.pasivos)}</p>
              <p className="kpi-nota">Lo que debe la organización</p>
            </div>
          </div>

          <div className={`resultado card ${datos.fondosNetos >= 0 ? 'balance-positivo' : 'balance-negativo'}`}>
            <p className="kpi-label">Fondos netos</p>
            <p className="resultado-valor">{datos.fondosNetos >= 0 ? '+' : ''}{formatoEuro(datos.fondosNetos)}</p>
            <p className="kpi-nota">Activos menos pasivos en el periodo seleccionado.</p>
          </div>
        </>
      )}
    </div>
  );
};

export default BalanceGeneral;
