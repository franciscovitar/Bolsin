import { Box, Button, Typography } from '@mui/material'
import { alpha } from '@mui/material/styles'
import TaskAltIcon from '@mui/icons-material/TaskAlt'
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined'

// Paso 12 del CU: el sistema informa el resultado (ejecución exitosa o cancelación).
export default function Resultado({ mensaje, exito, onReiniciar }) {
  const color = exito ? 'success' : 'info'
  const Icono = exito ? TaskAltIcon : InfoOutlinedIcon

  return (
    <Box sx={{ textAlign: 'center', py: { xs: 2, sm: 4 } }} role="status">
      <Box sx={(t) => ({
        width: 72, height: 72, mx: 'auto', mb: 2, borderRadius: '50%', display: 'grid', placeItems: 'center',
        color: `${color}.main`, bgcolor: alpha(t.palette[color].main, 0.1),
      })}>
        <Icono sx={{ fontSize: 40 }} />
      </Box>
      <Typography variant="h6" component="h2" sx={{ mb: 1 }}>
        {exito ? 'Recepción registrada' : 'Registro no confirmado'}
      </Typography>
      <Typography color="text.secondary" sx={{ maxWidth: 520, mx: 'auto', mb: 3 }}>{mensaje}</Typography>
      <Button variant="contained" onClick={onReiniciar}>Registrar otra recepción</Button>
    </Box>
  )
}
