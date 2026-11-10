import { useMemo, useState } from 'react'
import {
  Alert, Box, Button, Card, Chip, InputAdornment, Stack, Table, TableBody, TableCell, TableContainer, TableHead,
  TableRow, TextField, Typography,
} from '@mui/material'
import SearchIcon from '@mui/icons-material/Search'
import AccountBalanceOutlinedIcon from '@mui/icons-material/AccountBalanceOutlined'
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined'
import ArrowForwardIcon from '@mui/icons-material/ArrowForward'
import EncabezadoPaso from '../comunes/EncabezadoPaso'

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
    return (
      <Box sx={{ textAlign: 'center', py: 5 }}>
        <Inventory2OutlinedIcon sx={{ fontSize: 48, color: 'text.disabled', mb: 1 }} />
        <Alert severity="info" sx={{ justifyContent: 'center' }}>No hay bolsines enviados a su comisión médica.</Alert>
      </Box>
    )
  }

  const sinResultado = precinto.trim()
    ? `No se encontró un bolsín con el número de precinto "${precinto.trim()}".`
    : `No se encontró un bolsín de la comisión médica origen "${origen.trim()}".`

  const seleccionar = (b) => (
    <Button variant="contained" size="small" endIcon={<ArrowForwardIcon />} onClick={() => onSeleccionar(b.nroPrecinto)}>
      Seleccionar
    </Button>
  )

  return (
    <Box>
      <EncabezadoPaso
        icono={<Inventory2OutlinedIcon />}
        titulo="Bolsines enviados a su comisión médica"
        subtitulo="Seleccione el bolsín recibido. Puede filtrar por número de precinto o por comisión médica origen."
        extra={<Chip label={`${bolsines.length} ${bolsines.length === 1 ? 'bolsín' : 'bolsines'}`} />}
      />

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5} sx={{ mb: 2.5 }}>
        <TextField
          label="Número de precinto" value={precinto} onChange={(e) => setPrecinto(e.target.value)} fullWidth
          slotProps={{ input: { startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment> } }}
        />
        <TextField
          label="Comisión médica origen" value={origen} onChange={(e) => setOrigen(e.target.value)} fullWidth
          slotProps={{ input: { startAdornment: <InputAdornment position="start"><AccountBalanceOutlinedIcon fontSize="small" /></InputAdornment> } }}
        />
      </Stack>

      {filtrados.length === 0 ? (
        <Alert severity="warning">{sinResultado}</Alert>
      ) : (
        <>
          {/* Escritorio y tablet: tabla. */}
          <TableContainer component={Card} sx={{ display: { xs: 'none', sm: 'block' } }}>
            <Table size="medium">
              <TableHead>
                <TableRow>
                  <TableCell>Número de precinto</TableCell>
                  <TableCell>Comisión médica origen</TableCell>
                  <TableCell />
                </TableRow>
              </TableHead>
              <TableBody>
                {filtrados.map((b) => (
                  <TableRow key={b.nroPrecinto} hover sx={{ '&:last-child td': { borderBottom: 0 } }}>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 600, fontVariantNumeric: 'tabular-nums' }}>
                        {b.nroPrecinto}
                      </Typography>
                    </TableCell>
                    <TableCell>{b.origen}</TableCell>
                    <TableCell align="right">{seleccionar(b)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          {/* Celular: una tarjeta por bolsín. */}
          <Stack spacing={1.25} sx={{ display: { xs: 'flex', sm: 'none' } }}>
            {filtrados.map((b) => (
              <Card key={b.nroPrecinto} sx={{ p: 1.75 }}>
                <Typography variant="caption" color="text.secondary">Número de precinto</Typography>
                <Typography variant="subtitle1" sx={{ fontVariantNumeric: 'tabular-nums' }}>{b.nroPrecinto}</Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5 }}>{b.origen}</Typography>
                <Box sx={{ '& .MuiButton-root': { width: '100%' } }}>{seleccionar(b)}</Box>
              </Card>
            ))}
          </Stack>
        </>
      )}
    </Box>
  )
}
