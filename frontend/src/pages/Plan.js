import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import PageHeader from '../components/PageHeader';
import { Cargando } from '../components/Estados';
import { toast } from '../components/Toast';
import { formatoEuro, formatoFecha } from '../utils/format';

const API = 'http://localhost:8080/api/plan';

const euros = (precio) => (precio ? formatoEuro(precio.importe / 100) : null);

const USO = [
  ['miembros', 'Miembros del equipo'],
  ['proyectos', 'Proyectos'],
  ['subvenciones', 'Subvenciones'],
  ['movimientos', 'Ingresos y gastos este año'],
  ['facturas', 'Facturas este año'],
];

const Plan = () => {
  const [params, setParams] = useSearchParams();
  const [plan, setPlan] = useState(null);
  const [error, setError] = useState('');
  const [trabajando, setTrabajando] = useState('');
  const temporizador = useRef(null);

  const cargar = useCallback(async () => {
    try {
      const res = await axios.get(API);
      setPlan(res.data);
      setError('');
      return res.data;
    } catch (err) {
      setError('No se pudo cargar tu plan.');
      return null;
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  // Al volver de Stripe, el aviso del pago puede tardar unos segundos en llegar: se consulta varias veces
  useEffect(() => {
    const resultado = params.get('pago');
    if (!resultado) return undefined;
    setParams({}, { replace: true });
    if (resultado === 'cancelado') {
      toast('Has cancelado el pago. No se te ha cobrado nada.', 'info');
      return undefined;
    }
    toast('Pago recibido. Estamos activando tu plan...', 'success');
    let intentos = 0;
    const comprobar = async () => {
      const datos = await cargar();
      intentos += 1;
      if (datos && datos.origen === 'SUSCRIPCION') {
        toast('Tu plan Pro está activo', 'success');
      } else if (intentos < 6) {
        temporizador.current = setTimeout(comprobar, 2500);
      }
    };
    temporizador.current = setTimeout(comprobar, 1500);
    return () => clearTimeout(temporizador.current);
  }, [params, setParams, cargar]);

  const ir = async (ruta, cuerpo, clave) => {
    setTrabajando(clave);
    try {
      const res = await axios.post(`${API}/${ruta}`, cuerpo);
      window.location.href = res.data.url;
    } catch (err) {
      toast(err.response?.data?.message || 'No se pudo conectar con el servicio de pagos', 'error');
      setTrabajando('');
    }
  };

  const elegirOng = async (ongId) => {
    try {
      await axios.put(`${API}/ong-activa`, { ongId });
      await cargar();
      toast('ONG activa actualizada', 'success');
    } catch (err) {
      toast(err.response?.data?.message || 'No se pudo cambiar la ONG activa', 'error');
    }
  };

  if (error) return <div className="alert alert-error">{error}</div>;
  if (!plan) return <Cargando texto="Cargando tu plan..." />;

  const esPro = plan.plan === 'PRO';
  const ahorro =
    plan.precioMensual && plan.precioAnual
      ? Math.round((1 - plan.precioAnual.importe / (plan.precioMensual.importe * 12)) * 100)
      : 0;

  return (
    <div className="plan-pagina fade-in">
      <PageHeader eyebrow="Cuenta" titulo="Plan y facturación" subtitulo="Tu plan cubre todas las organizaciones que administras." />

      <section className="card card-pad plan-estado">
        <div className="plan-estado-cabecera">
          <span className={`badge ${esPro ? 'badge-primary' : ''}`}>{esPro ? 'Plan Pro' : 'Plan Básico'}</span>
          {plan.origen === 'PRUEBA' && <strong>Prueba gratuita: te quedan {plan.diasPrueba} {plan.diasPrueba === 1 ? 'día' : 'días'}</strong>}
          {plan.origen === 'SUSCRIPCION' && <strong>Suscripción activa</strong>}
          {plan.origen === 'GRATIS' && <strong>Gratis, con límites</strong>}
        </div>

        {plan.origen === 'SUSCRIPCION' && plan.finPeriodo && (
          <p className="text-muted">
            {plan.cancelaAlFinal
              ? `Tu suscripción se cancelará el ${formatoFecha(plan.finPeriodo)}. Hasta entonces conservas el plan Pro.`
              : `Próxima renovación: ${formatoFecha(plan.finPeriodo)}.`}
          </p>
        )}
        {plan.pagoPendiente && (
          <div className="alert alert-warn" role="alert">
            <Icon name="alert-circle" size={18} />
            <span>No hemos podido cobrar tu última cuota. Actualiza tu tarjeta desde «Gestionar suscripción» para no perder el plan Pro.</span>
          </div>
        )}
        {plan.origen === 'PRUEBA' && (
          <p className="text-muted">Al terminar la prueba pasarás al plan Básico, salvo que te suscribas al Pro. No se borra ningún dato.</p>
        )}

        {plan.tieneSuscripcion && (
          <div className="form-actions">
            <button type="button" className="btn btn-secondary" disabled={trabajando === 'portal'} onClick={() => ir('portal', null, 'portal')}>
              <Icon name="credit-card" size={16} /> Gestionar suscripción
            </button>
          </div>
        )}
      </section>

      {!(plan.origen === 'SUSCRIPCION') && (
        <section aria-labelledby="titulo-pro">
          <h2 id="titulo-pro" className="plan-titulo">Pasa al plan Pro</h2>
          {!plan.stripeConfigurado && (
            <div className="alert alert-warn" role="status">
              <Icon name="alert-circle" size={18} />
              <span>Los pagos todavía no están configurados en este servidor.</span>
            </div>
          )}
          <div className="plan-precios">
            <article className="card card-pad plan-precio">
              <h3>Mensual</h3>
              <p className="plan-importe">{euros(plan.precioMensual) || '—'}<small> / mes</small></p>
              <button type="button" className="btn btn-primary btn-block" disabled={!plan.stripeConfigurado || Boolean(trabajando)} onClick={() => ir('checkout', { intervalo: 'MENSUAL' }, 'mensual')}>
                {trabajando === 'mensual' ? 'Redirigiendo...' : 'Suscribirme'}
              </button>
            </article>
            <article className="card card-pad plan-precio plan-precio-destacado">
              {ahorro > 0 && <span className="badge badge-accent plan-ahorro">Ahorras un {ahorro} %</span>}
              <h3>Anual</h3>
              <p className="plan-importe">{euros(plan.precioAnual) || '—'}<small> / año</small></p>
              <button type="button" className="btn btn-primary btn-block" disabled={!plan.stripeConfigurado || Boolean(trabajando)} onClick={() => ir('checkout', { intervalo: 'ANUAL' }, 'anual')}>
                {trabajando === 'anual' ? 'Redirigiendo...' : 'Suscribirme'}
              </button>
            </article>
          </div>
          <p className="text-muted plan-nota">El pago lo gestiona Stripe. Puedes cancelar cuando quieras desde «Gestionar suscripción».</p>
        </section>
      )}

      <section className="plan-comparar" aria-label="Comparación de planes">
        <article className="card card-pad">
          <h3>Básico <span className="text-muted">· gratis</span></h3>
          <ul className="plan-lista">
            <li>1 ONG activa</li>
            <li>Hasta {plan.limites.miembros} miembros por ONG</li>
            <li>Hasta {plan.limites.proyectos} proyectos y {plan.limites.subvenciones} subvención</li>
            <li>{plan.limites.movimientos} ingresos y gastos al año</li>
            <li>{plan.limites.facturas} facturas al año</li>
            <li>Exportar tus datos a CSV</li>
          </ul>
        </article>
        <article className="card card-pad plan-comparar-pro">
          <h3>Pro</h3>
          <ul className="plan-lista">
            <li>Todas las ONG que necesites</li>
            <li>Miembros, proyectos y subvenciones ilimitados</li>
            <li>Ingresos, gastos y facturas ilimitados</li>
            <li>Certificados de donación y Modelo 182</li>
            <li>Registro de actividad</li>
            <li>Exportar tus datos a CSV</li>
          </ul>
        </article>
      </section>

      {!esPro && (
        <section className="card card-pad">
          <h3>Uso de tu ONG activa</h3>
          <div className="plan-uso">
            {USO.map(([clave, etiqueta]) => {
              const usado = plan.uso[clave];
              const maximo = plan.limites[clave];
              const pct = Math.min(100, Math.round((usado / maximo) * 100));
              return (
                <div key={clave}>
                  <div className="plan-uso-linea"><span>{etiqueta}</span><strong>{usado} / {maximo}</strong></div>
                  <div className="barra barra-fina"><span className={usado >= maximo ? 'barra-naranja' : ''} style={{ width: `${pct}%` }} /></div>
                </div>
              );
            })}
          </div>

          {plan.ongs.length > 1 && (
            <fieldset className="plan-ongs">
              <legend>ONG activa</legend>
              <p className="text-muted">En el plan Básico solo una ONG admite cambios. Las demás quedan en solo lectura, sin perder nada.</p>
              {plan.ongs.map((o) => (
                <label key={o.id}>
                  <input type="radio" name="ong-activa" checked={o.activa} onChange={() => elegirOng(o.id)} /> {o.nombre}
                </label>
              ))}
            </fieldset>
          )}
        </section>
      )}
    </div>
  );
};

export default Plan;
