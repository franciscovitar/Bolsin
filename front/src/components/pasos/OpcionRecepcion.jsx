import { useState } from 'react'
import { Button, FormControlLabel, Radio, RadioGroup, Stack, Typography } from '@mui/material'

// Pasos 7 y 8 del CU: el EB elige una de las opciones de recepción.
export default function OpcionRecepcion({ opciones, onElegir }) {
  const [opcion, setOpcion] = useState('')

  return (
    <Stack spacing={2}>
      <Typography variant="h6">¿Cómo se recibió el bolsín?</Typography>
      <RadioGroup value={opcion} onChange={(e) => setOpcion(e.target.value)}>
        {opciones.map((o) => <FormControlLabel key={o} value={o} control={<Radio />} label={o} />)}
      </RadioGroup>
      <Stack direction="row" justifyContent="flex-end">
        <Button variant="contained" disabled={!opcion} onClick={() => onElegir(opcion)}>Continuar</Button>
      </Stack>
    </Stack>
  )
}
