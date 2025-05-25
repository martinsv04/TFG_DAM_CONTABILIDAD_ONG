import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './AddExpense.css';

const AddExpense = () => {
  const { id } = useParams(); // ID de la ONG
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    descripcion: '',
    monto: '',
    categoria: '',
    fecha: new Date().toISOString().split('T')[0]
  });

  const [mensaje, setMensaje] = useState('');
  const [facturaId, setFacturaId] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const gasto = {
      ...formData,
      monto: parseFloat(formData.monto),
      id_ong: id
    };

    try {
      
      const response = await axios.post('http://localhost:8080/api/gastos', gasto);
      const data = response.data;

      setMensaje(data.message || 'Gasto añadido correctamente');
      setFacturaId(data.facturaId || null);
    } catch (error) {
      console.error('Error al guardar gasto:', error);
      alert('Ocurrió un error al guardar el gasto');
    }
  };

  return (
    <div className="add-expense-container">
      <h2>Añadir Gasto</h2>
      <form onSubmit={handleSubmit}>
        <label>Descripción:</label>
        <input type="text" name="descripcion" value={formData.descripcion} onChange={handleChange} required />

        <label>Monto (€):</label>
        <input type="number" name="monto" step="0.01" value={formData.monto} onChange={handleChange} required />

        <label>Categoría:</label>
        <select name="categoria" value={formData.categoria} onChange={handleChange} required>
          <option value="" disabled hidden>Seleccionar</option>
          <option value="PERSONAL">Personal</option>
          <option value="ALQUILER">Alquiler</option>
          <option value="SUMINISTROS">Suministros</option>
          <option value="TRANSPORTE">Transporte</option>
          <option value="FORMACIÓN">Formación</option>
          <option value="COMUNICACIÓN">Comunicación</option>
          <option value="OTROS">Otros</option>
        </select>

        <label>Fecha:</label>
        <input type="date" name="fecha" value={formData.fecha} onChange={handleChange} required />

        <button type="submit">Guardar Gasto</button>
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

export default AddExpense;
