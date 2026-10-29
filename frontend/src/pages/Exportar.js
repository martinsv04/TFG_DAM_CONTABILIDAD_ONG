import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import { nombreCuenta } from '../utils/cuentas';
import axios from 'axios';
import Icon from '../components/Icons';
import PageHeader from '../components/PageHeader';
import { toast } from '../components/Toast';
import { descargarCsv, fechaCsv } from '../utils/exportar';
import { ESTADOS_PROYECTO, ESTADOS_SUBVENCION } from '../utils/proyectos';
import { ultimosAnios } from '../utils/format';

const API = 'http://localhost:8080/api';

const delAnio = (lista, anio) => (anio ? lista.filter((m) => String(m.fecha || '').startsWith(String(anio))) : lista);
const porFecha = (a, b) => String(a.fecha || '').localeCompare(String(b.fecha || ''));
const sufijo = (anio) => (anio ? `_${anio}` : '');
const num = (v) => (v === null || v === undefined ? null : Number(v));

// Cada exportación: cómo se obtienen los datos y qué columnas llevan. Se descarga un CSV listo para abrir en Excel.
const EXPORTACIONES = [
  {
    clave: 'movimientos',
    icono: 'wallet',
    titulo: 'Ingresos y gastos',
    texto: 'Todos los movimientos del periodo en una sola hoja, con su proyecto y subvención.',
    usaAnio: true,
    fichero: (anio) => `ingresos-gastos${sufijo(anio)}.csv`,
    cargar: async (id, anio) => {
      const [i, g] = await Promise.all([axios.get(`${API}/ingresos/ong/${id}`), axios.get(`${API}/gastos/ong/${id}`)]);
      const filas = [
        ...delAnio(i.data, anio).map((x) => ({ tipoMov: 'Ingreso', concepto: x.tipo, importe: num(x.monto), ...x })),
        ...delAnio(g.data, anio).map((x) => ({ tipoMov: 'Gasto', concepto: x.categoria, importe: -num(x.monto), ...x })),
      ];
      return filas.sort(porFecha);
    },
    columnas: [
      { titulo: 'Fecha', valor: (f) => fechaCsv(f.fecha) },
      { titulo: 'Tipo de movimiento', valor: (f) => f.tipoMov },
      { titulo: 'Descripción', valor: (f) => f.descripcion },
      { titulo: 'Categoría / tipo', valor: (f) => f.concepto },
      { titulo: 'Cuenta contable', valor: (f) => f.cuenta },
      { titulo: 'Nombre de la cuenta', valor: (f) => nombreCuenta(f.cuenta) },
      { titulo: 'Proyecto', valor: (f) => f.proyecto?.nombre },
      { titulo: 'Subvención', valor: (f) => f.subvencion?.nombre },
      { titulo: 'Persona', valor: (f) => f.usuario?.nombre },
      { titulo: 'Importe (€)', valor: (f) => f.importe },
    ],
  },
  {
    clave: 'facturas',
    icono: 'file-text',
    titulo: 'Facturas',
    texto: 'Listado de facturas emitidas con su origen y total.',
    usaAnio: true,
    fichero: (anio) => `facturas${sufijo(anio)}.csv`,
    cargar: async (id, anio) => delAnio((await axios.get(`${API}/facturas/ong/${id}`)).data, anio).sort(porFecha),
    columnas: [
      { titulo: 'Número', valor: (f) => f.numero },
      { titulo: 'Fecha', valor: (f) => fechaCsv(f.fecha) },
      { titulo: 'Origen', valor: (f) => (f.ingreso ? 'Ingreso' : f.gasto ? 'Gasto' : '') },
      { titulo: 'Concepto', valor: (f) => f.ingreso?.descripcion || f.gasto?.descripcion },
      { titulo: 'Cuenta contable', valor: (f) => (f.ingreso || f.gasto)?.cuenta },
      { titulo: 'Proyecto', valor: (f) => (f.ingreso || f.gasto)?.proyecto?.nombre },
      { titulo: 'Emitida a', valor: (f) => f.usuario?.nombre || 'Anónimo' },
      { titulo: 'Total (€)', valor: (f) => num(f.total) },
    ],
  },
  {
    clave: 'donantes',
    icono: 'heart',
    titulo: 'Donantes y donaciones',
    texto: 'Total donado por cada persona identificada en el año elegido.',
    usaAnio: true,
    soloAnio: true,
    fichero: (anio) => `donantes_${anio}.csv`,
    cargar: async (id, anio) => (await axios.get(`${API}/certificados/ong/${id}/donantes?anio=${anio}`)).data,
    columnas: [
      { titulo: 'Nombre', valor: (f) => f.nombre },
      { titulo: 'NIF', valor: (f) => f.nifCif },
      { titulo: 'Email', valor: (f) => f.email },
      { titulo: 'Dirección', valor: (f) => f.direccion },
      { titulo: 'Código postal', valor: (f) => f.codigoPostal },
      { titulo: 'Nº de donaciones', valor: (f) => f.numDonaciones },
      { titulo: 'Total donado (€)', valor: (f) => num(f.totalAnio) },
    ],
  },
  {
    clave: 'proyectos',
    icono: 'folder',
    titulo: 'Proyectos',
    texto: 'Estado, presupuesto y ejecución de cada proyecto.',
    fichero: () => 'proyectos.csv',
    cargar: async (id) => (await axios.get(`${API}/proyectos/ong/${id}`)).data,
    columnas: [
      { titulo: 'Proyecto', valor: (f) => f.nombre },
      { titulo: 'Estado', valor: (f) => ESTADOS_PROYECTO[f.estado]?.texto || f.estado },
      { titulo: 'Inicio', valor: (f) => fechaCsv(f.fechaInicio) },
      { titulo: 'Fin', valor: (f) => fechaCsv(f.fechaFin) },
      { titulo: 'Presupuesto (€)', valor: (f) => num(f.presupuesto) },
      { titulo: 'Gastado (€)', valor: (f) => num(f.gastado) },
      { titulo: 'Ingresado (€)', valor: (f) => num(f.ingresado) },
      { titulo: 'Resultado (€)', valor: (f) => num(f.resultado) },
      { titulo: 'Ejecución (%)', valor: (f) => num(f.porcentajeEjecutado) },
    ],
  },
  {
    clave: 'subvenciones',
    icono: 'landmark',
    titulo: 'Subvenciones',
    texto: 'Importes, cobros, ejecución y plazo de justificación.',
    fichero: () => 'subvenciones.csv',
    cargar: async (id) => (await axios.get(`${API}/subvenciones/ong/${id}`)).data,
    columnas: [
      { titulo: 'Subvención', valor: (f) => f.nombre },
      { titulo: 'Financiador', valor: (f) => f.financiador },
      { titulo: 'Expediente', valor: (f) => f.expediente },
      { titulo: 'Estado', valor: (f) => ESTADOS_SUBVENCION[f.estado]?.texto || f.estado },
      { titulo: 'Solicitado (€)', valor: (f) => num(f.importeSolicitado) },
      { titulo: 'Concedido (€)', valor: (f) => num(f.importeConcedido) },
      { titulo: 'Cobrado (€)', valor: (f) => num(f.cobrado) },
      { titulo: 'Proyecto', valor: (f) => f.proyectoNombre },
      { titulo: 'Gastado (€)', valor: (f) => num(f.gastado) },
      { titulo: 'Ejecución (%)', valor: (f) => num(f.porcentajeEjecutado) },
      { titulo: 'Fecha de solicitud', valor: (f) => fechaCsv(f.fechaSolicitud) },
      { titulo: 'Fecha de concesión', valor: (f) => fechaCsv(f.fechaConcesion) },
      { titulo: 'Plazo de justificación', valor: (f) => fechaCsv(f.fechaJustificacion) },
    ],
  },
];

