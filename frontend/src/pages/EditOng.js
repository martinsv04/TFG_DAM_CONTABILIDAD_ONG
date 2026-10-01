import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import PageHeader from '../components/PageHeader';
import { Cargando } from '../components/Estados';
import { toast } from '../components/Toast';

const EditarOng = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [cargado, setCargado] = useState(false);
  const [enviando, setEnviando] = useState(false);
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
        toast('No se pudo cargar la ONG', 'error');
      } finally {
        setCargado(true);
      }
    };

    fetchOng();
  }, [id]);

  const handleChange = (e) => {
    setOng({ ...ong, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setEnviando(true);
    try {
      await axios.put(`http://localhost:8080/api/ongs/${id}`, ong);
      toast('ONG actualizada correctamente', 'success');
      navigate(`/ong/${id}`);
    } catch (error) {
      console.error('Error actualizando ONG:', error);
      toast('Error al actualizar la ONG', 'error');
    } finally {
      setEnviando(false);
    }
  };

  if (!cargado) return <Cargando texto="Cargando ONG..." />;

  return (
    <div className="form-pagina fade-in">
      <PageHeader eyebrow="Organización" titulo="Editar ONG" subtitulo="Actualiza los datos de contacto y la descripción." />

      <form className="card card-pad form" onSubmit={handleSubmit}>
        <div className="field">
          <label htmlFor="nombre">Nombre</label>
          <input id="nombre" type="text" name="nombre" value={ong.nombre || ''} onChange={handleChange} required />
        </div>

        <div className="field">
          <label htmlFor="descripcion">Descripción</label>
          <textarea id="descripcion" name="descripcion" value={ong.descripcion || ''} onChange={handleChange} required />
        </div>

        <div className="form-grid">
          <div className="field span-2">
            <label htmlFor="direccion">Dirección</label>
            <input id="direccion" type="text" name="direccion" value={ong.direccion || ''} onChange={handleChange} />
          </div>

          <div className="field">
            <label htmlFor="telefono">Teléfono</label>
            <input id="telefono" type="text" name="telefono" value={ong.telefono || ''} onChange={handleChange} />
          </div>

          <div className="field">
            <label htmlFor="email">Email</label>
            <input id="email" type="email" name="email" value={ong.email || ''} onChange={handleChange} />
          </div>
        </div>

        <div className="form-actions">
          <button type="submit" className="btn btn-primary btn-lg" disabled={enviando}>
            {enviando ? 'Guardando...' : 'Guardar cambios'}
          </button>
          <button type="button" className="btn btn-ghost btn-lg" onClick={() => navigate(`/ong/${id}`)}>Cancelar</button>
        </div>
      </form>
    </div>
  );
};

export default EditarOng;
