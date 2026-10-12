# PillSense

Aplicación Android para mejorar la adherencia a tratamientos farmacológicos mediante recordatorios inteligentes, registro de ingesta y análisis con IA.

## Configuración

### Clave de API de Gemini

1. Abre o crea `local.properties` en la raíz del proyecto (ignorado por git):

```properties
GEMINI_API_KEY=tu_clave_aquí
GEMINI_MODEL=gemini-3.8-flash
```

2. La clave se incluye en el APK durante la compilación a través de `buildConfigField`. No aparece en logs ni en README.

3. Por defecto, el modelo es `gemini-3.8-flash` si no se especifica.

## Caracteristicas

- Registro local de medicamentos con escaneo de código de barras (ML Kit, sin conexión).
- Recordatorios configurables con notificaciones exactas (API 31+).
- Seguimiento de adherencia semanal y mensual.
- Análisis con IA (Gemini) de patrones de olvido (opcional, requiere consentimiento).
- Datos cifrados localmente con SQLCipher.
- Contactos de emergencia con acceso rápido.
- Botón de emergencia para llamadas directas.
- Modo oscuro.

## Limitaciones Actuales

- La clave de API de Gemini viaja dentro del APK. Esto es una limitación de seguridad. En producción, se recomienda usar Firebase AI Logic con App Check o un servidor intermedio para gestionar las llamadas a Gemini de forma más segura.
- El análisis con IA requiere internet. Sin conexión, se usan recomendaciones locales basadas en reglas.
- Los recordatorios locales pueden retrasarse si faltan permisos o si el sistema operativo ha suspendido la app.

## Mejoras Futuras

- Servidor intermedio con autenticación para gestionar llamadas a Gemini de forma segura.
- Sincronización con servidores de farmacias para información de medicamentos.
- Integración con sistemas de salud electrónicos.
- Exportación de reportes para profesionales sanitarios.
- Soporte para múltiples idiomas.

## Construcción

```bash
./gradlew assembleDebug      # Compilar debug
./gradlew assembleRelease    # Compilar release
./gradlew testDebugUnitTest  # Ejecutar unit tests
```

## Privacidad

- Todos los datos se almacenan localmente en tu teléfono.
- El análisis con IA envía solo estadísticas anónimas (sin nombres, correos, dosis exactas ni fechas).
- No se registran datos de usuarios en logs.
- El consentimiento es explícito y configurable desde el perfil.

## Desarrollo

Requisitos:
- Android Studio Koala o posterior
- Java 17+
- API mínima: Android 8.0 (API 26)
- API objetivo: Android 14 (API 34)

Arquitectura:
- MVVM + Clean Architecture
- Hilt para inyección de dependencias
- Room con SQLCipher para persistencia
- Jetpack Compose para UI
- Coroutines + Flow para async
- Retrofit + OkHttp para HTTP (Gemini API)

## Licencia

Proyecto académico. Consulta con tu instructor o mentor antes de usar en producción.