// Exportación de datos a CSV (se abre directamente en Excel)
const Exportar = () => {
  const { id } = useParams(); // ID de la ONG
  const [anio, setAnio] = useState(new Date().getFullYear());
  const [descargando, setDescargando] = useState(null);

  const descargar = async (e) => {
    setDescargando(e.clave);
    try {
      const filtro = e.soloAnio ? anio || new Date().getFullYear() : e.usaAnio ? anio : '';
      const filas = await e.cargar(id, filtro);
      if (filas.length === 0) {
        toast('No hay datos que exportar con ese filtro', 'info');
        return;
      }
      descargarCsv(e.fichero(filtro), e.columnas, filas);
      toast(`${e.titulo}: ${filas.length} fila(s) exportadas`, 'success');
    } catch (err) {
      console.error('Error al exportar:', err);
      toast('No se pudo generar el archivo', 'error');
    } finally {
      setDescargando(null);
    }
  };

  return (
    <div className="fade-in">
      <PageHeader
        eyebrow="Área económica"
        titulo="Exportar datos"
        subtitulo="Descarga la información en CSV para abrirla en Excel o llevársela a tu gestor."
        acciones={
          <div className="field field-inline">
            <label htmlFor="anio">Año</label>
            <select id="anio" value={anio} onChange={(ev) => setAnio(Number(ev.target.value))}>
              <option value={0}>Todos los años</option>
              {ultimosAnios(8).map((a) => <option key={a} value={a}>{a}</option>)}
            </select>
          </div>
        }
      />

      <div className="exportar-grid">
        {EXPORTACIONES.map((e) => (
          <article key={e.clave} className="card exportar-card">
            <h3><Icon name={e.icono} size={20} /> {e.titulo}</h3>
            <p className="text-muted">
              {e.texto}
              {e.soloAnio && !anio && ' (se usará el año actual)'}
            </p>
            <button type="button" className="btn btn-secondary" onClick={() => descargar({ ...e })} disabled={descargando !== null}>
              <Icon name="download" size={16} /> {descargando === e.clave ? 'Generando...' : 'Descargar CSV'}
            </button>
          </article>
        ))}
      </div>

      <p className="certificado-aviso" style={{ textAlign: 'left' }}>
        Los archivos usan «;» como separador y coma decimal, el formato que Excel espera en español. Los gastos aparecen con importe negativo en la hoja de movimientos.
      </p>
    </div>
  );
};

export default Exportar;
