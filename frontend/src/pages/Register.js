import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import Logo from '../components/Logo';
import Icon from '../components/Icons';
import { toast } from '../components/Toast';
import '../styles/public.css';

const VACIO = { nombre: '', empresa: '', email: '', mensaje: '' };

const Register = () => {
  const [formData, setFormData] = useState(VACIO);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log('Formulario enviado:', formData);
    toast('¡Tu mensaje ha sido enviado!', 'success');
    setFormData(VACIO);
  };

  return (
    <div className="auth">
      <aside className="auth-aside">
        <Logo claro />
        <div>
          <h2>Hablemos de tu organización</h2>
          <p>Cuéntanos cómo trabaja tu ONG y te ayudaremos a empezar con ONGestión.</p>
        </div>
        <ul className="auth-puntos">
          <li><span className="punto-icono"><Icon name="check" size={15} /></span>Sin hojas de cálculo</li>
          <li><span className="punto-icono"><Icon name="check" size={15} /></span>Datos separados por organización</li>
          <li><span className="punto-icono"><Icon name="check" size={15} /></span>Pensado para equipos pequeños</li>
        </ul>
      </aside>

      <main className="auth-main">
        <div className="auth-form-wrap fade-in">
          <Link to="/" className="auth-volver"><Icon name="arrow-left" size={16} /> Volver al inicio</Link>
          <h1>Contáctanos</h1>
          <p className="auth-sub">Déjanos tus datos y un mensaje.</p>

          <form className="form" onSubmit={handleSubmit}>
            <div className="field">
              <label htmlFor="nombre">Nombre completo</label>
              <input id="nombre" type="text" name="nombre" value={formData.nombre} onChange={handleChange} required />
            </div>

            <div className="field">
              <label htmlFor="empresa">Empresa / Organización</label>
              <input id="empresa" type="text" name="empresa" value={formData.empresa} onChange={handleChange} required />
            </div>

            <div className="field">
              <label htmlFor="email">Correo electrónico</label>
              <input id="email" type="email" name="email" value={formData.email} onChange={handleChange} required />
            </div>

            <div className="field">
              <label htmlFor="mensaje">Mensaje</label>
              <textarea id="mensaje" name="mensaje" rows="4" value={formData.mensaje} onChange={handleChange} />
            </div>

            <button type="submit" className="btn btn-primary btn-lg btn-block">Enviar mensaje</button>
          </form>

          <p className="auth-pie">¿Ya tienes cuenta? <Link to="/iniciar-sesion">Iniciar sesión</Link></p>
        </div>
      </main>
    </div>
  );
};

export default Register;
