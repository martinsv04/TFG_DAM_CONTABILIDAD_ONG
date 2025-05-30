import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom'; 
import '../styles/Login.css';
import axios from 'axios';

const Login = () => {
  const navigate = useNavigate();
  const [credentials, setCredentials] = useState({
    email: '',
    password: ''
  });

  const handleChange = (e) => {
    setCredentials({
      ...credentials,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:8080/api/auth/login', credentials);
      console.log('Respuesta recibida:', response.data);
      alert('¡Login exitoso!');
  
      const rol = response.data.rol;
      const userId = response.data.id; 
      localStorage.setItem('rol', rol); 
      localStorage.setItem('userId', userId);
      navigate('/dashboard'); 
    } catch (error) {
      console.error('Error en login:', error.response ? error.response.data : error.message);
      alert('Correo o contraseña incorrectos');
    }
  };
  

  return (
    <div className="login-page">
      <div className="login-form-container">
        <h2>Iniciar Sesión</h2>
        <form onSubmit={handleSubmit}>
          <label>Correo electrónico:</label>
          <input 
            type="email" 
            name="email" 
            value={credentials.email} 
            onChange={handleChange} 
            required
          />
          <label>Contraseña:</label>
          <input 
            type="password" 
            name="password" 
            value={credentials.password} 
            onChange={handleChange} 
            required
          />
          <button type="submit">Entrar</button>
        </form>
      </div>
    </div>
  );
};

export default Login;
