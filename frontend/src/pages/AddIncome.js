import React from 'react';
import FormularioMovimiento from '../components/FormularioMovimiento';

// Mismos valores (en mayúsculas) que usa la pantalla de donaciones, para que todos los ingresos se guarden igual
const TIPOS = [
  { valor: 'DONACIÓN', texto: 'Donación' },
  { valor: 'SUBVENCIÓN', texto: 'Subvención' },
  { valor: 'EVENTOS', texto: 'Eventos' },
  { valor: 'VENTAS', texto: 'Ventas' },
  { valor: 'CUOTAS', texto: 'Cuotas' },
  { valor: 'OTROS', texto: 'Otros' },
];

const AddIncome = () => (
  <FormularioMovimiento
    tipoMovimiento="ingreso"
    titulo="Registrar ingreso"
    subtitulo="Anota una entrada de dinero; después podrás generar su factura."
    url="http://localhost:8080/api/ingresos"
    campo="tipo"
    etiquetaCampo="Tipo de ingreso"
    opciones={TIPOS}
    textoBoton="Guardar ingreso"
    mensajeOk="Ingreso añadido correctamente"
  />
);

export default AddIncome;
