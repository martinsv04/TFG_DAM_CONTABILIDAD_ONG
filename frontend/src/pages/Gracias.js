import React from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import Icon from '../components/Icons';
import { haySesionValida } from '../services/authService';
import '../styles/public.css';

// Página de agradecimiento tras donar (?motivo=donacion&ong=ID) o tras enviar el formulario de contacto (?motivo=contacto)
function Gracias() {
  const [params] = useSearchParams();
  const motivo = params.get('motivo');
  const idOng = /^\d+$/.test(params.get('ong') || '') ? params.get('ong') : null;

  let icono = 'check-circle';
  let titulo = '¡Gracias!';
  let texto = 'Hemos recibido tu solicitud.';
  let acciones;

  if (motivo === 'donacion') {
    icono = 'heart';
    titulo = '¡Gracias por tu donación!';
    texto = 'Tu aportación ya está registrada. Si no la has hecho de forma anónima, podrás consultar tus certificados de donación desde tu panel.';
    acciones = (
      <>
        {idOng && <Link to={`/ong/${idOng}`} className="btn btn-primary btn-lg">Volver a la organización</Link>}
        <Link to="/dashboard" className="btn btn-secondary btn-lg">Ir a mi panel</Link>
      </>
    );
  } else if (motivo === 'contacto') {
    titulo = '¡Mensaje enviado!';
    texto = 'Gracias por escribirnos. Hemos recibido tu mensaje y te responderemos al correo que nos has indicado lo antes posible.';
    acciones = (
      <>
        <Link to="/" className="btn btn-primary btn-lg">Volver al inicio</Link>
        {!haySesionValida() && <Link to="/iniciar-sesion" className="btn btn-secondary btn-lg">Iniciar sesión</Link>}
      </>
    );
  } else {
    acciones = <Link to="/" className="btn btn-primary btn-lg">Volver al inicio</Link>;
  }

  return (
    <main className="estado-pagina">
      <div className="estado-inner fade-in">
        <span className="estado-icono"><Icon name={icono} size={34} /></span>
        <h1>{titulo}</h1>
        <p className="estado-texto">{texto}</p>
        <div className="estado-acciones">{acciones}</div>
      </div>
    </main>
  );
}

export default Gracias;
