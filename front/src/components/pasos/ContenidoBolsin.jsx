import { Box, Button, Card, Chip, Divider, Stack, Typography } from '@mui/material'
import DescriptionOutlinedIcon from '@mui/icons-material/DescriptionOutlined'
import ReceiptLongOutlinedIcon from '@mui/icons-material/ReceiptLongOutlined'
import FactCheckOutlinedIcon from '@mui/icons-material/FactCheckOutlined'
import ArrowForwardIcon from '@mui/icons-material/ArrowForward'
import EncabezadoPaso from '../comunes/EncabezadoPaso'
import DatosDocumento from '../comunes/DatosDocumento'

// Paso 6 del CU: cada remito del bolsín con el asunto y el tipo de su documentación,
// para que el EB controle el contenido físico contra lo registrado.
export default function ContenidoBolsin({ nroPrecinto, remitos, onContinuar }) {
  const cantidadDocumentos = remitos.reduce((total, r) => total + r.documentacion.length, 0)

  return (
    <Box>
      <EncabezadoPaso
        icono={<FactCheckOutlinedIcon />}
        titulo={`Contenido del bolsín con precinto ${nroPrecinto}`}
        subtitulo="Controle que la documentación física coincida con la registrada."
      />

      <Stack direction="row" spacing={1} sx={{ mb: 2 }}>
        <Chip size="small" icon={<ReceiptLongOutlinedIcon />} label={`${remitos.length} ${remitos.length === 1 ? 'remito' : 'remitos'}`} />
        <Chip size="small" icon={<DescriptionOutlinedIcon />} label={`${cantidadDocumentos} ${cantidadDocumentos === 1 ? 'documentación' : 'documentaciones'}`} />
      </Stack>

      <Stack spacing={1.5}>
        {remitos.map((r) => (
          <Card key={r.nroRemito}>
            <Stack direction="row" alignItems="center" spacing={1} sx={{ px: 2, py: 1.25, bgcolor: '#f7f9fb' }}>
              <ReceiptLongOutlinedIcon fontSize="small" color="primary" />
              <Typography variant="subtitle2" sx={{ flexGrow: 1 }}>Remito {r.nroRemito}</Typography>
              <Typography variant="caption" color="text.secondary">
                {r.documentacion.length} {r.documentacion.length === 1 ? 'documentación' : 'documentaciones'}
              </Typography>
            </Stack>
            <Divider />
            <Stack divider={<Divider flexItem />}>
              {r.documentacion.map((d) => (
                <Stack key={d.nroDocumentacion} direction="row" spacing={1.5} alignItems="flex-start" sx={{ px: 2, py: 1.5 }}>
                  <DescriptionOutlinedIcon fontSize="small" sx={{ color: 'text.secondary', mt: 0.25 }} />
                  <DatosDocumento documento={d} />
                </Stack>
              ))}
            </Stack>
          </Card>
        ))}
      </Stack>

      <Stack direction="row" justifyContent="flex-end" sx={{ mt: 3 }}>
        <Button variant="contained" endIcon={<ArrowForwardIcon />} onClick={onContinuar}>Continuar</Button>
      </Stack>
    </Box>
  )
}
