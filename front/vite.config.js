import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// En desarrollo el front corre en :3000 y reenvia /api y /actuator al back en :8080
// (como el repo de la catedra), asi no hace falta configurar CORS.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
      '/actuator': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
})
