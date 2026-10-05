# Pillsense - Project Structure

## Estructura de Carpetas

```
app/src/main/
├── java/com/pillsense/app/
│   ├── core/                          # Módulo núcleo compartido
│   │   ├── database/                  # Room DB, DAOs, entidades
│   │   ├── designsystem/              # Temas, colores, tipografía
│   │   ├── di/                        # Módulos Hilt (inyección)
│   │   ├── network/                   # Retrofit, interceptores
│   │   ├── security/                  # Encriptación, autenticación
│   │   └── util/                      # Utilidades genéricas (Result, etc)
│   ├── feature/                       # Módulos de características
│   │   ├── adherence/                 # Seguimiento de adherencia
│   │   │   ├── data/                  # Repos, DataSources
│   │   │   ├── domain/                # UseCases, modelos
│   │   │   └── ui/                    # Composables, ViewModels
│   │   ├── auth/                      # Autenticación
│   │   │   ├── data/
│   │   │   ├── domain/
│   │   │   └── ui/
│   │   ├── emergency/                 # Botón de emergencia
│   │   │   ├── data/
│   │   │   ├── domain/
│   │   │   └── ui/
│   │   ├── intake/                    # Registro de ingesta
│   │   │   ├── data/
│   │   │   ├── domain/
│   │   │   └── ui/
│   │   ├── medication/                # Gestión de medicamentos
│   │   │   ├── data/
│   │   │   ├── domain/
│   │   │   ├── ui/
│   │   │   │   ├── confirm/           # Confirmación de ingesta
│   │   │   │   ├── list/              # Listado de medicamentos
│   │   │   │   └── scan/              # Escaneo de código de barras
│   │   └── reminders/                 # Recordatorios y notificaciones
│   │       ├── data/
│   │       ├── domain/
│   │       ├── receivers/             # BroadcastReceivers
│   │       ├── scheduler/             # WorkManager tasks
│   │       └── ui/
│   ├── navigation/                    # NavHost, rutas, NavGraphs
│   └── ui/                            # Raíz MainActivity
│       └── theme/                     # Tema global
└── res/
    ├── drawable/                      # Iconos, drawables
    ├── mipmap/                        # Launchers
    └── values/
        └── strings.xml                # Strings (español)
```

## Patrones de Código

### MVVM + Clean Architecture
- **Presentation**: Composables + ViewModels (Hilt)
- **Domain**: UseCases, modelos, interfaces de repos
- **Data**: Implementación de repos, DataSources, DB, API

### Inyección de Dependencias
- Módulos Hilt en `core/di/` para cada capa.
- Singletons para DB, API, SharedPreferences.
- Factory injection para UseCases.

### Persistencia
- Room para datos locales (medicamentos, ingesta, recordatorios).
- DataStore para preferencias encriptadas.
- EncryptedSharedPreferences para tokens/credenciales.

### Async
- Coroutines + Flow para operaciones I/O.
- StateFlow para UI state en ViewModels.
- WorkManager para recordatorios persistentes.

### Testing
- Unit tests en `app/src/test/` para lógica.
- Instrumented tests en `app/src/androidTest/` para UI y DB.

## Reglas de Estructura

1. No dejes carpetas vacías: usa `.gitkeep`.
2. Cada feature es independiente: sus repos no acceden a otros features.
3. Datos compartidos: solo desde `core/`.
4. UI: Composables sin lógica; lógica en ViewModels.
5. Nombres: inglés (código, archivos); español (strings, comentarios).
