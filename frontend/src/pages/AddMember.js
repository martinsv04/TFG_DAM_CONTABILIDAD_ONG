import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import '../styles/AddMember.css'; // Te paso luego CSS bonito si quieres

const AddMember = () => {
  const { id } = useParams(); // id de la ONG
  const navigate = useNavigate();

  const [memberData, setMemberData] = useState({
    nombre: '',
    email: '',
    password: 'default123', // puedes cambiarlo
    rol: 'VOLUNTARIO',
    telefono: '',
    nif_cif: ''
  });

  const handleChange = (e) => {
    setMemberData({
      ...memberData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      await axios.post('http://localhost:8080/api/usuarios', {
        ...memberData,
        idOng: id, // 👈 importante asociarlo a la ONG actual
        creadoEn: new Date().toISOString().slice(0, 10) // yyyy-mm-dd
      });
      alert('Miembro añadido correctamente');
      navigate(`/ong/${id}`);
    } catch (error) {
      console.error('Error añadiendo miembro:', error);
      alert('Error al añadir el miembro');
    }
  };

  return (
    <div className="add-member-container">
      <h1>Añadir Miembro</h1>
      <form onSubmit={handleSubmit}>
        <label>Nombre:</label>
        <input type="text" name="nombre" value={memberData.nombre} onChange={handleChange} required />

        <label>Email:</label>
        <input type="email" name="email" value={memberData.email} onChange={handleChange} required />

        <label>Rol:</label>
        <select name="rol" value={memberData.rol} onChange={handleChange}>
          <option value="CONTABLE">CONTABLE</option>
          <option value="VOLUNTARIO">VOLUNTARIO</option>
          <option value="DONANTE">DONANTE</option>
        </select>

        <label>Teléfono:</label>
        <input type="text" name="telefono" value={memberData.telefono} onChange={handleChange} />

        <label>NIF/CIF:</label>
        <input type="text" name="nif_cif" value={memberData.nif_cif} onChange={handleChange} />

        <button type="submit">Añadir</button>
      </form>
    </div>
  );
};

export default AddMember;
