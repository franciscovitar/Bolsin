import { Box, Stack, Typography } from '@mui/material'
import { alpha } from '@mui/material/styles'

// Encabezado común de cada paso del asistente: ícono, título y una línea de ayuda opcional.
export default function EncabezadoPaso({ icono, titulo, subtitulo, extra }) {
  return (
    <Stack direction="row" spacing={1.75} alignItems="flex-start" sx={{ mb: 2.5 }}>
      <Box sx={(t) => ({
        width: 42, height: 42, borderRadius: 2.5, flexShrink: 0, display: 'grid', placeItems: 'center',
        color: 'primary.main', bgcolor: alpha(t.palette.primary.main, 0.08),
      })}>
        {icono}
      </Box>
      <Box sx={{ flexGrow: 1, minWidth: 0 }}>
        <Typography variant="h6" component="h2" sx={{ lineHeight: 1.3 }}>{titulo}</Typography>
        {subtitulo && <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>{subtitulo}</Typography>}
      </Box>
      {extra && <Box sx={{ flexShrink: 0, display: { xs: 'none', sm: 'block' } }}>{extra}</Box>}
    </Stack>
  )
}
