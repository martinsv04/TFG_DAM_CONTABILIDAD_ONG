import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import PageHeader from '../components/PageHeader';
import { Cargando } from '../components/Estados';
import { toast } from '../components/Toast';
import SubirImagen from '../components/SubirImagen';

const EditarOng = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [cargado, setCargado] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [imagenes, setImagenes] = useState({ logo: '', firma: '' });
  const [guardandoImg, setGuardandoImg] = useState(false);
  const [ong, setOng] = useState({
    nombre: '',
    descripcion: '',
    direccion: '',
    telefono: '',
    email: '',
    nif: '',
    representante: '',
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
    axios
      .get(`http://localhost:8080/api/ongs/${id}/imagenes`)
      .then((res) => setImagenes({ logo: res.data.logo || '', firma: res.data.firma || '' }))
      .catch(() => {});
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

  const guardarImagenes = async () => {
    setGuardandoImg(true);
    try {
      await axios.put(`http://localhost:8080/api/ongs/${id}/imagenes`, imagenes);
      toast('Imágenes guardadas', 'success');
    } catch (error) {
      toast(error.response?.data?.message || 'No se pudieron guardar las imágenes', 'error');
    } finally {
      setGuardandoImg(false);
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

        <h3 className="form-seccion">Datos fiscales</h3>
        <p className="text-muted form-seccion-nota">Aparecen en los certificados de donación.</p>
        <div className="form-grid">
          <div className="field">
            <label htmlFor="nif">NIF de la entidad</label>
            <input id="nif" type="text" name="nif" value={ong.nif || ''} onChange={handleChange} placeholder="G12345678" />
          </div>

          <div className="field">
            <label htmlFor="representante">Representante legal</label>
            <input id="representante" type="text" name="representante" value={ong.representante || ''} onChange={handleChange} placeholder="Quien firma los certificados" />
          </div>
        </div>

        <div className="form-actions">
          <button type="submit" className="btn btn-primary btn-lg" disabled={enviando}>
            {enviando ? 'Guardando...' : 'Guardar cambios'}
          </button>
          <button type="button" className="btn btn-ghost btn-lg" onClick={() => navigate(`/ong/${id}`)}>Cancelar</button>
        </div>
      </form>

      <section className="card card-pad form" style={{ marginTop: '1.5rem' }}>
        <h3 className="form-seccion">Imagen de los certificados</h3>
        <p className="text-muted form-seccion-nota">El logo aparece en la cabecera y la firma sobre la línea del representante. Se guardan aparte del resto de datos.</p>
        <div className="form-grid">
          <SubirImagen id="logo" etiqueta="Logo de la entidad" ayuda="PNG, JPG o WEBP. Se reduce automáticamente." valor={imagenes.logo} onChange={(v) => setImagenes({ ...imagenes, logo: v })} maxAncho={400} maxAlto={200} />
          <SubirImagen id="firma" etiqueta="Firma y sello" ayuda="Mejor un PNG con fondo transparente." valor={imagenes.firma} onChange={(v) => setImagenes({ ...imagenes, firma: v })} maxAncho={500} maxAlto={200} />
        </div>
        <div className="form-actions">
          <button type="button" className="btn btn-primary" onClick={guardarImagenes} disabled={guardandoImg}>
            {guardandoImg ? 'Guardando...' : 'Guardar imágenes'}
          </button>
        </div>
      </section>
    </div>
  );
};

export default EditarOng;
