import axios from 'axios'

// Un llamado por cada mensaje del Encargado de Bolsines a la pantalla (diagrama de secuencia).
const pantalla = axios.create({ baseURL: '/api/registro-recepcion-bolsin' })

// Devuelve el mensaje que manda el back cuando algo no se puede hacer.
export const mensajeDeError = (error) =>
  error.response?.data?.mensaje ?? 'No se pudo comunicar con el servidor (¿está levantado el back en :8080?).'

export const opcRegistrarRecepcionBolsin = async () =>
  (await pantalla.post('/opcRegistrarRecepcionBolsin')).data

export const tomarSeleccionBolsin = async (nroPrecinto) =>
  (await pantalla.post('/tomarSeleccionBolsin', { nroPrecinto })).data

export const tomarSeleccionOpcionRecepcion = async (opcion) =>
  (await pantalla.post('/tomarSeleccionOpcionRecepcion', { opcion })).data

// A3 y A4: documentación que el EB marca como no recibida o que no corresponde al destino
// (mensaje nuevo, extensión del diagrama).
export const tomarSeleccionDocumentacion = async (nrosDocumentacion) =>
  (await pantalla.post('/tomarSeleccionDocumentacion', { nrosDocumentacion })).data

export const tomarConfirmacionRegistro = async (confirmacion) =>
  (await pantalla.post('/tomarConfirmacionRegistro', { confirmacion })).data
