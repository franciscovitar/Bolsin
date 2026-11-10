import { alpha, createTheme } from '@mui/material/styles'

// Tema institucional del Sistema de Bolsines: azul profundo, neutros fríos y colores de estado
// discretos. Sólo usa Material UI (sin fuentes ni librerías externas).
const azul = '#17375e'
const neutro = { fondo: '#f3f5f8', borde: '#dde3ea', texto: '#1c2430', secundario: '#5b6878' }

const theme = createTheme({
  palette: {
    primary: { main: azul, light: '#2f5b8f', dark: '#0f2744', contrastText: '#ffffff' },
    secondary: { main: '#3f6e9e' },
    success: { main: '#2e7d5b' },
    warning: { main: '#a86b16' },
    error: { main: '#b3261e' },
    info: { main: '#2f6aa3' },
    background: { default: neutro.fondo, paper: '#ffffff' },
    text: { primary: neutro.texto, secondary: neutro.secundario },
    divider: neutro.borde,
  },
  shape: { borderRadius: 10 },
  typography: {
    fontFamily: '"Segoe UI", "Inter", Roboto, "Helvetica Neue", Arial, sans-serif',
    h5: { fontWeight: 650, letterSpacing: '-0.01em' },
    h6: { fontWeight: 650, letterSpacing: '-0.005em' },
    subtitle1: { fontWeight: 600 },
    subtitle2: { fontWeight: 600 },
    overline: { fontWeight: 600, letterSpacing: '0.08em' },
    button: { textTransform: 'none', fontWeight: 600 },
  },
  components: {
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: { root: { borderRadius: 8, paddingInline: 18 }, sizeSmall: { paddingInline: 14 } },
    },
    MuiPaper: { styleOverrides: { outlined: { borderColor: neutro.borde } } },
    MuiCard: { defaultProps: { variant: 'outlined' }, styleOverrides: { root: { borderColor: neutro.borde } } },
    MuiAlert: {
      styleOverrides: {
        root: { borderRadius: 8, alignItems: 'center' },
        standardWarning: { border: `1px solid ${alpha('#a86b16', 0.25)}` },
        standardInfo: { border: `1px solid ${alpha('#2f6aa3', 0.2)}` },
        standardError: { border: `1px solid ${alpha('#b3261e', 0.25)}` },
        standardSuccess: { border: `1px solid ${alpha('#2e7d5b', 0.25)}` },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        head: {
          fontWeight: 600, fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em',
          color: neutro.secundario, backgroundColor: '#f7f9fb',
        },
        root: { borderColor: neutro.borde },
      },
    },
    MuiChip: { styleOverrides: { root: { fontWeight: 500 } } },
    MuiStepLabel: { styleOverrides: { label: { fontSize: '0.8rem' } } },
    MuiTextField: { defaultProps: { size: 'small' } },
  },
})

export default theme
