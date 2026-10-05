# Pillsense - Technical Stack

## Stack Tecnológico

### Frontend (Android)
- **Lenguaje**: Kotlin
- **Framework UI**: Jetpack Compose (UI moderna declarativa)
- **Versión mínima**: Android 8.0 (API 26)
- **Versión objetivo**: Android 14 (API 34)
- **Gradle**: 8.x

### Arquitectura
- **Patrón**: MVVM (Model-View-ViewModel)
- **Inyección de Dependencias**: Hilt
- **Persistencia**: Room Database
- **Async**: Coroutines + Flow
- **Testing**: JUnit 4, Compose UI Testing, Mockito

### Dependencias Principales
- **UI**: Jetpack Compose, Material Design 3
- **Persistencia**: Room, DataStore
- **Networking**: Retrofit, OkHttp (cuando aplique)
- **Logging**: Timber
- **QR/Barcode**: ML Kit (Google)
- **Notificaciones**: WorkManager para recordatorios
- **Seguridad**: EncryptedSharedPreferences

### Configuración de Build
- **Java**: 21+ (usa toolchain via gradle.properties)
- **AGP**: Última versión compatible
- **ProGuard**: Habilitado para release

## Principios de Implementación

1. **Sin mocks en producción**: Toda integración real configurable por variable de entorno.
2. **Error handling**: Try-catch explícito, logs en Timber.
3. **Seguridad**: Encriptación de datos sensibles, validación de entradas.
4. **Testing**: Tests unitarios para lógica; UI tests para flujos críticos.
5. **Performance**: Lazy loading, paginación en listas largas.

## Reglas Permanentes

- Al terminar cualquier tarea, márcala como completada en `docs/TASKS.md`.
- No inventes datos: integración real y configurable por entorno.
- Usa versiones estables fijadas en `build.gradle.kts`.
- Kumandos de git: NO. El usuario maneja control de versiones.
