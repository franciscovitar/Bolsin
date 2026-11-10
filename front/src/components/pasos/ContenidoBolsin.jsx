import { Button, Card, CardContent, List, ListItem, ListItemText, Stack, Typography } from '@mui/material'
import DescriptionOutlinedIcon from '@mui/icons-material/DescriptionOutlined'

// Paso 6 del CU: cada remito del bolsín con el asunto y el tipo de su documentación,
// para que el EB controle el contenido físico contra lo registrado.
export default function ContenidoBolsin({ nroPrecinto, remitos, onContinuar }) {
  return (
    <Stack spacing={2}>
      <Typography variant="h6">Contenido del bolsín con precinto {nroPrecinto}</Typography>
      <Typography color="text.secondary">
        Controle que la documentación física coincida con la registrada.
      </Typography>
      {remitos.map((r) => (
        <Card key={r.nroRemito} variant="outlined">
          <CardContent>
            <Typography variant="subtitle1" fontWeight={600}>Remito {r.nroRemito}</Typography>
            <List dense>
              {r.documentacion.map((d) => (
                <ListItem key={d.nroDocumentacion}>
                  <DescriptionOutlinedIcon fontSize="small" sx={{ mr: 1.5, color: 'text.secondary' }} />
                  <ListItemText primary={d.asunto}
                    secondary={d.areaCMCDestino ? `${d.tipo} · Área: ${d.areaCMCDestino}` : d.tipo} />
                </ListItem>
              ))}
            </List>
          </CardContent>
        </Card>
      ))}
      <Stack direction="row" justifyContent="flex-end">
        <Button variant="contained" onClick={onContinuar}>Continuar</Button>
      </Stack>
    </Stack>
  )
}
