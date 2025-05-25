import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import './EditOng.css';
import axios from 'axios';

const EditarOng = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [ong, setOng] = useState({
    nombre: '',
    descripcion: '',
    direccion: '',
    telefono: '',
    email: '',
  });

  useEffect(() => {
    const fetchOng = async () => {
      try {
        const response = await axios.get(`http://localhost:8080/api/ongs/${id}`);
        setOng(response.data);
      } catch (error) {
        console.error('Error cargando ONG:', error);
      }
    };

    fetchOng();
  }, [id]);

  const handleChange = (e) => {
    setOng({
      ...ong,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.put(`http://localhost:8080/api/ongs/${id}`, ong);
      alert('ONG actualizada correctamente');
      navigate(`/ong/${id}`);
    } catch (error) {
      console.error('Error actualizando ONG:', error);
      alert('Error al actualizar la ONG');
    }
  };

  return (
    <div className="editar-ong-container">
      <h1>Editar ONG</h1>
      <form onSubmit={handleSubmit}>
        <label>Nombre:</label>
        <input type="text" name="nombre" value={ong.nombre} onChange={handleChange} required />

        <label>Descripción:</label>
        <textarea name="descripcion" value={ong.descripcion} onChange={handleChange} required />

        <label>Dirección:</label>
        <input type="text" name="direccion" value={ong.direccion} onChange={handleChange} />

        <label>Teléfono:</label>
        <input type="text" name="telefono" value={ong.telefono} onChange={handleChange} />

        <label>Email:</label>
        <input type="email" name="email" value={ong.email} onChange={handleChange} />

        <button type="submit">Guardar cambios</button>
      </form>
    </div>
  );
};

export default EditarOng;
