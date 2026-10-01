import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import PageHeader from '../components/PageHeader';
import { Cargando, EstadoVacio } from '../components/Estados';
import { toast } from '../components/Toast';
import { iniciales, formatoFecha, ETIQUETA_ROL } from '../utils/format';

const ROLES = ['ADMIN', 'DONANTE', 'VOLUNTARIO', 'CONTABLE'];
const CLASE_ROL = { ADMIN: 'badge-primary', CONTABLE: 'badge-info', DONANTE: 'badge-accent', VOLUNTARIO: 'badge-success' };

const ViewOng = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [rol, setRol] = useState('');
  const miId = parseInt(localStorage.getItem('userId'), 10);
  const [ong, setOng] = useState(null);
  const [miembros, setMiembros] = useState([]);
  const [rolEditableId, setRolEditableId] = useState(null);
  const [nuevoRol, setNuevoRol] = useState('');
  const [error, setError] = useState(false);

  useEffect(() => {
    setRol(localStorage.getItem('rol'));

    const fetchOng = async () => {
      try {
        const response = await axios.get(`http://localhost:8080/api/ongs/${id}`);
        setOng(response.data);
      } catch (err) {
        console.error('Error al cargar la ONG:', err);
        setError(true);
      }
    };

    const fetchMiembros = async () => {
      try {
        const response = await axios.get(`http://localhost:8080/api/ongs/${id}/miembros`);
        setMiembros(response.data);
      } catch (err) {
        console.error('Error al cargar los miembros:', err);
      }
    };

    fetchOng();
    fetchMiembros();
  }, [id]);

  const abrirSelector = (miembro) => {
    setRolEditableId(miembro.id);
    setNuevoRol(miembro.rol);
  };

  const recargarMiembros = async () => {
    const response = await axios.get(`http://localhost:8080/api/ongs/${id}/miembros`);
    setMiembros(response.data);
  };

  const cambiarRol = async (idUsuario) => {
    try {
      await axios.patch(
        `http://localhost:8080/api/usuarios/${idUsuario}/rol`,
        { rol: nuevoRol },
        { headers: { 'Content-Type': 'application/json' } }
      );
      await recargarMiembros();
      setRolEditableId(null);
      toast('Rol actualizado', 'success');
    } catch (err) {
      console.error('Error al actualizar el rol:', err);
      toast('No se pudo actualizar el rol', 'error');
    }
  };

  const eliminarMiembro = async (miembro) => {
    if (!window.confirm(`¿Seguro que quieres eliminar a ${miembro.nombre}? Esta acción no se puede deshacer.`)) {
      return;
    }

    try {
      await axios.delete(`http://localhost:8080/api/usuarios/${miembro.id}`);
      await recargarMiembros();
      if (rolEditableId === miembro.id) {
        setRolEditableId(null);
      }
      toast('Miembro eliminado', 'success');
    } catch (err) {
      console.error('Error al eliminar el miembro:', err);
      toast('No se pudo eliminar al miembro', 'error');
    }
  };

  if (error) {
    return (
      <div className="card">
        <EstadoVacio icono="alert-circle" titulo="No se pudo cargar la ONG" texto="Puede que no tengas acceso a esta organización." />
      </div>
    );
  }
  if (!ong) return <Cargando texto="Cargando ONG..." />;

  const esAdmin = rol === 'ADMIN';

  return (
    <div className="fade-in">
      <PageHeader
        eyebrow="Organización"
        titulo={ong.nombre}
        acciones={
          <>
            {esAdmin && (
              <>
                <button type="button" className="btn btn-secondary" onClick={() => navigate(`/editar-ong/${ong.id}`)}>
                  <Icon name="edit" size={16} /> Editar ONG
                </button>
                <button type="button" className="btn btn-primary" onClick={() => navigate(`/add-member/${ong.id}`)}>
                  <Icon name="user-plus" size={16} /> Añadir miembro
                </button>
              </>
            )}
            {rol === 'DONANTE' && (
              <button type="button" className="btn btn-accent" onClick={() => navigate(`/donar/${ong.id}`)}>
                <Icon name="heart" size={16} /> Donar a esta ONG
              </button>
            )}
          </>
        }
      />

      <div className="ong-detalle">
        <section className="card card-pad ong-info">
          <h3>Sobre la organización</h3>
          {ong.descripcion && <p className="ong-info-desc">{ong.descripcion}</p>}

          <dl className="datos">
            <div><dt><Icon name="map-pin" size={16} /> Dirección</dt><dd>{ong.direccion || '-'}</dd></div>
            <div><dt><Icon name="phone" size={16} /> Teléfono</dt><dd>{ong.telefono || '-'}</dd></div>
            <div><dt><Icon name="mail" size={16} /> Email</dt><dd>{ong.email || '-'}</dd></div>
            <div><dt><Icon name="calendar" size={16} /> Creada el</dt><dd>{formatoFecha(ong.fechaCreacion)}</dd></div>
          </dl>
        </section>

        <section className="card">
          <div className="card-header">
            <h3><Icon name="users" size={20} /> Equipo <span className="badge">{miembros.length}</span></h3>
          </div>

          {miembros.length === 0 ? (
            <EstadoVacio icono="users" titulo="Aún no hay miembros" texto="Añade a las personas de tu equipo para que puedan colaborar." />
          ) : (
            <ul className="miembros">
              {miembros.map((miembro) => (
                <li key={miembro.id} className="miembro">
                  <span className="avatar avatar-round">{iniciales(miembro.nombre)}</span>

                  <div className="miembro-datos">
                    <p className="miembro-nombre">{miembro.nombre}</p>
                    <span className={`badge ${CLASE_ROL[miembro.rol] || ''}`}>{ETIQUETA_ROL[miembro.rol] || miembro.rol}</span>
                  </div>

                  {esAdmin && (
                    <div className="miembro-acciones">
                      {rolEditableId === miembro.id ? (
                        <>
                          <select
                            value={nuevoRol}
                            onChange={(e) => setNuevoRol(e.target.value)}
                            className="select-inline"
                            aria-label={`Nuevo rol de ${miembro.nombre}`}
                          >
                            {ROLES.map((r) => (
                              <option key={r} value={r}>{ETIQUETA_ROL[r]}</option>
                            ))}
                          </select>
                          <button type="button" className="btn btn-primary btn-sm" onClick={() => cambiarRol(miembro.id)}>
                            Guardar
                          </button>
                          <button type="button" className="btn btn-ghost btn-sm" onClick={() => setRolEditableId(null)}>
                            Cancelar
                          </button>
                        </>
                      ) : (
                        <button type="button" className="btn btn-secondary btn-sm" onClick={() => abrirSelector(miembro)}>
                          Cambiar rol
                        </button>
                      )}

                      {/* No se puede eliminar al administrador de la ONG ni a uno mismo */}
                      {miembro.id !== ong.admin?.id && miembro.id !== miId && (
                        <button type="button" className="btn btn-danger btn-sm" onClick={() => eliminarMiembro(miembro)}>
                          <Icon name="trash" size={15} /> Eliminar
                        </button>
                      )}
                    </div>
                  )}
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  );
};

export default ViewOng;
