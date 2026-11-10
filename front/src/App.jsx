import { useCallback, useEffect, useState } from 'react'
import { Alert, CircularProgress, CssBaseline, Stack, ThemeProvider } from '@mui/material'
import MainLayout from './components/layout/MainLayout'
import SeleccionBolsin from './components/pasos/SeleccionBolsin'
import ContenidoBolsin from './components/pasos/ContenidoBolsin'
import OpcionRecepcion from './components/pasos/OpcionRecepcion'
import SeleccionDocumentacion from './components/pasos/SeleccionDocumentacion'
import Confirmacion from './components/pasos/Confirmacion'
import Resultado from './components/pasos/Resultado'
import {
  mensajeDeError, opcRegistrarRecepcionBolsin, tomarConfirmacionRegistro, tomarSeleccionBolsin,
  tomarSeleccionDocumentacion, tomarSeleccionOpcionRecepcion,
} from './services/api'
import theme from './theme'

// Pantalla del CU 28. Cada paso llama al mensaje correspondiente de PantallaRegistroRecepcionBolsines.
export default function App() {
  const [paso, setPaso] = useState(0)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [nombreCM, setNombreCM] = useState('')
  const [bolsines, setBolsines] = useState([])
  const [nroPrecinto, setNroPrecinto] = useState(null)
  const [seleccion, setSeleccion] = useState({ remitos: [], opciones: [] })
  const [opcion, setOpcion] = useState('')
  const [mensaje, setMensaje] = useState('')
  const [exito, setExito] = useState(false)
  const [marcarDocumentacion, setMarcarDocumentacion] = useState(false)
  const [marcadas, setMarcadas] = useState([])

  // Envuelve cada llamado: muestra la espera y el mensaje de error que devuelve el back.
  const ejecutar = useCallback(async (llamado) => {
    setCargando(true)
    setError('')
    try {
      await llamado()
    } catch (e) {
      setError(mensajeDeError(e))
    } finally {
      setCargando(false)
    }
  }, [])

  // opcRegistrarRecepcionBolsin(): CM del usuario logueado y bolsines enviados.
  const iniciar = useCallback(() => ejecutar(async () => {
    const datos = await opcRegistrarRecepcionBolsin()
    setNombreCM(datos.nombreCM)
    setBolsines(datos.bolsines)
    setMarcarDocumentacion(false)
    setMarcadas([])
    setPaso(0)
  }), [ejecutar])

  useEffect(() => { iniciar() }, [iniciar])

  const seleccionarBolsin = (nro) => ejecutar(async () => {
    setSeleccion(await tomarSeleccionBolsin(nro))
    setNroPrecinto(nro)
    setPaso(1)
  })

  // Si la pantalla pide marcar documentación (A3 o A4), se queda en este paso.
  const elegirOpcion = (opcionElegida) => ejecutar(async () => {
    const respuesta = await tomarSeleccionOpcionRecepcion(opcionElegida)
    setOpcion(opcionElegida)
    setMensaje(respuesta.mensaje)
    setMarcadas([])
    setMarcarDocumentacion(respuesta.seleccionarDocumentacion)
    if (!respuesta.seleccionarDocumentacion) setPaso(3)
  })

  // A3 y A4: tomarSeleccionDocumentacion(nrosDocumentacion).
  const seleccionarDocumentacion = (nros) => ejecutar(async () => {
    const respuesta = await tomarSeleccionDocumentacion(nros)
    setMensaje(respuesta.mensaje)
    setMarcadas(seleccion.remitos.flatMap((r) => r.documentacion)
      .filter((d) => nros.includes(d.nroDocumentacion)).map((d) => d.asunto))
    setPaso(3)
  })

  const confirmar = (confirmacion) => ejecutar(async () => {
    const respuesta = await tomarConfirmacionRegistro(confirmacion)
    setMensaje(respuesta.mensaje)
    setExito(confirmacion)
    setPaso(4)
  })

  const pasos = [
    <SeleccionBolsin key="seleccion" bolsines={bolsines} onSeleccionar={seleccionarBolsin} />,
    <ContenidoBolsin key="contenido" nroPrecinto={nroPrecinto} remitos={seleccion.remitos} onContinuar={() => setPaso(2)} />,
    marcarDocumentacion
      ? <SeleccionDocumentacion key="documentacion" mensaje={mensaje} remitos={seleccion.remitos}
          onSeleccionar={seleccionarDocumentacion} onVolver={() => setMarcarDocumentacion(false)} />
      : <OpcionRecepcion key="opcion" opciones={seleccion.opciones} onElegir={elegirOpcion} />,
    <Confirmacion key="confirmacion" mensaje={mensaje} nroPrecinto={nroPrecinto} opcion={opcion}
      marcadas={marcadas} onConfirmar={confirmar} />,
    <Resultado key="resultado" mensaje={mensaje} exito={exito} onReiniciar={iniciar} />,
  ]

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <MainLayout pasoActual={paso} nombreCM={nombreCM} onCancelar={iniciar}>
        <Stack spacing={2}>
          {error && <Alert severity="error" onClose={() => setError('')}>{error}</Alert>}
          {cargando ? <Stack alignItems="center" py={4}><CircularProgress /></Stack> : pasos[paso]}
        </Stack>
      </MainLayout>
    </ThemeProvider>
  )
}
