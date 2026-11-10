import { useState } from 'react'
import { Box, Button, Card, Checkbox, Chip, Divider, Stack, Typography } from '@mui/material'
import { alpha } from '@mui/material/styles'
import ReceiptLongOutlinedIcon from '@mui/icons-material/ReceiptLongOutlined'
import FactCheckOutlinedIcon from '@mui/icons-material/FactCheckOutlined'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import ArrowForwardIcon from '@mui/icons-material/ArrowForward'
import EncabezadoPaso from '../comunes/EncabezadoPaso'
import DatosDocumento from '../comunes/DatosDocumento'

// Flujos A3, A4 y A5: el EB marca, remito por remito, la documentación que no llegó (A3), que no
// corresponde a su comisión médica (A4) o que va a otra área de la CMC (A5). El mensaje lo da la
// pantalla según la opción elegida; si el detalle del remito tiene un área de la CMC, se muestra.
export default function SeleccionDocumentacion({ mensaje, remitos, onSeleccionar, onVolver }) {
  const [marcadas, setMarcadas] = useState([])

  const alternar = (nro) =>
    setMarcadas((actuales) => (actuales.includes(nro) ? actuales.filter((n) => n !== nro) : [...actuales, nro]))

  return (
    <Box>
      <EncabezadoPaso
        icono={<FactCheckOutlinedIcon />}
        titulo={mensaje}
        subtitulo="Puede marcar una o más documentaciones."
        extra={<Chip color={marcadas.length ? 'primary' : 'default'} label={`${marcadas.length} marcada${marcadas.length === 1 ? '' : 's'}`} />}
      />

      <Stack spacing={1.5}>
        {remitos.map((r) => (
          <Card key={r.nroRemito}>
            <Stack direction="row" alignItems="center" spacing={1} sx={{ px: 2, py: 1.25, bgcolor: '#f7f9fb' }}>
              <ReceiptLongOutlinedIcon fontSize="small" color="primary" />
              <Typography variant="subtitle2">Remito {r.nroRemito}</Typography>
            </Stack>
            <Divider />
            <Stack divider={<Divider flexItem />}>
              {r.documentacion.map((d) => {
                const marcada = marcadas.includes(d.nroDocumentacion)
                return (
                  <Box
                    key={d.nroDocumentacion} component="label"
                    sx={(t) => ({
                      display: 'flex', alignItems: 'flex-start', gap: 1, px: 1, py: 1, cursor: 'pointer',
                      bgcolor: marcada ? alpha(t.palette.primary.main, 0.06) : 'transparent',
                      borderLeft: '3px solid', borderLeftColor: marcada ? 'primary.main' : 'transparent',
                      transition: 'background-color .15s',
                      '&:hover': { bgcolor: alpha(t.palette.primary.main, marcada ? 0.08 : 0.03) },
                    })}
                  >
                    <Checkbox
                      checked={marcada} onChange={() => alternar(d.nroDocumentacion)}
                      slotProps={{ input: { 'aria-label': d.asunto } }}
                    />
                    <Box sx={{ pt: 1, minWidth: 0 }}><DatosDocumento documento={d} /></Box>
                  </Box>
                )
              })}
            </Stack>
          </Card>
        ))}
      </Stack>

      <Stack direction={{ xs: 'column-reverse', sm: 'row' }} spacing={1.5} justifyContent="flex-end" sx={{ mt: 3 }}>
        <Button variant="outlined" startIcon={<ArrowBackIcon />} onClick={onVolver}>Elegir otra opción</Button>
        <Button
          variant="contained" endIcon={<ArrowForwardIcon />} disabled={marcadas.length === 0}
          onClick={() => onSeleccionar(marcadas)}
        >
          Continuar
        </Button>
      </Stack>
    </Box>
  )
}
