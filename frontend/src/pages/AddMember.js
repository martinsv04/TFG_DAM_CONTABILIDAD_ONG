import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import PageHeader from '../components/PageHeader';
import { toast } from '../components/Toast';

const AddMember = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [enviando, setEnviando] = useState(false);

  const [memberData, setMemberData] = useState({
    nombre: '',
    email: '',
    password: '',
    rol: 'VOLUNTARIO',
    telefono: '',
    nifCif: '',
  });

  const handleChange = (e) => {
    setMemberData({ ...memberData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setEnviando(true);

    try {
      await axios.post('http://localhost:8080/api/usuarios', {
        ...memberData,
        creadoEn: new Date().toISOString().slice(0, 10),
        ong: { id: parseInt(id) },
      });
      toast('Miembro añadido correctamente', 'success');
      navigate(`/ong/${id}`);
    } catch (error) {
      console.error('Error añadiendo miembro:', error);
      toast('Error al añadir el miembro', 'error');
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="form-pagina fade-in">
      <PageHeader eyebrow="Equipo" titulo="Añadir miembro" subtitulo="Crea una cuenta para una persona de tu organización." />

      <form className="card card-pad form" onSubmit={handleSubmit}>
        <div className="form-grid">
          <div className="field">
            <label htmlFor="nombre">Nombre</label>
            <input id="nombre" type="text" name="nombre" value={memberData.nombre} onChange={handleChange} required />
          </div>

          <div className="field">
            <label htmlFor="email">Email</label>
            <input id="email" type="email" name="email" value={memberData.email} onChange={handleChange} required />
          </div>

          <div className="field">
            <label htmlFor="password">Contraseña inicial</label>
            <input id="password" type="password" name="password" value={memberData.password} onChange={handleChange} required minLength={6} autoComplete="new-password" />
            <span className="hint">Mínimo 6 caracteres. La persona podrá cambiarla después.</span>
          </div>

          <div className="field">
            <label htmlFor="rol">Rol</label>
            <select id="rol" name="rol" value={memberData.rol} onChange={handleChange}>
              <option value="CONTABLE">Contable</option>
              <option value="VOLUNTARIO">Voluntario</option>
              <option value="DONANTE">Donante</option>
            </select>
          </div>

          <div className="field">
            <label htmlFor="telefono">Teléfono</label>
            <input id="telefono" type="text" name="telefono" value={memberData.telefono} onChange={handleChange} />
          </div>

          <div className="field">
            <label htmlFor="nifCif">NIF/CIF</label>
            <input id="nifCif" type="text" name="nifCif" value={memberData.nifCif} onChange={handleChange} />
          </div>
        </div>

        <div className="form-actions">
          <button type="submit" className="btn btn-primary btn-lg" disabled={enviando}>
            {enviando ? 'Añadiendo...' : 'Añadir miembro'}
          </button>
          <button type="button" className="btn btn-ghost btn-lg" onClick={() => navigate(`/ong/${id}`)}>Cancelar</button>
        </div>
      </form>
    </div>
  );
};

export default AddMember;
