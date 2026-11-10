import { useState } from 'react'
import { Button, Card, CardContent, Checkbox, FormControlLabel, FormGroup, Stack, Typography } from '@mui/material'

// Flujos A3, A4 y A5: el EB marca, remito por remito, la documentación que no llegó (A3), que no
// corresponde a su comisión médica (A4) o que va a otra área de la CMC (A5). El mensaje lo da la
// pantalla según la opción elegida; si el detalle del remito tiene un área de la CMC, se muestra.
export default function SeleccionDocumentacion({ mensaje, remitos, onSeleccionar, onVolver }) {
  const [marcadas, setMarcadas] = useState([])

  const alternar = (nro) =>
    setMarcadas((actuales) => (actuales.includes(nro) ? actuales.filter((n) => n !== nro) : [...actuales, nro]))

  return (
    <Stack spacing={2}>
      <Typography variant="h6">{mensaje}</Typography>
      {remitos.map((r) => (
        <Card key={r.nroRemito} variant="outlined">
          <CardContent>
            <Typography variant="subtitle1" fontWeight={600}>Remito {r.nroRemito}</Typography>
            <FormGroup>
              {r.documentacion.map((d) => (
                <FormControlLabel
                  key={d.nroDocumentacion}
                  control={<Checkbox checked={marcadas.includes(d.nroDocumentacion)} onChange={() => alternar(d.nroDocumentacion)} />}
                  label={`${d.asunto} (${d.tipo})${d.areaCMCDestino ? ` · Área: ${d.areaCMCDestino}` : ''}`}
                />
              ))}
            </FormGroup>
          </CardContent>
        </Card>
      ))}
      <Stack direction="row" spacing={2} justifyContent="flex-end">
        <Button variant="outlined" onClick={onVolver}>Elegir otra opción</Button>
        <Button variant="contained" disabled={marcadas.length === 0} onClick={() => onSeleccionar(marcadas)}>
          Continuar
        </Button>
      </Stack>
    </Stack>
  )
}
