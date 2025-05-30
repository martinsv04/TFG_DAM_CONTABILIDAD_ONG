import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/AddIncome.css';

const AddIncome = () => {
  const { id } = useParams(); // ID de la ONG
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    descripcion: '',
    monto: '',
    tipo: '',
    fecha: new Date().toISOString().split('T')[0] // Fecha actual
  });

  const [mensaje, setMensaje] = useState('');
  const [facturaId, setFacturaId] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const ingreso = {
      ...formData,
      monto: parseFloat(formData.monto),
      id_ong: id
    };

    try {
      const response = await axios.post('http://localhost:8080/api/ingresos', ingreso);
      const data = response.data;

      setMensaje(data.message || 'Ingreso añadido correctamente');
      setFacturaId(data.facturaId || null);
    } catch (error) {
      console.error('Error al guardar ingreso:', error);
      alert('Ocurrió un error al guardar el ingreso');
    }
  };

  return (
    <div className="add-income-container">
      <h2>Añadir Ingreso</h2>
      <form onSubmit={handleSubmit}>
        <label>Descripción:</label>
        <input type="text" name="descripcion" value={formData.descripcion} onChange={handleChange} required />

        <label>Monto (€):</label>
        <input type="number" name="monto" step="0.01" value={formData.monto} onChange={handleChange} required />

        <label>Tipo:</label>
        <select name="tipo" value={formData.tipo} onChange={handleChange} required>
          <option value="" disabled hidden>Seleccionar</option>
          <option value="donaciones">Donaciones</option>
          <option value="subvenciones">Subvenciones</option>
          <option value="eventos">Eventos</option>
          <option value="ventas">Ventas</option>
          <option value="cuotas">Cuotas</option>
          <option value="otros">Otros</option>
        </select>

        <label>Fecha:</label>
        <input type="date" name="fecha" value={formData.fecha} onChange={handleChange} required />

        <button type="submit">Guardar Ingreso</button>
      </form>

      {mensaje && (
        <div className="factura-exito">
          <p>{mensaje}</p>
          {facturaId && (
            <button onClick={() => navigate(`/facturas/${facturaId}`)}>
              Ver y Descargar Factura
            </button>
          )}
          <button onClick={() => navigate(`/area-economica/${id}`)} style={{ marginLeft: '10px' }}>
            Volver al Área Económica
          </button>
        </div>
      )}
    </div>
  );
};

export default AddIncome;
