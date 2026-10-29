import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import PageHeader from '../components/PageHeader';
import { toast } from '../components/Toast';

const NuevaOng = () => {
  const navigate = useNavigate();
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState('');
  const [ong, setOng] = useState({
    nombre: '',
    descripcion: '',
    direccion: '',
    telefono: '',
    email: '',
    nif: '',
    representante: '',
  });

  const handleChange = (e) => setOng({ ...ong, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!ong.nombre.trim()) {
      setError('El nombre de la ONG es obligatorio.');
      return;
    }
    setEnviando(true);
    try {
      const res = await axios.post('http://localhost:8080/api/ongs', {
        ...ong,
        nombre: ong.nombre.trim(),
      });
      toast('ONG creada correctamente', 'success');
      navigate(`/ong/${res.data.id}`);
    } catch (err) {
      console.error('Error creando la ONG:', err);
      setError(err.response?.data?.message || 'No se pudo crear la ONG. Inténtalo de nuevo.');
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="form-pagina fade-in">
      <PageHeader
        eyebrow="Organización"
        titulo="Nueva ONG"
        subtitulo="Crea la organización. Después podrás añadir a su equipo, subir el logo y empezar a registrar ingresos y gastos."
      />

      <form className="card card-pad form" onSubmit={handleSubmit}>
        <div className="field">
          <label htmlFor="nombre">Nombre</label>
          <input id="nombre" type="text" name="nombre" value={ong.nombre} onChange={handleChange} required />
        </div>

        <div className="field">
          <label htmlFor="descripcion">Descripción</label>
          <textarea id="descripcion" name="descripcion" value={ong.descripcion} onChange={handleChange} placeholder="A qué se dedica la organización" />
        </div>

        <div className="form-grid">
          <div className="field span-2">
            <label htmlFor="direccion">Dirección</label>
            <input id="direccion" type="text" name="direccion" value={ong.direccion} onChange={handleChange} />
          </div>

          <div className="field">
            <label htmlFor="telefono">Teléfono</label>
            <input id="telefono" type="text" name="telefono" value={ong.telefono} onChange={handleChange} />
          </div>

          <div className="field">
            <label htmlFor="email">Email</label>
            <input id="email" type="email" name="email" value={ong.email} onChange={handleChange} />
          </div>
        </div>

        <h3 className="form-seccion">Datos fiscales</h3>
        <p className="text-muted form-seccion-nota">Aparecen en los certificados de donación y en el Modelo 182. Puedes completarlos más tarde en «Editar ONG».</p>
        <div className="form-grid">
          <div className="field">
            <label htmlFor="nif">NIF de la entidad</label>
            <input id="nif" type="text" name="nif" value={ong.nif} onChange={handleChange} placeholder="G12345678" />
          </div>

          <div className="field">
            <label htmlFor="representante">Representante legal</label>
            <input id="representante" type="text" name="representante" value={ong.representante} onChange={handleChange} placeholder="Quien firma los certificados" />
          </div>
        </div>

        {error && <div className="alert alert-error" role="alert">{error}</div>}

        <div className="form-actions">
          <button type="submit" className="btn btn-primary btn-lg" disabled={enviando}>
            {enviando ? 'Creando...' : 'Crear ONG'}
          </button>
          <button type="button" className="btn btn-ghost btn-lg" onClick={() => navigate('/dashboard')}>Cancelar</button>
        </div>
      </form>
    </div>
  );
};

export default NuevaOng;
