import React from 'react';
import { formatoEuro, formatoFecha, formatoFechaLarga } from '../utils/format';

// Documento del certificado de donación (se descarga en PDF desde el elemento con id="certificado-pdf").
// Incluye los datos que exige el artículo 24 de la Ley 49/2002: identificación fiscal de donante y entidad,
// mención a la entidad beneficiaria del mecenazgo, fecha e importe, destino de la donación y carácter irrevocable.
const CertificadoPapel = ({ certificado }) => {
  const { entidad, donante, donaciones } = certificado;
  const anual = certificado.tipo === 'ANUAL';

  return (
    <div id="certificado-pdf" className="certificado-papel">
      <header className="certificado-cabecera">
        <div className="certificado-entidad">
          {entidad.logo && <img className="certificado-logo" src={entidad.logo} alt="Logo" />}
          <strong>{entidad.nombre}</strong>
          <span>NIF {entidad.nif}</span>
          <span>{entidad.direccion}</span>
        </div>
        <div className="certificado-numero">
          <span>Certificado nº</span>
          <strong>{certificado.numero}</strong>
        </div>
      </header>

      <h2 className="certificado-titulo">
        Certificado de donación{anual ? ` · Año ${certificado.anio}` : ''}
      </h2>

      <p>
        D./Dña. <strong>{entidad.representante}</strong>, como representante legal de <strong>{entidad.nombre}</strong>,
        con NIF <strong>{entidad.nif}</strong> y domicilio en {entidad.direccion},
      </p>

      <p className="certificado-certifica">CERTIFICA</p>

      <p>
        Que <strong>{donante.nombre}</strong>, con NIF <strong>{donante.nif}</strong> y domicilio en {donante.direccion},
        ha realizado a favor de esta entidad {anual ? 'durante el año ' + certificado.anio + ' las siguientes donaciones' : 'la siguiente donación'},
        por un importe total de <strong>{formatoEuro(certificado.importeTotal)}</strong>:
      </p>

      <table className="certificado-tabla">
        <thead>
          <tr>
            <th>Fecha</th>
            <th>Concepto</th>
            <th className="num">Importe</th>
          </tr>
        </thead>
        <tbody>
          {donaciones.map((d, i) => (
            <tr key={i}>
              <td>{formatoFecha(d.fecha)}</td>
              <td>{d.descripcion || 'Donación'}</td>
              <td className="num">{formatoEuro(d.importe)}</td>
            </tr>
          ))}
        </tbody>
        {anual && donaciones.length > 1 && (
          <tfoot>
            <tr>
              <td colSpan="2">Total {certificado.anio}</td>
              <td className="num">{formatoEuro(certificado.importeTotal)}</td>
            </tr>
          </tfoot>
        )}
      </table>

      <p className="certificado-legal">
        La entidad donataria se encuentra incluida entre las entidades beneficiarias del mecenazgo reguladas en la
        Ley 49/2002, de 23 de diciembre, de régimen fiscal de las entidades sin fines lucrativos y de los incentivos
        fiscales al mecenazgo. Las cantidades donadas se destinan al cumplimiento de los fines de interés general de
        la entidad. La donación tiene carácter irrevocable, sin perjuicio de lo dispuesto en las normas imperativas
        civiles, y se certifica a los efectos de las deducciones fiscales previstas en dicha Ley.
      </p>

      <p>Y para que conste, se expide el presente certificado el {formatoFechaLarga(certificado.fechaEmision)}.</p>

      <div className="certificado-firma">
        <div className="certificado-firma-linea">
          {entidad.firma && <img src={entidad.firma} alt="Firma" />}
        </div>
        <strong>{entidad.representante}</strong>
        <span>Representante legal · Firma y sello</span>
      </div>
    </div>
  );
};

export default CertificadoPapel;
