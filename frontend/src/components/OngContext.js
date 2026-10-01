import { createContext } from 'react';

// Permite que una página cuya URL no lleva el id de la ONG (por ejemplo, el detalle de una factura)
// indique a la barra lateral a qué ONG pertenece, para mostrar su menú.
export const OngContext = createContext({ fijarOng: () => {} });
