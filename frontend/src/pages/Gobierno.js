import React, { useCallback, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import Icon from '../components/Icons';
import PageHeader from '../components/PageHeader';
import Organigrama from '../components/Organigrama';
import Confirmar from '../components/Confirmar';
import FormularioGobierno from '../components/FormularioGobierno';
import { Cargando, EstadoVacio } from '../components/Estados';
import { toast } from '../components/Toast';
import { getRol } from '../services/authService';

const API = 'http://localhost:8080/api';

// Órgano de gobierno (patronato, junta...) y equipo de dirección de la ONG
const Gobierno = () => {
  const { id } = useParams();
  const esAdmin = getRol() === 'ADMIN';
  const [personas, setPersonas] = useState(null);
  const [formulario, setFormulario] = useState(null); // { persona, organo }
  const [aEliminar, setAEliminar] = useState(null);
  const [eliminando, setEliminando] = useState(false);

  const cargar = useCallback(() => {
    axios
      .get(`${API}/gobierno/ong/${id}`)
      .then((res) => setPersonas(res.data))
      .catch(() => {
        toast('No se pudo cargar el organigrama', 'error');
        setPersonas([]);
      });
  }, [id]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const eliminar = async () => {
    setEliminando(true);
    try {
      await axios.delete(`${API}/gobierno/${aEliminar.id}`);
      toast('Persona eliminada', 'success');
      setAEliminar(null);
      cargar();
    } catch (e) {
      toast('No se pudo eliminar', 'error');
    } finally {
      setEliminando(false);
    }
  };

  const acciones = esAdmin
    ? (p) => (
      <>
        <button type="button" className="btn btn-ghost btn-sm" onClick={() => setFormulario({ persona: p })} aria-label={`Editar a ${p.nombre}`}>
          <Icon name="edit" size={15} />
        </button>
        <button type="button" className="btn btn-ghost btn-sm" onClick={() => setAEliminar(p)} aria-label={`Quitar a ${p.nombre}`}>
          <Icon name="trash" size={15} />
        </button>
      </>
    )
    : null;

  const nuevo = esAdmin && (
    <button type="button" className="btn btn-primary" onClick={() => setFormulario({ organo: 'GOBIERNO' })}>
      <Icon name="plus" size={16} /> Añadir persona
    </button>
  );

  return (
    <div className="fade-in">
      <PageHeader
        eyebrow="Gobierno"
        titulo="Gobierno y organigrama"
        subtitulo="Quién gobierna y dirige la entidad. Esta información aparece en la página pública de transparencia."
        acciones={nuevo}
      />

      {!personas ? (
        <Cargando texto="Cargando organigrama..." />
      ) : personas.length === 0 ? (
        <section className="card">
          <EstadoVacio
            icono="network"
            titulo="Todavía no hay personas registradas"
            texto="Añade a la presidencia, la secretaría, la tesorería y al equipo de dirección para completar el organigrama."
            accion={nuevo || null}
          />
        </section>
      ) : (
        <Organigrama personas={personas} acciones={acciones} />
      )}

      {aEliminar && (
        <Confirmar
          titulo="¿Quitar del organigrama?"
          texto={`${aEliminar.nombre} dejará de aparecer como ${aEliminar.cargo}, también en la página pública.`}
          textoBoton="Quitar"
          enviando={eliminando}
          alConfirmar={eliminar}
          alCerrar={() => setAEliminar(null)}
        />
      )}

      {formulario && (
        <FormularioGobierno
          idOng={id}
          persona={formulario.persona}
          organoInicial={formulario.organo}
          alCerrar={() => setFormulario(null)}
          alGuardar={() => {
            setFormulario(null);
            toast('Cambios guardados', 'success');
            cargar();
          }}
        />
      )}
    </div>
  );
};

export default Gobierno;
