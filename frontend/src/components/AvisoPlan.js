import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import Icon from './Icons';

// Franja de aviso para el administrador: fin de la prueba, pago fallido o límites del plan Básico
const AvisoPlan = () => {
  const [plan, setPlan] = useState(null);

  useEffect(() => {
    let activo = true;
    axios
      .get('http://localhost:8080/api/plan')
      .then((res) => activo && setPlan(res.data))
      .catch(() => {});
    return () => {
      activo = false;
    };
  }, []);

  if (!plan) return null;

  let texto = null;
  if (plan.pagoPendiente) {
    texto = 'No hemos podido cobrar tu suscripción. Actualiza tu tarjeta para no perder el plan Pro.';
  } else if (plan.origen === 'PRUEBA' && plan.diasPrueba <= 5) {
    texto = `Tu prueba del plan Pro termina en ${plan.diasPrueba} ${plan.diasPrueba === 1 ? 'día' : 'días'}.`;
  } else if (plan.plan === 'BASICO') {
    const alLimite = ['miembros', 'proyectos', 'subvenciones', 'movimientos', 'facturas'].some(
      (k) => plan.uso[k] >= plan.limites[k]
    );
    if (alLimite) texto = 'Has alcanzado un límite del plan Básico.';
    else if (plan.ongs.length > 1) texto = 'Con el plan Básico solo tu ONG activa admite cambios.';
  }
  if (!texto) return null;

  return (
    <div className="aviso-plan" role="status">
      <Icon name="alert-circle" size={18} />
      <span>{texto}</span>
      <Link to="/plan">Ver planes</Link>
    </div>
  );
};

export default AvisoPlan;
