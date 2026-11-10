import { Box, Button, Card, Chip, Divider, Stack, Typography } from '@mui/material'
import MarkEmailReadOutlinedIcon from '@mui/icons-material/MarkEmailReadOutlined'
import EncabezadoPaso from '../comunes/EncabezadoPaso'

// Fila "dato: valor" del resumen.
function Dato({ titulo, children }) {
  return (
    <Stack direction={{ xs: 'column', sm: 'row' }} spacing={{ xs: 0.5, sm: 2 }} sx={{ px: 2, py: 1.5 }}>
      <Typography variant="body2" color="text.secondary" sx={{ width: { sm: 200 }, flexShrink: 0 }}>{titulo}</Typography>
      <Box sx={{ minWidth: 0 }}>{children}</Box>
    </Stack>
  )
}

// Pasos 9 y 10 del CU: el sistema pide confirmación y el EB confirma (o no: flujo A6).
// En A3, A4 y A5 también se listan los asuntos de la documentación que marcó el EB.
export default function Confirmacion({ mensaje, nroPrecinto, opcion, marcadas, onConfirmar }) {
  return (
    <Box>
      <EncabezadoPaso
        icono={<MarkEmailReadOutlinedIcon />}
        titulo={mensaje}
        subtitulo="Revise el resumen. Al confirmar se registra la recepción y se notifica a cada comisión médica de origen."
      />

      <Card>
        <Stack divider={<Divider flexItem />}>
          <Dato titulo="Bolsín con precinto">
            <Typography variant="body2" sx={{ fontWeight: 600, fontVariantNumeric: 'tabular-nums' }}>{nroPrecinto}</Typography>
          </Dato>
          <Dato titulo="Opción de recepción">
            <Typography variant="body2" sx={{ fontWeight: 500 }}>{opcion}</Typography>
          </Dato>
          {marcadas.length > 0 && (
            <Dato titulo="Documentación marcada">
              <Stack direction="row" spacing={0.75} useFlexGap flexWrap="wrap">
                {marcadas.map((asunto) => (
                  <Chip key={asunto} label={asunto} size="small" color="warning" variant="outlined" />
                ))}
              </Stack>
            </Dato>
          )}
        </Stack>
      </Card>

      <Stack direction={{ xs: 'column-reverse', sm: 'row' }} spacing={1.5} justifyContent="flex-end" sx={{ mt: 3 }}>
        <Button variant="outlined" color="inherit" onClick={() => onConfirmar(false)}>No confirmar</Button>
        <Button variant="contained" onClick={() => onConfirmar(true)}>Confirmar recepción</Button>
      </Stack>
    </Box>
  )
}
