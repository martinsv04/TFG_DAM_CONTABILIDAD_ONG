import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios'; 
import './ViewOng.css';

const ViewOng = () => {
  const { id } = useParams(); 
  const navigate = useNavigate();
  const [rol, setRol] = useState('');
  const [ong, setOng] = useState(null);
  const [miembros, setMiembros] = useState([]);

  useEffect(() => {
    const storedRol = localStorage.getItem('rol');
    setRol(storedRol);

    const fetchOng = async () => {
      try {
        const response = await axios.get(`http://localhost:8080/api/ongs/${id}`);
        setOng(response.data);
      } catch (error) {
        console.error('Error al cargar la ONG:', error);
      }
    };

    fetchOng();
  }, [id]);

  if (!ong) return <div>Cargando...</div>;

  return (  
      <div className="vista-ong-container">
        <h1>{ong.nombre}</h1>
        <p><strong>Descripción:</strong> {ong.descripcion}</p>
        <p><strong>Dirección:</strong> {ong.direccion}</p>
        <p><strong>Teléfono:</strong> {ong.telefono}</p>
        <p><strong>Email:</strong> {ong.email}</p>
        <p><strong>Fecha de Creación:</strong> {ong.fechaCreacion}</p>

        <h2>Miembros ({miembros.length})</h2>
        <ul className="miembros-lista">
          {miembros.map(miembro => (
            <li key={miembro.id}>
              {miembro.nombre} - {miembro.rol}
              {rol === 'ADMIN' && (
                <span style={{ marginLeft: '10px' }}>
                  <button className="small-button">Cambiar Rol</button>
                  <button className="small-button">Eliminar</button>
                </span>
              )}
            </li>
          ))}
        </ul>

        {rol === 'ADMIN' && (
          <div className="admin-actions">
            <button className="main-button" onClick={() => navigate(`/editar-ong/${ong.id}`)}>Editar ONG</button>
            <button className="main-button" onClick={() => navigate(`/add-member/${ong.id}`)}>Añadir Miembro</button>
          </div>
        )}
      </div>
    
  );
};

export default ViewOng;
