import { createTheme } from '@mui/material/styles'

// Tema provisorio. Los colores y la tipografía definitivos salen del moodboard del grupo.
const theme = createTheme({
  palette: {
    primary: { main: '#1f3a5f' },
    secondary: { main: '#c8873a' },
    background: { default: '#f4f6f8' },
  },
  shape: { borderRadius: 8 },
})

export default theme
