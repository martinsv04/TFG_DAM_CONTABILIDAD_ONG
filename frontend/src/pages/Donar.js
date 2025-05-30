import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/Donar.css';


const Donar = () => {
    const { id } = useParams(); // id de la ONG
    const navigate = useNavigate();
    const [monto, setMonto] = useState('');
    const [descripcion, setDescripcion] = useState('');
    const [anonimo, setAnonimo] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();

        const payload = {
            descripcion,
            monto: parseFloat(monto),
            tipo: "DONACIÓN",
            fecha: new Date().toISOString().split('T')[0], // YYYY-MM-DD
            id_ong: parseInt(id),
            id_usuario: anonimo ? null : parseInt(localStorage.getItem('userId'))
        };

        try {
            await axios.post('http://localhost:8080/api/ingresos', payload, {
                headers: { 'Content-Type': 'application/json' }
            });
            alert('¡Gracias por tu donación!');
            navigate(`/ong/${id}`);
        } catch (error) {
            console.error('Error al registrar la donación:', error);
            alert('No se pudo completar la donación');
        }
    };

    return (
        <div className="form-donacion">
            <h2>Donar a la ONG</h2>
            <form onSubmit={handleSubmit}>
                <label>Monto (€):</label>
                <input
                    type="number"
                    value={monto}
                    onChange={(e) => setMonto(e.target.value)}
                    required
                    min="1"
                />

                <label>Comentario (opcional):</label>
                <textarea
                    value={descripcion}
                    onChange={(e) => setDescripcion(e.target.value)}
                />

                <label>
                    <input
                        type="checkbox"
                        checked={anonimo}
                        onChange={() => setAnonimo(!anonimo)}
                    />
                    Donar de forma anónima
                </label>

                <button type="submit">Donar</button>
            </form>
        </div>
    );
};

export default Donar;
