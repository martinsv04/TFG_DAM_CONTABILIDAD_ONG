// Cuentas contables más habituales en una ONG, según el plan de contabilidad de las
// entidades sin fines lucrativos (RD 1491/2011): grupo 7 para ingresos, grupo 6 para gastos
// y algunas del grupo 1 (subvenciones, donaciones y legados de capital).
// Es una selección práctica, no el cuadro completo; conviene que el gestor de la entidad la revise.
// Una cuenta propia (3 a 7 cifras) también se acepta al escribirla.

export const CUENTAS_INGRESO = [
  { codigo: '720', nombre: 'Cuotas de asociados y afiliados' },
  { codigo: '721', nombre: 'Cuotas de usuarios' },
  { codigo: '722', nombre: 'Promociones para captación de recursos' },
  { codigo: '723', nombre: 'Ingresos de patrocinadores y colaboraciones' },
  { codigo: '740', nombre: 'Subvenciones, donaciones y legados a la actividad' },
  { codigo: '747', nombre: 'Otras subvenciones, donaciones y legados transferidos al excedente' },
  { codigo: '705', nombre: 'Prestaciones de servicios' },
  { codigo: '700', nombre: 'Ventas de mercaderías' },
  { codigo: '701', nombre: 'Ventas de productos terminados' },
  { codigo: '752', nombre: 'Ingresos por arrendamientos' },
  { codigo: '759', nombre: 'Ingresos por servicios diversos' },
  { codigo: '769', nombre: 'Otros ingresos financieros' },
  { codigo: '778', nombre: 'Ingresos excepcionales' },
  { codigo: '130', nombre: 'Subvenciones oficiales de capital' },
  { codigo: '131', nombre: 'Donaciones y legados de capital' },
  { codigo: '132', nombre: 'Otras subvenciones, donaciones y legados' },
];

export const CUENTAS_GASTO = [
  { codigo: '650', nombre: 'Ayudas monetarias' },
  { codigo: '651', nombre: 'Ayudas no monetarias' },
  { codigo: '653', nombre: 'Compensación de gastos por prestaciones de colaboración' },
  { codigo: '654', nombre: 'Reembolsos de gastos al órgano de gobierno' },
  { codigo: '640', nombre: 'Sueldos y salarios' },
  { codigo: '642', nombre: 'Seguridad Social a cargo de la entidad' },
  { codigo: '649', nombre: 'Otros gastos sociales' },
  { codigo: '621', nombre: 'Arrendamientos y cánones' },
  { codigo: '622', nombre: 'Reparaciones y conservación' },
  { codigo: '623', nombre: 'Servicios de profesionales independientes' },
  { codigo: '624', nombre: 'Transportes' },
  { codigo: '625', nombre: 'Primas de seguros' },
  { codigo: '626', nombre: 'Servicios bancarios y similares' },
  { codigo: '627', nombre: 'Publicidad, propaganda y relaciones públicas' },
  { codigo: '628', nombre: 'Suministros' },
  { codigo: '629', nombre: 'Otros servicios' },
  { codigo: '600', nombre: 'Compras de bienes destinados a la actividad' },
  { codigo: '602', nombre: 'Compras de otros aprovisionamientos' },
  { codigo: '607', nombre: 'Trabajos realizados por otras entidades' },
  { codigo: '631', nombre: 'Otros tributos' },
  { codigo: '662', nombre: 'Intereses de deudas' },
  { codigo: '669', nombre: 'Otros gastos financieros' },
  { codigo: '678', nombre: 'Gastos excepcionales' },
  { codigo: '681', nombre: 'Amortización del inmovilizado material' },
];

const TODAS = [...CUENTAS_INGRESO, ...CUENTAS_GASTO];

// Nombre de una cuenta conocida (cadena vacía si es una cuenta propia)
export const nombreCuenta = (codigo) => TODAS.find((c) => c.codigo === codigo)?.nombre || '';

// "720 · Cuotas de asociados y afiliados" (o solo el código si no está en la lista)
export const textoCuenta = (codigo) => {
  if (!codigo) return '';
  const nombre = nombreCuenta(codigo);
  return nombre ? `${codigo} · ${nombre}` : codigo;
};

const sinTildes = (t) => t.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();

// Cuentas que coinciden con lo escrito (por código al principio o por palabras del nombre)
export const buscarCuentas = (lista, texto) => {
  const t = sinTildes((texto || '').trim());
  if (!t) return lista;
  const palabras = t.split(/\s+/);
  return lista.filter((c) => {
    const nombre = sinTildes(c.nombre);
    return palabras.every((p) => c.codigo.startsWith(p) || nombre.includes(p));
  });
};
