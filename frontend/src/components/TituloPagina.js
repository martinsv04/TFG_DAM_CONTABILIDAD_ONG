import { useEffect } from 'react';
import { useLocation, matchPath } from 'react-router-dom';
import { SITIO } from '../config/sitio';

// Título, descripción y robots de cada ruta. 'publica' = se puede indexar; el resto lleva noindex.
const RUTAS = [
  { ruta: '/', titulo: null, publica: true },
  { ruta: '/iniciar-sesion', titulo: 'Iniciar sesión', publica: true, descripcion: 'Accede a tu cuenta de ONGestión para gestionar la contabilidad de tu ONG.' },
  { ruta: '/registrarse', titulo: 'Contacto', publica: true, descripcion: 'Escríbenos y te ayudamos a empezar con ONGestión.' },
  { ruta: '/privacidad', titulo: 'Política de privacidad', publica: true, descripcion: 'Cómo trata ONGestión tus datos personales y qué derechos tienes.' },
  { ruta: '/terminos', titulo: 'Términos y condiciones', publica: true, descripcion: 'Condiciones de uso de la plataforma ONGestión.' },
  { ruta: '/gracias', titulo: 'Gracias' },

  { ruta: '/dashboard', titulo: 'Panel' },
  { ruta: '/ong/:id', titulo: 'Mi ONG' },
  { ruta: '/nueva-ong', titulo: 'Nueva ONG' },
  { ruta: '/plan', titulo: 'Plan y facturación' },
  { ruta: '/editar-ong/:id', titulo: 'Editar ONG' },
  { ruta: '/add-member/:id', titulo: 'Añadir miembro' },
  { ruta: '/area-economica/:id', titulo: 'Área económica' },
  { ruta: '/ongs/:id/ingresos/nuevo', titulo: 'Registrar ingreso' },
  { ruta: '/ongs/:id/gastos/nuevo', titulo: 'Registrar gasto' },
  { ruta: '/ongs/:id/ingresos/:movimientoId/editar', titulo: 'Editar ingreso' },
  { ruta: '/ongs/:id/gastos/:movimientoId/editar', titulo: 'Editar gasto' },
  { ruta: '/informe/resultados/:id', titulo: 'Estado de resultados' },
  { ruta: '/informe/balance/:id', titulo: 'Balance general' },
  { ruta: '/ongs/:id/facturas', titulo: 'Facturas' },
  { ruta: '/ongs/:id/facturas/nueva', titulo: 'Nueva factura' },
  { ruta: '/facturas/:id', titulo: 'Factura' },
  { ruta: '/ongs/:id/certificados', titulo: 'Certificados de donación' },
  { ruta: '/ongs/:id/certificados/donacion/:ingresoId', titulo: 'Certificado de donación' },
  { ruta: '/ongs/:id/certificados/anual/:usuarioId', titulo: 'Certificado anual' },
  { ruta: '/ongs/:id/mis-certificados', titulo: 'Mis certificados' },
  { ruta: '/ongs/:id/proyectos', titulo: 'Proyectos' },
  { ruta: '/ongs/:id/proyectos/:proyectoId', titulo: 'Proyecto' },
  { ruta: '/ongs/:id/subvenciones', titulo: 'Subvenciones' },
  { ruta: '/ongs/:id/auditoria', titulo: 'Registro de actividad' },
  { ruta: '/ongs/:id/exportar', titulo: 'Exportar datos' },
  { ruta: '/ongs/:id/cuentas', titulo: 'Resumen por cuenta' },
  { ruta: '/ongs/:id/modelo-182', titulo: 'Modelo 182' },
  { ruta: '/ongs/:id/transparencia', titulo: 'Transparencia' },
  { ruta: '/ongs/:id/gobierno', titulo: 'Gobierno y organigrama' },
  { ruta: '/transparencia/:id', titulo: 'Transparencia', publica: true, descripcion: 'Cuentas, origen de la financiación y órgano de gobierno de la entidad.' },
  { ruta: '/perfil', titulo: 'Mi perfil' },
  { ruta: '/donar/:id', titulo: 'Donar' },
];

const TITULO_INICIO = `${SITIO.nombre} · Contabilidad para ONGs`;

const fijarMeta = (selector, crear, valor) => {
  let el = document.head.querySelector(selector);
  if (!el) {
    el = crear();
    document.head.appendChild(el);
  }
  el.setAttribute(el.tagName === 'LINK' ? 'href' : 'content', valor);
};

// Actualiza document.title, la descripción, robots y la URL canónica según la ruta
function TituloPagina() {
  const { pathname } = useLocation();

  useEffect(() => {
    const coincide = RUTAS.find((r) => matchPath({ path: r.ruta, end: true }, pathname));
    const noEncontrada = !coincide;
    const publica = Boolean(coincide && coincide.publica);

    let titulo = TITULO_INICIO;
    if (noEncontrada) titulo = `Página no encontrada · ${SITIO.nombre}`;
    else if (coincide.titulo) titulo = `${coincide.titulo} · ${SITIO.nombre}`;
    document.title = titulo;

    fijarMeta('meta[name="description"]', () => {
      const m = document.createElement('meta');
      m.setAttribute('name', 'description');
      return m;
    }, (coincide && coincide.descripcion) || SITIO.descripcion);

    // Las zonas con sesión y las páginas sin contenido propio no deben aparecer en buscadores
    fijarMeta('meta[name="robots"]', () => {
      const m = document.createElement('meta');
      m.setAttribute('name', 'robots');
      return m;
    }, publica ? 'index,follow' : 'noindex,nofollow');

    // URL canónica solo en las páginas públicas y fuera de localhost
    const canonica = document.head.querySelector('link[rel="canonical"]');
    if (publica && window.location.hostname !== 'localhost') {
      fijarMeta('link[rel="canonical"]', () => {
        const l = document.createElement('link');
        l.setAttribute('rel', 'canonical');
        return l;
      }, `${SITIO.url}${pathname === '/' ? '/' : pathname}`);
    } else if (canonica) {
      canonica.remove();
    }
  }, [pathname]);

  return null;
}

export default TituloPagina;
