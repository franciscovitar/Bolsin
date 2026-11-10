import { Box, Chip, Stack, Typography } from '@mui/material'
import ApartmentOutlinedIcon from '@mui/icons-material/ApartmentOutlined'

// Asunto de una documentación con su tipo y, si el detalle del remito lo tiene, el área de la CMC.
export default function DatosDocumento({ documento }) {
  return (
    <Box sx={{ minWidth: 0 }}>
      <Typography variant="body2" sx={{ fontWeight: 500, overflowWrap: 'anywhere' }}>{documento.asunto}</Typography>
      <Stack direction="row" spacing={0.75} useFlexGap flexWrap="wrap" sx={{ mt: 0.5 }}>
        <Chip label={documento.tipo} size="small" variant="outlined" />
        {documento.areaCMCDestino && (
          <Chip
            icon={<ApartmentOutlinedIcon />} label={`Área: ${documento.areaCMCDestino}`} size="small"
            color="primary" variant="outlined"
          />
        )}
      </Stack>
    </Box>
  )
}
