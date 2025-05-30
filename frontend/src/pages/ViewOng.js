import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/ViewOng.css';

const ViewOng = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [rol, setRol] = useState('');
    const [ong, setOng] = useState(null);
    const [miembros, setMiembros] = useState([]);
    const [rolEditableId, setRolEditableId] = useState(null);
    const [nuevoRol, setNuevoRol] = useState('');

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

        const fetchMiembros = async () => {
            try {
                const response = await axios.get(`http://localhost:8080/api/ongs/${id}/miembros`);
                setMiembros(response.data);
            } catch (error) {
                console.error('Error al cargar los miembros:', error);
            }
        };

        fetchOng();
        fetchMiembros();
    }, [id]);

    const abrirSelector = (miembro) => {
        setRolEditableId(miembro.id);
        setNuevoRol(miembro.rol);
    };

    const cambiarRol = async (idUsuario) => {
        console.log('Enviando nuevo rol:', nuevoRol);
        try {
            await axios.patch(
                `http://localhost:8080/api/usuarios/${idUsuario}/rol`,
                { rol: nuevoRol },
                {
                    headers: { 'Content-Type': 'application/json' },
                }
            );

            // ⚠️ Aquí forzamos recarga desde el backend
            const response = await axios.get(`http://localhost:8080/api/ongs/${id}/miembros`);
            setMiembros(response.data);
            setRolEditableId(null);
        } catch (error) {
            console.error('Error al actualizar el rol:', error);
            alert('No se pudo actualizar el rol');
        }
    };

    if (!ong) return <div className="loading-text">Cargando...</div>;

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
                {miembros.map((miembro) => (
                    <li key={miembro.id}>
                        {miembro.nombre} - {miembro.rol}
                        {rol === 'ADMIN' && (
                            <span style={{ marginLeft: '10px' }}>
                                {rolEditableId === miembro.id ? (
                                    <>
                                        <select
                                            value={nuevoRol}
                                            onChange={(e) => setNuevoRol(e.target.value)}
                                            className="small-select"
                                        >
                                            <option value="ADMIN">ADMIN</option>
                                            <option value="DONANTE">DONANTE</option>
                                            <option value="VOLUNTARIO">VOLUNTARIO</option>
                                            <option value="CONTABLE">CONTABLE</option>
                                        </select>
                                        <button
                                            type="button"
                                            className="small-button"
                                            onClick={() => cambiarRol(miembro.id)}
                                        >
                                            Guardar
                                        </button>
                                        <button
                                            type="button"
                                            className="small-button"
                                            onClick={() => setRolEditableId(null)}
                                        >
                                            Cancelar
                                        </button>
                                    </>
                                ) : (
                                    <button
                                        type="button"
                                        className="small-button"
                                        onClick={() => abrirSelector(miembro)}
                                    >
                                        Cambiar Rol
                                    </button>
                                )}
                                <button className="small-button">Eliminar</button>
                            </span>
                        )}
                    </li>
                ))}
            </ul>

            {rol === 'ADMIN' && (
                <div className="admin-actions">
                    <button className="main-button" onClick={() => navigate(`/editar-ong/${ong.id}`)}>
                        Editar ONG
                    </button>
                    <button className="main-button" onClick={() => navigate(`/add-member/${ong.id}`)}>
                        Añadir Miembro
                    </button>
                </div>
            )}
            {rol === 'DONANTE' && (
                <div style={{ marginTop: '20px' }}>
                    <button
                        className="main-button"
                        onClick={() => navigate(`/donar/${ong.id}`)}
                    >
                        Donar a esta ONG
                    </button>
                </div>
            )}


        </div>

    );
};

export default ViewOng;
