import { useMemo, useState } from 'react'
import {
  Alert, Button, InputAdornment, Stack, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography,
} from '@mui/material'
import SearchIcon from '@mui/icons-material/Search'

// Paso 4 y 5 del CU: lista de bolsines enviados a la CM del usuario, con filtro por
// número de precinto o CM origen (flujos alternativos A1 y A2 cuando el filtro no encuentra nada).
export default function SeleccionBolsin({ bolsines, onSeleccionar }) {
  const [precinto, setPrecinto] = useState('')
  const [origen, setOrigen] = useState('')

  const filtrados = useMemo(
    () => bolsines.filter((b) =>
      String(b.nroPrecinto).includes(precinto.trim())
      && b.origen.toLowerCase().includes(origen.trim().toLowerCase())),
    [bolsines, precinto, origen],
  )

  if (bolsines.length === 0) {
    return <Alert severity="info">No hay bolsines enviados a su comisión médica.</Alert>
  }

  const sinResultado = precinto.trim()
    ? `No se encontró un bolsín con el número de precinto "${precinto.trim()}".`
    : `No se encontró un bolsín de la comisión médica origen "${origen.trim()}".`

  return (
    <Stack spacing={2}>
      <Typography variant="h6">Bolsines enviados a su comisión médica</Typography>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <TextField
          label="Número de precinto" value={precinto} onChange={(e) => setPrecinto(e.target.value)} size="small"
          slotProps={{ input: { startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment> } }}
        />
        <TextField label="Comisión médica origen" value={origen} onChange={(e) => setOrigen(e.target.value)} size="small" />
      </Stack>
      {filtrados.length === 0 ? (
        <Alert severity="warning">{sinResultado}</Alert>
      ) : (
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Número de precinto</TableCell>
              <TableCell>Comisión médica origen</TableCell>
              <TableCell />
            </TableRow>
          </TableHead>
          <TableBody>
            {filtrados.map((b) => (
              <TableRow key={b.nroPrecinto} hover>
                <TableCell>{b.nroPrecinto}</TableCell>
                <TableCell>{b.origen}</TableCell>
                <TableCell align="right">
                  <Button variant="contained" size="small" onClick={() => onSeleccionar(b.nroPrecinto)}>
                    Seleccionar
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </Stack>
  )
}
