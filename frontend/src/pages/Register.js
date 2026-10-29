import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Logo from '../components/Logo';
import Icon from '../components/Icons';
import { toast } from '../components/Toast';
import '../styles/public.css';

const VACIO = { nombre: '', empresa: '', email: '', mensaje: '', web: '' }; // «web» es una trampa para bots: debe quedar vacío

const Register = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState(VACIO);
  const [acepta, setAcepta] = useState(false);
  const [enviando, setEnviando] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setEnviando(true);
    try {
      await axios.post('http://localhost:8080/api/contacto', formData);
      navigate('/gracias?motivo=contacto');
    } catch (error) {
      console.error('Error al enviar el mensaje:', error);
      toast(error.response?.data?.message || 'No se pudo enviar el mensaje. Inténtalo de nuevo en unos minutos.', 'error');
    } finally {
      setEnviando(false);
    }
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
              <textarea id="mensaje" name="mensaje" rows="4" maxLength="2000" value={formData.mensaje} onChange={handleChange} required />
            </div>

            {/* Campo oculto para personas: solo los bots lo rellenan */}
            <div className="campo-trampa" aria-hidden="true">
              <label htmlFor="web">No rellenar</label>
              <input id="web" type="text" name="web" tabIndex="-1" autoComplete="off" value={formData.web} onChange={handleChange} />
            </div>

            <label className="check-legal">
              <input type="checkbox" checked={acepta} onChange={(e) => setAcepta(e.target.checked)} required />
              <span>He leído y acepto la <Link to="/privacidad" target="_blank">política de privacidad</Link>.</span>
            </label>

            <button type="submit" className="btn btn-primary btn-lg btn-block" disabled={enviando || !acepta}>
              {enviando ? 'Enviando...' : 'Enviar mensaje'}
            </button>
          </form>

          <p className="auth-pie">¿Ya tienes cuenta? <Link to="/iniciar-sesion">Iniciar sesión</Link></p>
        </div>
      </main>
    </div>
  );
};

export default Register;
