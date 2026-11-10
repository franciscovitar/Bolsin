import { useState } from 'react'
import { Box, Button, Radio, RadioGroup, Stack, Typography } from '@mui/material'
import { alpha } from '@mui/material/styles'
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline'
import RemoveCircleOutlineIcon from '@mui/icons-material/RemoveCircleOutline'
import BlockOutlinedIcon from '@mui/icons-material/BlockOutlined'
import AltRouteIcon from '@mui/icons-material/AltRoute'
import InventoryOutlinedIcon from '@mui/icons-material/Inventory2Outlined'
import ArrowForwardIcon from '@mui/icons-material/ArrowForward'
import EncabezadoPaso from '../comunes/EncabezadoPaso'

// Ícono de cada opción, en el orden del paso 7 del CU (ERS). Sólo es presentación: la opción que
// se envía al back es el texto que él mismo mandó.
const ICONOS = [CheckCircleOutlineIcon, RemoveCircleOutlineIcon, BlockOutlinedIcon, AltRouteIcon]

// Pasos 7 y 8 del CU: el EB elige una de las opciones de recepción.
export default function OpcionRecepcion({ opciones, onElegir }) {
  const [opcion, setOpcion] = useState('')

  return (
    <Box>
      <EncabezadoPaso
        icono={<InventoryOutlinedIcon />}
        titulo="¿Cómo se recibió el bolsín?"
        subtitulo="Seleccione la opción que corresponde a lo recibido."
      />

      <RadioGroup value={opcion} onChange={(e) => setOpcion(e.target.value)}>
        <Stack spacing={1.25}>
          {opciones.map((o, i) => {
            const Icono = ICONOS[i] ?? CheckCircleOutlineIcon
            const elegida = o === opcion
            return (
              <Box
                key={o} component="label"
                sx={(t) => ({
                  display: 'flex', alignItems: 'center', gap: 1.5, px: 1.5, py: 1.25, cursor: 'pointer',
                  borderRadius: 2, border: '1px solid',
                  borderColor: elegida ? 'primary.main' : 'divider',
                  bgcolor: elegida ? alpha(t.palette.primary.main, 0.05) : 'background.paper',
                  boxShadow: elegida ? `0 0 0 1px ${t.palette.primary.main}` : 'none',
                  transition: 'border-color .15s, background-color .15s',
                  '&:hover': { borderColor: 'primary.light' },
                })}
              >
                <Radio value={o} size="small" />
                <Icono sx={{ color: elegida ? 'primary.main' : 'text.secondary' }} />
                <Box sx={{ minWidth: 0 }}>
                  <Typography variant="body2" sx={{ fontWeight: elegida ? 600 : 500 }}>{o}</Typography>
                  {i > 0 && (
                    <Typography variant="caption" component="div" color="text.secondary" sx={{ lineHeight: 1.4, mt: 0.25 }}>
                      A continuación deberá marcar la documentación correspondiente.
                    </Typography>
                  )}
                </Box>
              </Box>
            )
          })}
        </Stack>
      </RadioGroup>

      <Stack direction="row" justifyContent="flex-end" sx={{ mt: 3 }}>
        <Button variant="contained" endIcon={<ArrowForwardIcon />} disabled={!opcion} onClick={() => onElegir(opcion)}>
          Continuar
        </Button>
      </Stack>
    </Box>
  )
}
