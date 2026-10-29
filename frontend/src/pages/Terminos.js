import React from 'react';
import { Link } from 'react-router-dom';
import PaginaLegal, { Dato } from '../components/PaginaLegal';
import { TITULAR, SITIO } from '../config/sitio';

function Terminos() {
  return (
    <PaginaLegal
      titulo="Términos y condiciones"
      intro={`Estas condiciones regulan el uso de la plataforma ${SITIO.nombre}. Al crear una cuenta o usar el servicio las aceptas.`}
    >
      <h2>1. Quiénes somos</h2>
      <p>
        {SITIO.nombre} es un servicio de <Dato valor={TITULAR.nombre} /> (NIF/CIF <Dato valor={TITULAR.nif} />, domicilio en <Dato valor={TITULAR.direccion} />,
        correo <Dato valor={TITULAR.email} />), conforme a la Ley 34/2002 de servicios de la sociedad de la información (LSSI).
      </p>

      <h2>2. Qué ofrece el servicio</h2>
      <p>
        Una plataforma para que las ONG gestionen su contabilidad: registro de ingresos y gastos, facturas, proyectos y subvenciones, certificados de donación,
        datos para el Modelo 182, informes y exportación de datos, con distintos roles de acceso (administrador, contable, voluntario y donante).
      </p>

      <h2>3. Cuenta y acceso</h2>
      <ul>
        <li>Las cuentas las da de alta el administrador de cada ONG. Tú eres responsable de mantener en secreto tu contraseña y de lo que se haga con tu cuenta.</li>
        <li>Debes facilitar datos verdaderos y mantenerlos actualizados.</li>
        <li>Avísanos sin demora si sospechas que alguien ha accedido a tu cuenta sin permiso.</li>
      </ul>

      <h2>4. Uso aceptable</h2>
      <p>No está permitido intentar acceder a datos de otras organizaciones, alterar o sobrecargar el servicio, introducir contenido ilícito ni usar la plataforma para fines contrarios a la ley.</p>

      <h2>5. Responsabilidad de cada ONG sobre sus datos</h2>
      <p>
        Cada ONG es responsable de la veracidad de los datos que introduce y del cumplimiento de sus propias obligaciones contables, fiscales y de protección de datos.
        Los certificados de donación, los datos del Modelo 182 y los informes que genera {SITIO.nombre} son herramientas de ayuda: antes de presentarlos o entregarlos
        deben ser revisados por la propia entidad o por su gestor o asesor fiscal.
      </p>

      <h2>6. Donaciones</h2>
      <p>
        {SITIO.nombre} registra las donaciones que se indican en la plataforma, pero no procesa pagos ni actúa como intermediario financiero.
        La relación de la donación es entre el donante y la ONG destinataria.
      </p>

      <h2>7. Propiedad intelectual</h2>
      <p>
        El software, el diseño, los textos y la marca {SITIO.nombre} son del titular o de sus licenciantes. Los datos que introduce cada ONG siguen siendo suyos.
      </p>

      <h2>8. Disponibilidad y límites de responsabilidad</h2>
      <p>
        Trabajamos para que el servicio esté siempre disponible, pero se ofrece «tal cual» y puede haber interrupciones por mantenimiento o causas ajenas.
        En la medida que permita la ley, no respondemos de daños indirectos ni de decisiones tomadas solo con la información de la plataforma. Recomendamos
        conservar copias de los datos importantes; puedes descargarlos en CSV desde la sección de exportación.
      </p>

      <h2>9. Protección de datos</h2>
      <p>
        El tratamiento de datos personales se rige por la <Link to="/privacidad">política de privacidad</Link>, que forma parte de estas condiciones.
      </p>

      <h2>10. Cambios y terminación</h2>
      <p>
        Podemos modificar estas condiciones o el servicio; los cambios importantes se comunicarán en la propia plataforma. Puedes dejar de usar el servicio en cualquier momento.
        Podemos suspender cuentas que incumplan estas condiciones.
      </p>

      <h2>11. Ley aplicable</h2>
      <p>Estas condiciones se rigen por la ley española. Para cualquier controversia, y salvo que la normativa de consumidores disponga otra cosa, serán competentes los juzgados y tribunales del domicilio del titular.</p>
    </PaginaLegal>
  );
}

export default Terminos;
