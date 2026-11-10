import { AppBar, Box, Button, Chip, Container, Paper, Step, StepLabel, Stepper, Toolbar, Typography } from '@mui/material'
import MailOutlineIcon from '@mui/icons-material/MailOutline'
import PlaceOutlinedIcon from '@mui/icons-material/PlaceOutlined'

// Pasos del CU 28 tal como los ve el Encargado de Bolsines (wizard, como el repo de la cátedra).
export const PASOS = ['Seleccionar bolsín', 'Controlar contenido', 'Opción de recepción', 'Confirmar', 'Resultado']

export default function MainLayout({ pasoActual, nombreCM, onCancelar, children }) {
  const puedeCancelar = onCancelar && pasoActual > 0 && pasoActual < PASOS.length - 1

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="static" elevation={0}>
        <Toolbar sx={{ gap: 2 }}>
          <MailOutlineIcon />
          <Typography variant="h6" sx={{ flexGrow: 1 }}>Registrar recepción de bolsín</Typography>
          {nombreCM && (
            <Chip icon={<PlaceOutlinedIcon />} label={nombreCM} color="secondary" sx={{ color: 'white' }} />
          )}
        </Toolbar>
      </AppBar>
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Stepper activeStep={pasoActual} alternativeLabel sx={{ mb: 4 }}>
          {PASOS.map((paso) => (
            <Step key={paso}><StepLabel>{paso}</StepLabel></Step>
          ))}
        </Stepper>
        <Paper variant="outlined" sx={{ p: 3 }}>{children}</Paper>
        {puedeCancelar && (
          <Box sx={{ mt: 2, textAlign: 'right' }}>
            <Button color="inherit" onClick={onCancelar}>Cancelar registro</Button>
          </Box>
        )}
      </Container>
    </Box>
  )
}
