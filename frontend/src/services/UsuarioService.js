import axios from 'axios';

const API_URL = 'http://localhost:8080/api/usuarios'; 

export const getUsuarios = () => axios.get(API_URL);

export const getUsuarioById = (id) => axios.get(`${API_URL}/${id}`);

export const createUsuario = (usuario) => axios.post(API_URL, usuario);

export const updateUsuario = (id, usuario) => axios.put(`${API_URL}/${id}`, usuario);

export const deleteUsuario = (id) => axios.delete(`${API_URL}/${id}`);
