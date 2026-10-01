import React from 'react';
import FormularioMovimiento from '../components/FormularioMovimiento';

const CATEGORIAS = [
  { valor: 'PERSONAL', texto: 'Personal' },
  { valor: 'ALQUILER', texto: 'Alquiler' },
  { valor: 'SUMINISTROS', texto: 'Suministros' },
  { valor: 'TRANSPORTE', texto: 'Transporte' },
  { valor: 'FORMACIÓN', texto: 'Formación' },
  { valor: 'COMUNICACIÓN', texto: 'Comunicación' },
  { valor: 'OTROS', texto: 'Otros' },
];

const AddExpense = () => (
  <FormularioMovimiento
    tipoMovimiento="gasto"
    titulo="Registrar gasto"
    subtitulo="Anota una salida de dinero; después podrás generar su factura."
    url="http://localhost:8080/api/gastos"
    campo="categoria"
    etiquetaCampo="Categoría"
    opciones={CATEGORIAS}
    textoBoton="Guardar gasto"
    mensajeOk="Gasto añadido correctamente"
    botonClase="btn-accent"
  />
);

export default AddExpense;
