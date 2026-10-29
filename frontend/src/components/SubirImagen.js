import React, { useRef, useState } from 'react';

// Selector de imagen: la reduce en el navegador (canvas) y devuelve una "data URL" PNG/JPEG pequeña.
// Los PNG conservan la transparencia (útil para la firma).
const reducir = (archivo, maxAncho, maxAlto) =>
  new Promise((resolve, reject) => {
    const lector = new FileReader();
    lector.onerror = () => reject(new Error('No se pudo leer el archivo'));
    lector.onload = () => {
      const img = new Image();
      img.onerror = () => reject(new Error('El archivo no es una imagen válida'));
      img.onload = () => {
        const escala = Math.min(1, maxAncho / img.width, maxAlto / img.height);
        const canvas = document.createElement('canvas');
        canvas.width = Math.max(1, Math.round(img.width * escala));
        canvas.height = Math.max(1, Math.round(img.height * escala));
        canvas.getContext('2d').drawImage(img, 0, 0, canvas.width, canvas.height);
        resolve(canvas.toDataURL('image/png'));
      };
      img.src = lector.result;
    };
    lector.readAsDataURL(archivo);
  });

const SubirImagen = ({ id, etiqueta, ayuda, valor, onChange, maxAncho = 400, maxAlto = 200 }) => {
  const input = useRef(null);
  const [error, setError] = useState('');

  const elegir = async (e) => {
    const archivo = e.target.files && e.target.files[0];
    e.target.value = '';
    if (!archivo) return;
    setError('');
    if (!/^image\/(png|jpeg|webp)$/.test(archivo.type)) {
      setError('Usa una imagen PNG, JPG o WEBP.');
      return;
    }
    try {
      const url = await reducir(archivo, maxAncho, maxAlto);
      if (url.length > 390000) {
        setError('La imagen es demasiado grande, prueba con una más sencilla.');
        return;
      }
      onChange(url);
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="field subir-imagen">
      <label htmlFor={id}>{etiqueta}</label>
      <div className="subir-imagen-caja">
        <div className="subir-imagen-vista">
          {valor ? <img src={valor} alt={etiqueta} /> : <span>Sin imagen</span>}
        </div>
        <div className="subir-imagen-botones">
          <input id={id} ref={input} type="file" accept="image/png,image/jpeg,image/webp" onChange={elegir} hidden />
          <button type="button" className="btn btn-ghost" onClick={() => input.current.click()}>
            {valor ? 'Cambiar' : 'Subir imagen'}
          </button>
          {valor && <button type="button" className="btn btn-ghost" onClick={() => onChange('')}>Quitar</button>}
        </div>
      </div>
      {ayuda && <span className="hint">{ayuda}</span>}
      {error && <span className="hint" style={{ color: 'var(--danger, #b3261e)' }}>{error}</span>}
    </div>
  );
};

export default SubirImagen;
