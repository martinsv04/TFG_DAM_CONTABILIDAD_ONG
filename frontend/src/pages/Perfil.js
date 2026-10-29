import React, { useEffect, useState } from 'react';
import axios from 'axios';
import PageHeader from '../components/PageHeader';
import { Cargando } from '../components/Estados';
import { toast } from '../components/Toast';
import { ETIQUETA_ROL } from '../utils/format';

// "Mi perfil": datos personales y fiscales del usuario (los fiscales se usan en los certificados de donación).
const Perfil = () => {
  const [perfil, setPerfil] = useState(null);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState('');
  const [pass, setPass] = useState({ actual: '', nueva: '', repetir: '' });
  const [errorPass, setErrorPass] = useState('');
  const [enviandoPass, setEnviandoPass] = useState(false);

  useEffect(() => {
    axios
      .get('http://localhost:8080/api/perfil')
      .then((res) => setPerfil(res.data))
      .catch(() => setError('No se pudo cargar tu perfil.'));
  }, []);

  const cambiar = (e) => setPerfil({ ...perfil, [e.target.name]: e.target.value });

  const guardar = async (e) => {
    e.preventDefault();
    setError('');
    setEnviando(true);
    try {
      const res = await axios.put('http://localhost:8080/api/perfil', {
        nombre: perfil.nombre,
        telefono: perfil.telefono,
        nifCif: perfil.nifCif,
        direccion: perfil.direccion,
        codigoPostal: perfil.codigoPostal,
      });
      setPerfil(res.data);
      toast('Perfil actualizado', 'success');
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudo guardar el perfil.');
    } finally {
      setEnviando(false);
    }
  };

  const cambiarPass = (e) => setPass({ ...pass, [e.target.name]: e.target.value });

  const guardarPass = async (e) => {
    e.preventDefault();
    setErrorPass('');
    if (pass.nueva !== pass.repetir) {
      setErrorPass('Las contraseñas nuevas no coinciden.');
      return;
    }
    setEnviandoPass(true);
    try {
      await axios.put('http://localhost:8080/api/perfil/password', { actual: pass.actual, nueva: pass.nueva });
      setPass({ actual: '', nueva: '', repetir: '' });
      toast('Contraseña actualizada', 'success');
    } catch (err) {
      setErrorPass(err.response?.data?.message || 'No se pudo cambiar la contraseña.');
    } finally {
      setEnviandoPass(false);
    }
  };

  if (!perfil) return error ? <div className="alert alert-error">{error}</div> : <Cargando texto="Cargando perfil..." />;

  return (
    <div className="form-pagina fade-in">
      <PageHeader eyebrow="Cuenta" titulo="Mi perfil" subtitulo="Tu NIF y tu dirección aparecen en los certificados de donación." />

      <form className="card card-pad form" onSubmit={guardar}>
        <div className="form-grid">
          <div className="field">
            <label htmlFor="nombre">Nombre completo</label>
            <input id="nombre" name="nombre" type="text" value={perfil.nombre || ''} onChange={cambiar} required />
          </div>
          <div className="field">
            <label htmlFor="email">Email</label>
            <input id="email" type="email" value={perfil.email || ''} disabled />
            <span className="hint">{ETIQUETA_ROL[perfil.rol] || perfil.rol}</span>
          </div>
          <div className="field">
            <label htmlFor="nifCif">NIF / NIE</label>
            <input id="nifCif" name="nifCif" type="text" value={perfil.nifCif || ''} onChange={cambiar} placeholder="12345678Z" />
          </div>
          <div className="field">
            <label htmlFor="telefono">Teléfono</label>
            <input id="telefono" name="telefono" type="text" value={perfil.telefono || ''} onChange={cambiar} />
          </div>
          <div className="field">
            <label htmlFor="codigoPostal">Código postal</label>
            <input id="codigoPostal" name="codigoPostal" type="text" inputMode="numeric" maxLength={5} value={perfil.codigoPostal || ''} onChange={cambiar} placeholder="41001" />
          </div>
          <div className="field span-2">
            <label htmlFor="direccion">Dirección postal</label>
            <input id="direccion" name="direccion" type="text" value={perfil.direccion || ''} onChange={cambiar} placeholder="Calle, número y ciudad" />
          </div>
        </div>

        {error && <div className="alert alert-error" role="alert">{error}</div>}

        <div className="form-actions">
          <button type="submit" className="btn btn-primary btn-lg" disabled={enviando}>
            {enviando ? 'Guardando...' : 'Guardar cambios'}
          </button>
        </div>
      </form>

      <form className="card card-pad form" onSubmit={guardarPass} style={{ marginTop: '1.5rem' }}>
        <h3>Cambiar contraseña</h3>
        <div className="form-grid">
          <div className="field span-2">
            <label htmlFor="pass-actual">Contraseña actual</label>
            <input id="pass-actual" name="actual" type="password" autoComplete="current-password" value={pass.actual} onChange={cambiarPass} required />
          </div>
          <div className="field">
            <label htmlFor="pass-nueva">Contraseña nueva</label>
            <input id="pass-nueva" name="nueva" type="password" autoComplete="new-password" value={pass.nueva} onChange={cambiarPass} required minLength={8} />
            <span className="hint">Mínimo 8 caracteres, con letras y números.</span>
          </div>
          <div className="field">
            <label htmlFor="pass-repetir">Repite la contraseña nueva</label>
            <input id="pass-repetir" name="repetir" type="password" autoComplete="new-password" value={pass.repetir} onChange={cambiarPass} required />
          </div>
        </div>

        {errorPass && <div className="alert alert-error" role="alert">{errorPass}</div>}

        <div className="form-actions">
          <button type="submit" className="btn btn-primary btn-lg" disabled={enviandoPass}>
            {enviandoPass ? 'Guardando...' : 'Cambiar contraseña'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default Perfil;
