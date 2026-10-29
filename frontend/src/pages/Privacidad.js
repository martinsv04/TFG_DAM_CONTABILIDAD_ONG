import React from 'react';
import { Link } from 'react-router-dom';
import PaginaLegal, { Dato } from '../components/PaginaLegal';
import { TITULAR, SITIO } from '../config/sitio';
import { abrirPreferenciasCookies } from '../utils/cookies';

function Privacidad() {
  return (
    <PaginaLegal
      titulo="Política de privacidad"
      intro={`En ${SITIO.nombre} tratamos tus datos personales conforme al Reglamento (UE) 2016/679 (RGPD) y a la Ley Orgánica 3/2018 (LOPDGDD). Te explicamos de forma sencilla qué datos usamos, para qué y qué derechos tienes.`}
    >
      <h2>1. Quién es el responsable</h2>
      <p>
        Titular del servicio: <Dato valor={TITULAR.nombre} /> · NIF/CIF: <Dato valor={TITULAR.nif} /> · Dirección: <Dato valor={TITULAR.direccion} /> ·
        Correo de contacto: <Dato valor={TITULAR.email} />.
      </p>
      <p>
        Hay que distinguir dos situaciones. Los datos de tu propia cuenta, los mensajes de contacto y el registro de seguridad los trata el titular como
        responsable del tratamiento. Los datos que cada ONG introduce en la plataforma (miembros, donantes, ingresos, gastos, facturas, certificados)
        pertenecen a esa ONG, que es la responsable de ellos; el titular de {SITIO.nombre} actúa en ese caso como encargado del tratamiento y los trata solo
        siguiendo sus instrucciones, según el contrato de encargo correspondiente (art. 28 RGPD).
      </p>

      <h2>2. Qué datos tratamos</h2>
      <ul>
        <li><strong>Cuenta de usuario:</strong> nombre, correo electrónico, rol dentro de la ONG y contraseña (guardada cifrada, nunca en texto legible).</li>
        <li><strong>Datos de perfil, opcionales según el rol:</strong> teléfono, NIF/CIF, dirección y código postal. Los necesitan, por ejemplo, los donantes para recibir su certificado de donación y para que la ONG pueda presentar el Modelo 182.</li>
        <li><strong>Donaciones y movimientos económicos:</strong> importe, fecha, descripción y, si no son anónimas, la persona que las realiza.</li>
        <li><strong>Datos contables de la ONG:</strong> ingresos, gastos, facturas, proyectos, subvenciones e informes.</li>
        <li><strong>Registro de actividad:</strong> qué usuario creó, modificó o eliminó un movimiento, un proyecto o una factura, con su rol y la fecha y hora.</li>
        <li><strong>Formulario de contacto:</strong> nombre, organización, correo electrónico y el mensaje que nos escribas.</li>
      </ul>

      <h2>3. Para qué los usamos y con qué base legal</h2>
      <ul>
        <li><strong>Prestarte el servicio</strong> (crear tu cuenta, permitirte acceder, registrar donaciones y gestionar la contabilidad de tu ONG). Base: ejecución de la relación contractual (art. 6.1.b RGPD).</li>
        <li><strong>Cumplir obligaciones legales</strong>, como emitir certificados de donación y facilitar los datos del Modelo 182 (Ley 49/2002) y conservar la documentación contable. Base: obligación legal (art. 6.1.c).</li>
        <li><strong>Mantener la seguridad y trazabilidad</strong> de los datos mediante el registro de actividad. Base: interés legítimo (art. 6.1.f).</li>
        <li><strong>Responder a tus consultas</strong> enviadas por el formulario de contacto. Base: tu consentimiento al enviarlo (art. 6.1.a), que puedes retirar cuando quieras.</li>
      </ul>
      <p>No tomamos decisiones automatizadas sobre ti ni usamos tus datos para publicidad.</p>

      <h2>4. A quién se comunican</h2>
      <p>
        No vendemos ni cedemos tus datos. Pueden acceder a ellos los proveedores que nos dan soporte técnico (alojamiento y mantenimiento), obligados por contrato
        a protegerlos, y las administraciones públicas cuando la ley lo exija (por ejemplo, la Agencia Tributaria con el Modelo 182, que presenta la propia ONG).
        Los usuarios de una ONG solo ven los datos de su organización y según su rol.
      </p>
      <p>
        Esta web carga sus tipografías desde Google Fonts, por lo que tu navegador se conecta a servidores de Google, que pueden ver tu dirección IP.
        No se envía ningún otro dato a Google.
      </p>

      <h2>5. Cuánto tiempo los conservamos</h2>
      <ul>
        <li>Los datos de tu cuenta, mientras la mantengas activa o la ONG lo necesite.</li>
        <li>Los datos contables y fiscales, durante los plazos que fija la ley (con carácter general, entre 4 y 6 años según la normativa tributaria y mercantil).</li>
        <li>Los mensajes de contacto, el tiempo necesario para atender tu consulta.</li>
      </ul>
      <p>Pasados los plazos, los datos se suprimen o se bloquean mientras puedan derivarse responsabilidades.</p>

      <h2>6. Tus derechos</h2>
      <p>
        Puedes ejercer los derechos de acceso, rectificación, supresión, oposición, limitación del tratamiento y portabilidad escribiendo a <Dato valor={TITULAR.email} />,
        indicando el derecho que quieres ejercer. Si tus datos los trata una ONG como responsable, dirígete también a ella. Si crees que no hemos tratado bien tus datos,
        puedes reclamar ante la Agencia Española de Protección de Datos (<a href="https://www.aepd.es" target="_blank" rel="noopener noreferrer">www.aepd.es</a>).
      </p>

      <h2>7. Cómo protegemos tus datos</h2>
      <p>
        Las contraseñas se guardan cifradas, el acceso se hace con una sesión que caduca y cada organización solo puede ver sus propios datos.
        Las operaciones relevantes quedan anotadas en el registro de actividad. Ninguna medida es infalible, pero revisamos la seguridad de forma continua.
      </p>

      <h2 id="cookies">8. Cookies y almacenamiento local</h2>
      <p>
        {SITIO.nombre} no usa cookies de publicidad ni de seguimiento. Para funcionar guarda en tu navegador unos pocos datos técnicos, imprescindibles y exentos de consentimiento:
      </p>
      <div className="legal-tabla-wrap">
        <table className="legal-tabla">
          <thead><tr><th>Dato</th><th>Para qué sirve</th><th>Duración</th></tr></thead>
          <tbody>
            <tr><td><code>token</code>, <code>rol</code>, <code>userId</code></td><td>Mantener tu sesión iniciada y mostrarte las opciones de tu rol.</td><td>Hasta que cierres sesión o caduque (8 horas).</td></tr>
            <tr><td><code>ongestion_cookies</code></td><td>Recordar la elección que hagas en el aviso de cookies.</td><td>Hasta que lo borres.</td></tr>
          </tbody>
        </table>
      </div>
      <p>
        Puedes cambiar tu elección en cualquier momento:{' '}
        <button type="button" className="enlace-boton" onClick={abrirPreferenciasCookies}>configurar cookies</button>.
        También puedes borrar estos datos desde los ajustes de tu navegador; si lo haces, tendrás que volver a iniciar sesión.
      </p>

      <h2>9. Menores</h2>
      <p>El servicio no está dirigido a menores de 14 años y no recogemos datos suyos de forma consciente.</p>

      <h2>10. Cambios en esta política</h2>
      <p>
        Podemos actualizar esta política cuando cambie el servicio o la normativa. La fecha de la última revisión figura al principio de la página.
        Consulta también los <Link to="/terminos">términos y condiciones</Link>.
      </p>
    </PaginaLegal>
  );
}

export default Privacidad;
