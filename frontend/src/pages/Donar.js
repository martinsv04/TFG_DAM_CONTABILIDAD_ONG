import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import { toast } from '../components/Toast';

const IMPORTES = [10, 25, 50, 100];

const Donar = () => {
  const { id } = useParams(); // id de la ONG
  const navigate = useNavigate();
  const [monto, setMonto] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [anonimo, setAnonimo] = useState(false);
  const [enviando, setEnviando] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setEnviando(true);

    const payload = {
      descripcion,
      monto: parseFloat(monto),
      tipo: 'DONACIÓN',
      fecha: new Date().toISOString().split('T')[0], // YYYY-MM-DD
      id_ong: parseInt(id),
      id_usuario: anonimo ? null : parseInt(localStorage.getItem('userId')),
    };

    try {
      await axios.post('http://localhost:8080/api/ingresos', payload, {
        headers: { 'Content-Type': 'application/json' },
      });
      toast('¡Gracias por tu donación!', 'success');
      navigate(`/ong/${id}`);
    } catch (error) {
      console.error('Error al registrar la donación:', error);
      toast('No se pudo completar la donación', 'error');
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="form-pagina donar fade-in">
      <div className="donar-cabecera">
        <span className="donar-icono"><Icon name="heart" size={28} /></span>
        <h1>Haz una donación</h1>
        <p className="page-sub">Tu aportación ayuda a que esta organización siga adelante.</p>
      </div>

      <form className="card card-pad form" onSubmit={handleSubmit}>
        <div className="field">
          <span className="label">Elige un importe</span>
          <div className="importes">
            {IMPORTES.map((importe) => (
              <button
                key={importe}
                type="button"
                className={`importe${Number(monto) === importe ? ' activo' : ''}`}
                onClick={() => setMonto(String(importe))}
              >
                {importe} €
              </button>
            ))}
          </div>
        </div>

        <div className="field">
          <label htmlFor="monto">Otro importe (€)</label>
          <input id="monto" type="number" value={monto} onChange={(e) => setMonto(e.target.value)} required min="1" placeholder="0" />
        </div>

        <div className="field">
          <label htmlFor="comentario">Comentario (opcional)</label>
          <textarea id="comentario" value={descripcion} onChange={(e) => setDescripcion(e.target.value)} placeholder="Un mensaje para el equipo..." />
        </div>

        <label className="interruptor">
          <input type="checkbox" checked={anonimo} onChange={() => setAnonimo(!anonimo)} />
          <span className="interruptor-pista" aria-hidden="true" />
          <span>
            <strong>Donar de forma anónima</strong>
            <small>Tu nombre no aparecerá en la factura.</small>
          </span>
        </label>

        <button type="submit" className="btn btn-accent btn-lg btn-block" disabled={enviando}>
          <Icon name="heart" size={18} /> {enviando ? 'Enviando...' : monto ? `Donar ${monto} €` : 'Donar'}
        </button>
      </form>
    </div>
  );
};

export default Donar;
