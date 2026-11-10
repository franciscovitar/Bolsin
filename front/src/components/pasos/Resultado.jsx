import { Alert, Button, Stack } from '@mui/material'

// Paso 12 del CU: el sistema informa el resultado (ejecución exitosa o cancelación).
export default function Resultado({ mensaje, exito, onReiniciar }) {
  return (
    <Stack spacing={2}>
      <Alert severity={exito ? 'success' : 'info'}>{mensaje}</Alert>
      <Stack direction="row" justifyContent="flex-end">
        <Button variant="contained" onClick={onReiniciar}>Registrar otra recepción</Button>
      </Stack>
    </Stack>
  )
}
