import {
  AppBar, Box, Button, Chip, Container, LinearProgress, Paper, Step, StepLabel, Stepper, Toolbar, Typography,
  useMediaQuery,
} from '@mui/material'
import { useTheme } from '@mui/material/styles'
import LocalShippingOutlinedIcon from '@mui/icons-material/LocalShippingOutlined'
import PlaceOutlinedIcon from '@mui/icons-material/PlaceOutlined'

// Pasos del CU 28 tal como los ve el Encargado de Bolsines (wizard, como el repo de la cátedra).
export const PASOS = ['Seleccionar bolsín', 'Controlar contenido', 'Opción de recepción', 'Confirmar', 'Resultado']

// Comisión médica del usuario logueado (paso 2 del CU: mostrarCM).
function ChipCM({ nombreCM }) {
  return (
    <Chip
      icon={<PlaceOutlinedIcon />} label={nombreCM} variant="outlined"
      sx={{
        color: 'common.white', borderColor: 'rgba(255,255,255,0.35)', bgcolor: 'rgba(255,255,255,0.08)',
        '& .MuiChip-icon': { color: 'inherit' },
      }}
    />
  )
}

export default function MainLayout({ pasoActual, nombreCM, onCancelar, children }) {
  const puedeCancelar = onCancelar && pasoActual > 0 && pasoActual < PASOS.length - 1
  const compacto = useMediaQuery(useTheme().breakpoints.down('sm'))

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default', display: 'flex', flexDirection: 'column' }}>
      <AppBar position="static" elevation={0}>
        <Toolbar sx={{ gap: 1.5, minHeight: { xs: 60, sm: 68 } }}>
          <Box sx={{
            width: 38, height: 38, borderRadius: 2, display: 'grid', placeItems: 'center',
            bgcolor: 'rgba(255,255,255,0.12)', flexShrink: 0,
          }}>
            <LocalShippingOutlinedIcon fontSize="small" />
          </Box>
          <Box sx={{ flexGrow: 1, minWidth: 0 }}>
            <Typography variant="overline" noWrap sx={{ display: 'block', lineHeight: 1.4, opacity: 0.75 }}>
              Sistema de Bolsines
            </Typography>
            <Typography variant="subtitle1" component="h1" sx={{ lineHeight: 1.2 }}>
              Registrar recepción de bolsín
            </Typography>
          </Box>
          {nombreCM && !compacto && <ChipCM nombreCM={nombreCM} />}
        </Toolbar>
        {/* En pantallas chicas la CM del usuario va en una franja propia, para que no se corte. */}
        {nombreCM && compacto && (
          <Box sx={{ px: 2, pb: 1.25 }}><ChipCM nombreCM={nombreCM} /></Box>
        )}
      </AppBar>

      <Container maxWidth="md" sx={{ py: { xs: 2.5, sm: 4 }, flexGrow: 1 }}>
        <Paper variant="outlined" sx={{ px: { xs: 2, sm: 3 }, py: { xs: 1.5, sm: 2.5 }, mb: 2.5 }}>
          {compacto ? (
            <Box>
              <Typography variant="caption" color="text.secondary">
                Paso {pasoActual + 1} de {PASOS.length}
              </Typography>
              <Typography variant="subtitle2" sx={{ mb: 1 }}>{PASOS[pasoActual]}</Typography>
              <LinearProgress
                variant="determinate" value={((pasoActual + 1) / PASOS.length) * 100}
                sx={{ height: 6, borderRadius: 3 }}
              />
            </Box>
          ) : (
            <Stepper activeStep={pasoActual} alternativeLabel>
              {PASOS.map((paso) => (
                <Step key={paso}><StepLabel>{paso}</StepLabel></Step>
              ))}
            </Stepper>
          )}
        </Paper>

        <Paper variant="outlined" sx={{ p: { xs: 2, sm: 3.5 } }}>{children}</Paper>

        {puedeCancelar && (
          <Box sx={{ mt: 1.5, textAlign: 'right' }}>
            <Button color="inherit" onClick={onCancelar} sx={{ color: 'text.secondary' }}>Cancelar registro</Button>
          </Box>
        )}
      </Container>

      <Box component="footer" sx={{ py: 2, textAlign: 'center' }}>
        <Typography variant="caption" color="text.secondary">
          CU 28 · Registrar recepción de bolsín · PPAI DSI 2026
        </Typography>
      </Box>
    </Box>
  )
}
