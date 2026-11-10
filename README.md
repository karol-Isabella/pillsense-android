# PillSense

Aplicación móvil Android que mejora la adherencia a tratamientos farmacológicos mediante recordatorios automáticos, registro de ingesta y análisis personalizado con IA.

## Funcionalidades

### Requisitos Funcionales

- **RF-001**: Autenticación segura con correo y contraseña, datos cifrados en el dispositivo.
- **RF-002**: Gestión de medicamentos: agregar, editar y eliminar tratamientos con horarios configurables.
- **RF-003**: Escaneo de recetas y cajas de medicamentos mediante OCR (ML Kit) integrado en el dispositivo.
- **RF-004**: Recordatorios con notificaciones exactas y alarmas sonoras que funcionan sin conexión a Internet.
- **RF-005**: Registro de ingesta: marcar medicamentos como tomados, pospuestos (15 minutos) u omitidos.
- **RF-006**: Historial de tomas: visualizar registro completo con filtros por estado.
- **RF-007**: Seguimiento de adherencia semanal: porcentaje y gráficos de cumplimiento.
- **RF-008**: Análisis con IA opcional (Gemini): recomendaciones personalizadas basadas en adherencia (estadísticas anónimas).
- **RF-009**: Avisos a contactos de emergencia: SMS automático a contactos registrados si falta confirmar una toma a los 30 minutos.
- **RF-010**: Gestión de stock: notificación cuando quedan pocas unidades de un medicamento.

## Arquitectura

### Capas

**Presentación (UI)**: Composables de Jetpack Compose, ViewModels con StateFlow, gestión de estado reactivo.

**Dominio**: UseCases, modelos de negocio, interfaces de repositorio, lógica agnóstica de la plataforma.

**Datos**: Room para persistencia local, repositorio central, acceso a SharedPreferences cifradas, integración con APIs externas.

### Patrón MVVM + Clean Architecture

```
feature/ → data/ (repos, datasources) + domain/ (usecases) + ui/ (composables, viewmodels)
core/ → database, designsystem, di, network, security, util
navigation/ → rutas, composición de pantallas
```

### Módulos Core

- **database**: Room Database (SQLCipher), Migrations, DAOs.
- **designsystem**: Tema Material 3, colores, tipografía.
- **di**: Inyección de dependencias con Hilt.
- **network**: OkHttp, interceptores.
- **security**: EncryptedSharedPreferences, SessionStore, autenticación.
- **util**: Helpers, Result type, permisos, TimeFormatHelper.

## Stack Tecnológico

- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose, Material Design 3
- **Persistencia**: Room Database, SQLCipher, DataStore, EncryptedSharedPreferences
- **Inyección**: Hilt
- **Async**: Coroutines, Flow
- **Reconocimiento**: ML Kit OCR
- **IA**: Google Generative AI (Gemini)
- **Notificaciones**: WorkManager, AlarmManager, SMS (SmsManager)
- **Testing**: JUnit 4, Mockito
- **Build**: Gradle 8.x, Java 21+

## Estructura de Carpetas

```
app/src/main/
├── java/com/pillsense/app/
│   ├── core/
│   │   ├── database/           # Room, DAOs, entities
│   │   ├── designsystem/       # Tema, colores, tipografía
│   │   ├── di/                 # Módulos Hilt
│   │   ├── network/            # HTTP, interceptores
│   │   ├── security/           # Cifrado, autenticación
│   │   └── util/               # Helpers, TimeFormatHelper, permisos
│   ├── feature/
│   │   ├── adherence/          # Análisis, IA, recomendaciones
│   │   ├── auth/               # Inicio de sesión, registro
│   │   ├── emergency/          # Contactos, perfil, botón de emergencia
│   │   ├── intake/             # Registro de tomas, historial, hoy
│   │   ├── medication/         # Gestión, confirmación, escaneo
│   │   └── reminders/          # Recordatorios, alarmas, SMS
│   ├── navigation/             # NavHost, rutas, AppViewModel
│   └── ui/                     # MainActivity, tema global
├── res/
│   ├── drawable/               # Iconos
│   ├── mipmap/                 # Launchers
│   └── values/
│       ├── strings.xml         # Español
│       └── strings-en/         # Inglés
└── AndroidManifest.xml
```

## Requerimientos No Funcionales

- **RNF-001**: Disponibilidad sin conexión para recordatorios, registro de tomas e historial.
- **RNF-002**: Confidencialidad: datos cifrados en el dispositivo; OCR local, no sale foto del teléfono.
- **RNF-003**: Rendimiento: carga de pantallas <2s, sin lag en listas con 100+ tomas.
- **RNF-004**: Compatibilidad: Android 8.0 (API 26) a Android 14 (API 34).
- **RNF-005**: Seguridad: validación de entrada, prevención de inyecciones, permisos restrictivos.
