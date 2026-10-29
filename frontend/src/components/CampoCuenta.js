import React, { useEffect, useRef, useState } from 'react';
import { buscarCuentas, textoCuenta } from '../utils/cuentas';

// Cuadro de texto con sugerencias de cuentas contables. value = código (p. ej. "720") o ''.
const CampoCuenta = ({ id, value, onChange, lista }) => {
  const [texto, setTexto] = useState(textoCuenta(value));
  const [abierto, setAbierto] = useState(false);
  const [activo, setActivo] = useState(0);
  const caja = useRef(null);

  // Si el formulario se reinicia desde fuera, se refleja aquí
  useEffect(() => {
    if (!value) setTexto('');
  }, [value]);

  const sugerencias = buscarCuentas(lista, abierto ? texto : '').slice(0, 8);

  const elegir = (cuenta) => {
    onChange(cuenta.codigo);
    setTexto(textoCuenta(cuenta.codigo));
    setAbierto(false);
  };

  // Al salir: un código escrito a mano (3 a 7 cifras) se acepta; si no, se vuelve a la cuenta elegida
  const confirmar = () => {
    setAbierto(false);
    const t = texto.trim();
    if (!t) {
      onChange('');
      return;
    }
    const m = t.match(/^\d{3,7}/);
    if (m) {
      onChange(m[0]);
      setTexto(textoCuenta(m[0]));
    } else {
      setTexto(textoCuenta(value));
    }
  };

  const teclas = (e) => {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setAbierto(true);
      setActivo((a) => Math.min(a + 1, sugerencias.length - 1));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setActivo((a) => Math.max(a - 1, 0));
    } else if (e.key === 'Enter' && abierto && sugerencias[activo]) {
      e.preventDefault();
      elegir(sugerencias[activo]);
    } else if (e.key === 'Escape') {
      setAbierto(false);
    }
  };

  return (
    <div className="combo" ref={caja}>
      <input
        id={id}
        type="text"
        role="combobox"
        aria-expanded={abierto}
        aria-controls={`${id}-lista`}
        autoComplete="off"
        placeholder="Escribe un código o un nombre"
        value={texto}
        onChange={(e) => { setTexto(e.target.value); setAbierto(true); setActivo(0); }}
        onFocus={() => setAbierto(true)}
        onBlur={confirmar}
        onKeyDown={teclas}
      />
      {abierto && sugerencias.length > 0 && (
        <ul className="combo-lista" id={`${id}-lista`} role="listbox">
          {sugerencias.map((c, i) => (
            <li
              key={c.codigo}
              role="option"
              aria-selected={i === activo}
              className={i === activo ? 'activa' : ''}
              onMouseDown={(e) => { e.preventDefault(); elegir(c); }}
              onMouseEnter={() => setActivo(i)}
            >
              <strong>{c.codigo}</strong> {c.nombre}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default CampoCuenta;
