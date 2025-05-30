import React, { useState } from 'react';
import '../styles/Register.css';

const Register = () => {
  const [formData, setFormData] = useState({
    nombre: '',
    empresa: '',
    email: '',
    mensaje: ''
  });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log('Formulario enviado:', formData);
    alert('¡Tu mensaje ha sido enviado!');
  };

  return (
    <div className="register-page">
      <div className="register-form-container">
        <h2>Contáctanos</h2>
        <form onSubmit={handleSubmit}>
          <label>Nombre completo:</label>
          <input 
            type="text" 
            name="nombre" 
            value={formData.nombre} 
            onChange={handleChange} 
            required
          />
          <label>Empresa / Organización:</label>
          <input 
            type="text" 
            name="empresa" 
            value={formData.empresa} 
            onChange={handleChange} 
            required
          />
          <label>Correo electrónico:</label>
          <input 
            type="email" 
            name="email" 
            value={formData.email} 
            onChange={handleChange} 
            required
          />
          <label>Mensaje:</label>
          <textarea 
            name="mensaje" 
            value={formData.mensaje} 
            onChange={handleChange}
            rows="4"
          />
          <button type="submit">Enviar</button>
        </form>
      </div>
    </div>
  );
};

export default Register;
