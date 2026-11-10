# PPAI 2026 · Bolsines · CU 28 Registrar recepción de bolsín

Diseño de Sistemas de Información · UTN FRC · 3K5 · Grupo 06

Implementación del CU 28 para la Entrega 2, con el **patrón State aplicado a Documentación**.
Incluye el flujo descripto, los **seis flujos alternativos** (A1 a A6) y el «include» del
**CU 29**, que notifica a cada CM de origen la situación de su documentación. Sólo se implementa
este caso de uso.

| Parte | Tecnología |
|---|---|
| Lenguaje y framework | Java 21 · Spring Boot 4.1.1 (igual que el TPI de Backend) |
| Tecnología | Web: API REST + front en React |
| Base de datos y persistencia | H2 en modo archivo · Spring Data JPA / Hibernate |
| Front | React 19 · Vite 7 · Material UI 7 · Axios (misma base que el repo de la cátedra) |

## Estructura

```
ppai-bolsines/
├── back/                 Spring Boot (Maven)
│   └── src/main/java/ppai/bolsines/
│       ├── pantalla/     boundary: PantallaRegistroRecepcionBolsines (@RestController) + ManejadorErrores
│       ├── gestor/       control: GestorRegistroRecepcionBolsines (@Service)
│       ├── modelo/       entidades del dominio + jerarquía del patrón State
│       ├── notificacion/ CU 29: InterfazNotificacionBolsin + implementación que escribe en el log
│       ├── dto/          records que devuelve / recibe la pantalla
│       ├── repositorio/  Spring Data JPA
│       └── config/       beans (reloj del sistema)
│   └── src/test/         pruebas + fixtures aislados (datos-cmc.sql, datos-varios-origenes.sql)
├── front/                React + Vite + MUI (wizard con los pasos del CU)
├── docs/
│   ├── catedra/          ERS, descripción del caso, consignas, modelo de dominio
│   ├── diagramas/        diagramas definitivos del grupo (sin modificaciones) y proyectos de EA
│   ├── sesiones/         registro de cada sesión de trabajo (01 a 06)
│   ├── diferencias_codigo_diagramas.md   todas las diferencias entre el código y los diagramas
│   └── decisiones_para_la_tutora.md      decisiones provisionales a validar
├── AGENTS.md             reglas del proyecto y del modelado
├── CONTEXTO_IA.md        estado del trabajo y cómo seguir
├── COMO_EJECUTAR.txt     paso a paso para levantarlo en una computadora nueva
├── GUIA_DEMOSTRACION.md  guion de la defensa (incluye A5 con su fixture)
├── GUIA_CAMBIOS_DEL_PROYECTO.md  resumen de lo que se agregó
└── PENDIENTE.txt         tareas pendientes
```

## Requisitos

- JDK 21 (con otras versiones Lombok no compila)
- Node.js 22 LTS (Vite 7 pide 20.19 o superior)

## Cómo ejecutar (Windows, PowerShell)

Paso a paso completo y problemas comunes: `COMO_EJECUTAR.txt`. Guion de la demo:
`GUIA_DEMOSTRACION.md`.

```powershell
# 1. Back -> http://localhost:8080 (la base H2 se crea sola en back/data)
cd back
.\mvnw.cmd clean install      # compila y corre las 25 pruebas
.\mvnw.cmd spring-boot:run

# 2. Front (otra terminal) -> http://localhost:3000
cd front
npm ci                        # instala exactamente las versiones de package-lock.json
npm run dev
```

Abrir http://localhost:3000. La usuaria logueada es Laura Gómez, Encargada de Bolsines de la
Comisión Médica Córdoba; aparecen los bolsines con precinto 5001, 5002 y 5005. Al confirmar una
recepción, el correo del CU 29 aparece en la consola del back. A5 (redirigir a un área de la CMC)
se muestra levantando el back con el fixture de la CMC: ver la Parte 2 de `GUIA_DEMOSTRACION.md`.

Cada arranque del back recrea las tablas y carga `data.sql`, así la demo siempre empieza igual.
Para ver las tablas: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/bolsines`,
usuario `sa`, sin contraseña).
