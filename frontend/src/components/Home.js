import React from 'react';
import { Link } from 'react-router-dom';
import Icon from './Icons';
import './Home.css';

const FUNCIONES = [
  {
    icono: 'wallet',
    titulo: 'Ingresos y gastos',
    texto: 'Registra cada movimiento con su categoría y fecha, y consulta en todo momento el balance de tu organización.',
  },
  {
    icono: 'file-text',
    titulo: 'Facturas en PDF',
    texto: 'Cada ingreso o gasto genera su factura, lista para consultar y descargar en PDF cuando la necesites.',
  },
  {
    icono: 'pie-chart',
    titulo: 'Informes financieros',
    texto: 'Balance general y estado de resultados por mes, trimestre o año, con la opción de guardarlos como reporte.',
  },
  {
    icono: 'users',
    titulo: 'Equipos y roles',
    texto: 'Administradores, contables, voluntarios y donantes: cada persona ve y hace solo lo que le corresponde.',
  },
  {
    icono: 'heart',
    titulo: 'Donaciones',
    texto: 'Los donantes registran su aportación en segundos, con su nombre o de forma anónima.',
  },
  {
    icono: 'shield',
    titulo: 'Datos protegidos',
    texto: 'Acceso con sesión cifrada y separación por ONG: nadie ve la información de otra organización.',
  },
];

const PASOS = [
  { titulo: 'Crea tu equipo', texto: 'Da de alta a las personas de tu ONG y asígnales un rol.' },
  { titulo: 'Registra tus movimientos', texto: 'Anota ingresos y gastos; las facturas se generan solas.' },
  { titulo: 'Genera tus informes', texto: 'Obtén el balance y el estado de resultados cuando los necesites.' },
];

function Home() {
  return (
    <main className="home">
      {/* ---------- Portada ---------- */}
      <section className="hero">
        <div className="hero-inner">
          <div className="hero-texto fade-in">
            <span className="badge badge-primary hero-badge">
              <Icon name="heart" size={14} /> Contabilidad pensada para ONGs
            </span>
            <h1 className="hero-titulo">Las cuentas de tu ONG, claras y siempre al día</h1>
            <p className="hero-sub">
              Registra ingresos y gastos, genera facturas e informes y gestiona tu equipo desde un solo lugar,
              sin hojas de cálculo ni complicaciones.
            </p>
            <div className="hero-cta">
              <Link to="/iniciar-sesion" className="btn btn-primary btn-lg">
                Iniciar sesión <Icon name="arrow-right" size={18} />
              </Link>
              <Link to="/registrarse" className="btn btn-secondary btn-lg">Contáctanos</Link>
            </div>
          </div>

          {/* Vista de ejemplo de la aplicación (ilustrativa) */}
          <div className="hero-visual fade-in" aria-hidden="true">
            <div className="mock card">
              <div className="mock-head">
                <span className="mock-dot" /><span className="mock-dot" /><span className="mock-dot" />
                <span className="mock-title">Resumen financiero</span>
              </div>
              <div className="mock-kpis">
                <div className="mock-kpi">
                  <span className="mock-kpi-icon mock-up"><Icon name="trending-up" size={16} /></span>
                  <span className="mock-label">Ingresos</span>
                  <span className="mock-value">12.480 €</span>
                </div>
                <div className="mock-kpi">
                  <span className="mock-kpi-icon mock-down"><Icon name="trending-down" size={16} /></span>
                  <span className="mock-label">Gastos</span>
                  <span className="mock-value">8.150 €</span>
                </div>
              </div>
              <div className="mock-chart">
                {[38, 54, 46, 70, 62, 84, 76].map((alto, i) => (
                  <span key={i} className="mock-bar" style={{ height: `${alto}%` }} />
                ))}
              </div>
              <div className="mock-balance">
                <span>Balance actual</span>
                <strong>+4.330 €</strong>
              </div>
            </div>
            <div className="mock-float card">
              <span className="avatar avatar-round"><Icon name="check" size={18} /></span>
              <div>
                <strong>Factura generada</strong>
                <p>Lista para descargar</p>
              </div>
            </div>
            <p className="mock-nota">Vista de ejemplo con datos ilustrativos</p>
          </div>
        </div>
      </section>

      {/* ---------- Funciones ---------- */}
      <section className="seccion" id="funciones">
        <div className="seccion-inner">
          <div className="seccion-cabecera">
            <span className="eyebrow">Funciones</span>
            <h2 className="seccion-titulo">Todo lo que necesitas para rendir cuentas</h2>
            <p className="seccion-sub">Herramientas sencillas para que el equipo se centre en lo importante: su misión.</p>
          </div>

          <div className="funciones-grid">
            {FUNCIONES.map((funcion) => (
              <article key={funcion.titulo} className="funcion card card-hover">
                <span className="funcion-icono"><Icon name={funcion.icono} size={22} /></span>
                <h3>{funcion.titulo}</h3>
                <p>{funcion.texto}</p>
              </article>
            ))}
          </div>
        </div>
      </section>

      {/* ---------- Cómo funciona ---------- */}
      <section className="seccion seccion-alt" id="como-funciona">
        <div className="seccion-inner">
          <div className="seccion-cabecera">
            <span className="eyebrow">Cómo funciona</span>
            <h2 className="seccion-titulo">Empieza en tres pasos</h2>
          </div>

          <ol className="pasos">
            {PASOS.map((paso, i) => (
              <li key={paso.titulo} className="paso">
                <span className="paso-numero">{i + 1}</span>
                <h3>{paso.titulo}</h3>
                <p>{paso.texto}</p>
              </li>
            ))}
          </ol>
        </div>
      </section>

      {/* ---------- Llamada final ---------- */}
      <section className="seccion">
        <div className="seccion-inner">
          <div className="cta-final">
            <h2>¿Listo para ordenar las cuentas de tu ONG?</h2>
            <p>Inicia sesión o escríbenos y te ayudamos a empezar.</p>
            <div className="hero-cta cta-botones">
              <Link to="/iniciar-sesion" className="btn btn-lg cta-claro">Iniciar sesión</Link>
              <Link to="/registrarse" className="btn btn-lg cta-borde">Contáctanos</Link>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}

export default Home;
