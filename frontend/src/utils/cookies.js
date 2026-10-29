// Preferencias de cookies / almacenamiento local. Se guardan en localStorage.
// Hoy solo se usa almacenamiento técnico necesario (sesión); la categoría "analitica" queda
// preparada para el día que se añada analítica: se consulta con hayConsentimiento('analitica').
const CLAVE = 'ongestion_cookies';
const EVENTO_CAMBIO = 'ongestion:cookies';
const EVENTO_ABRIR = 'ongestion:abrir-cookies';

export const leerConsentimiento = () => {
  try {
    const guardado = JSON.parse(localStorage.getItem(CLAVE));
    return guardado && typeof guardado === 'object' ? guardado : null;
  } catch (error) {
    return null;
  }
};

export const guardarConsentimiento = ({ analitica }) => {
  const valor = { analitica: Boolean(analitica), fecha: new Date().toISOString() };
  try {
    localStorage.setItem(CLAVE, JSON.stringify(valor));
  } catch (error) {
    // Sin almacenamiento disponible: la elección solo dura mientras la página siga abierta
  }
  window.dispatchEvent(new CustomEvent(EVENTO_CAMBIO, { detail: valor }));
};

export const hayConsentimiento = (categoria) => {
  if (categoria === 'necesarias') return true;
  const c = leerConsentimiento();
  return Boolean(c && c[categoria]);
};

export const abrirPreferenciasCookies = () => window.dispatchEvent(new Event(EVENTO_ABRIR));

export { EVENTO_ABRIR, EVENTO_CAMBIO };
