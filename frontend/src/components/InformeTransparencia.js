import React from 'react';
import Icon from './Icons';
import GraficoDonut from './GraficoDonut';
import ListaBarras from './ListaBarras';
import { EstadoVacio } from './Estados';
import { formatoEuro } from '../utils/format';

// Colores categóricos (validados para daltonismo); cada concepto conserva siempre el suyo
export const COLOR_ORIGEN = { PUBLICO: '#4f6bd8', PRIVADO: '#0d9488' };
export const COLOR_AREA = { MISION: '#0d9488', ADMINISTRACION: '#e8743b', CAPTACION: '#4f6bd8' };

const pct = (n) => String(n).replace('.', ',');

// Cifras del año: ingresos, de dónde vienen, a qué se destinan. La usan la pantalla interna y la página pública.
const InformeTransparencia = ({ datos }) => {
  const sinDatos = Number(datos.ingresos) === 0 && Number(datos.gastos) === 0;
  if (sinDatos) {
    return (
      <section className="card">
        <EstadoVacio icono="pie-chart" titulo={`Sin movimientos en ${datos.anio}`} texto="Cuando se registren ingresos y gastos de este año, aquí aparecerán los gráficos." />
      </section>
    );
  }

  const saldo = Number(datos.resultado);
  const mision = datos.gastosPorArea.find((a) => a.clave === 'MISION');
  const admin = datos.gastosPorArea.find((a) => a.clave === 'ADMINISTRACION');
  const publica = datos.financiacion.find((f) => f.clave === 'PUBLICO');
  const concentracion = Number(datos.mayorFinanciadorPorcentaje) >= 50 && datos.financiadores.length > 1;

  return (
    <>
      <div className="kpi-grid">
        <div className="card kpi">
          <span className="kpi-icono kpi-ingresos"><Icon name="trending-up" size={22} /></span>
          <p className="kpi-label">Ingresos {datos.anio}</p>
          <p className="kpi-valor">{formatoEuro(datos.ingresos)}</p>
          <p className="kpi-nota">{publica ? `${pct(publica.porcentaje)} % de origen público` : ''}</p>
        </div>
        <div className="card kpi">
          <span className="kpi-icono kpi-gastos"><Icon name="trending-down" size={22} /></span>
          <p className="kpi-label">Gastos {datos.anio}</p>
          <p className="kpi-valor">{formatoEuro(datos.gastos)}</p>
          <p className="kpi-nota">{mision ? `${pct(mision.porcentaje)} % se destina a la misión` : ''}</p>
        </div>
        <div className={`card kpi ${saldo >= 0 ? 'balance-positivo' : 'balance-negativo'}`}>
          <span className="kpi-icono kpi-saldo"><Icon name="wallet" size={22} /></span>
          <p className="kpi-label">Resultado del ejercicio</p>
          <p className="kpi-valor">{formatoEuro(saldo)}</p>
          <p className="kpi-nota">{saldo >= 0 ? 'Superávit' : 'Déficit'}</p>
        </div>
      </div>

      <div className="graficos graficos-iguales">
        <section className="card card-pad">
          <div className="grafico-cabecera"><h3>¿De dónde viene el dinero?</h3></div>
          <GraficoDonut titulo="Ingresos por origen de la financiación" partes={datos.financiacion} colores={COLOR_ORIGEN} centro="ingresos" />
        </section>
        <section className="card card-pad">
          <div className="grafico-cabecera"><h3>¿En qué se gasta?</h3></div>
          <GraficoDonut titulo="Gastos por destino" partes={datos.gastosPorArea} colores={COLOR_AREA} centro="gastos" />
          {admin && (
            <p className="text-muted tr-nota">
              Los gastos de administración suponen el <strong>{pct(admin.porcentaje)} %</strong> del total.
            </p>
          )}
        </section>
      </div>

      <section className="card card-pad tr-seccion">
        <div className="grafico-cabecera"><h3>Ingresos por financiador</h3></div>
        {concentracion && (
          <div className="alert alert-warn tr-alerta" role="status">
            <Icon name="alert-circle" size={18} />
            <span><strong>{datos.mayorFinanciador}</strong> aporta el {pct(datos.mayorFinanciadorPorcentaje)} % de los ingresos: conviene diversificar las fuentes.</span>
          </div>
        )}
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr><th>Financiador</th><th>Origen</th><th className="num">Importe</th><th className="num">% del total</th></tr>
            </thead>
            <tbody>
              {datos.financiadores.map((f) => (
                <tr key={f.clave}>
                  <td>{f.nombre}</td>
                  <td><span className={`badge ${f.clave.startsWith('PUBLICO') ? 'badge-info' : 'badge-success'}`}>{f.clave.startsWith('PUBLICO') ? 'Público' : 'Privado'}</span></td>
                  <td className="num">{formatoEuro(f.importe)}</td>
                  <td className="num">{pct(f.porcentaje)} %</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <div className="graficos graficos-iguales">
        <section className="card card-pad">
          <div className="grafico-cabecera"><h3>Ingresos por tipo</h3></div>
          <ListaBarras partes={datos.ingresosPorTipo} color="#0d9488" etiqueta="Ingresos por tipo" />
        </section>
        <section className="card card-pad">
          <div className="grafico-cabecera"><h3>Gastos por categoría</h3></div>
          <ListaBarras partes={datos.gastosPorCategoria} color="#e8743b" etiqueta="Gastos por categoría" />
        </section>
      </div>
    </>
  );
};

export default InformeTransparencia;
