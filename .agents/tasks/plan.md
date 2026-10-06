# Plan de Implementación - Reestructuración Arquitectónica de PillSense

## Resumen Ejecutivo

Reestructuración completa del proyecto Android PillSense siguiendo Clean Architecture y Feature-based Modularization. El proyecto ya tiene la mayoría de la estructura en su lugar, pero requiere limpieza del sistema de diseño, integración de Gemini API, gestión de permisos y eliminación de dependencias no usadas.

---

## FASE 1: LIMPIEZA DEL SISTEMA DE DISEÑO

### 1. Eliminar duplicación en ui/theme vs core/designsystem

**Decisión arquitectónica**: Los archivos de tema (Theme.kt, Color.kt, Type.kt) existen en AMBOS lugares. Como `core/designsystem/` es la fuente canónica según la estructura del proyecto y todos los imports ya apuntan allá, se elimina `app/src/main/java/com/pillsense/app/ui/theme/` completamente.

**Verificación previa realizada**: 
- No hay imports desde `ui.theme` en el codebase
- Todos los screens importan desde `core.designsystem`
- Las carpetas contienen: Theme.kt, Color.kt, Type.kt (duplicadas)

- [ ] 1. Eliminar la carpeta `app/src/main/java/com/pillsense/app/ui/theme/` completa con sus archivos Theme.kt, Color.kt, Type.kt
      Files: (eliminar) `app/src/main/java/com/pillsense/app/ui/theme/`
      Verify: Confirmar que la carpeta no existe con: `Test-Path 'c:\5_semestre\trabajos-finales\Pillsense\app\src\main\java\com\pillsense\app\ui\theme\'` (debe retornar False)

- [ ] 2. Eliminar la carpeta raíz `app/src/main/java/com/pillsense/app/ui/` (ahora vacía, solo tenía theme)
      Files: (eliminar) `app/src/main/java/com/pillsense/app/ui/`
      Verify: `Test-Path 'c:\5_semestre\trabajos-finales\Pillsense\app\src\main\java\com\pillsense\app\ui\'` (debe retornar False)

---

## FASE 2: INTEGRACIÓN GEMINI API

**Decisión arquitectónica**: Se crea un nuevo módulo `core/ai/` para encapsular la lógica de análisis de imágenes con Gemini API. El módulo proporcionará una interfaz limpia que permite análisis real con API key o mock cuando no esté disponible. Soporte para configuración por variable de entorno (BuildConfig).

### 2.1 Crear estructura y tipos

- [ ] 3. Crear package `core/ai/` y archivo `MedicationExtractResult.kt` con data class para resultados de análisis
      Contenido: data class con campos nombre, dosis, frecuencia, indicaciones (todos String)
      Files: `app/src/main/java/com/pillsense/app/core/ai/MedicationExtractResult.kt`
      Verify: Compilar con `.\gradlew.bat assembleDebug 2>&1 | Select-String "^e: "` (0 errores en este archivo)

- [ ] 4. Crear archivo `GeminiImageAnalyzer.kt` con interfaz y implementación
      Contenido: 
      - Interface `ImageAnalyzer` con `suspend fun analyzeImage(imageBitmapBytes: ByteArray): Result<MedicationExtractResult>`
      - Implementación `GeminiImageAnalyzer` que usa GenerativeAI SDK (con mock cuando API key está vacía)
      - Soportar configuración desde BuildConfig.GEMINI_API_KEY
      - Manejo de errores con try-catch y logging en Timber
      Files: `app/src/main/java/com/pillsense/app/core/ai/GeminiImageAnalyzer.kt`
      Verify: Compilar y revisar que no hay errores de imports

### 2.2 Agregar dependencia Gemini SDK

**Decisión**: Usar `com.google.ai.client.generativeai` versión más reciente (consultar disponibilidad). Se añade a `libs.versions.toml` con versión fija.

- [ ] 5. Actualizar `gradle/libs.versions.toml` para agregar dependencia de Gemini SDK
      Agregar: `generative-ai = "x.y.z"` (versión estable más reciente)
      En dependencies: `generativeai = { group = "com.google.ai.client", name = "generativeai", version.ref = "generative-ai" }`
      Files: `gradle/libs.versions.toml`
      Verify: El alias `libs.generativeai` está disponible (verificar en IDE o compilación)

- [ ] 6. Actualizar `app/build.gradle.kts` para incluir dependencia de Gemini
      Agregar línea: `implementation(libs.generativeai)`
      Files: `app/build.gradle.kts`
      Verify: Compilar con `.\gradlew.bat assembleDebug` (debe resolver dependencia)

### 2.3 Configurar API key en BuildConfig

- [ ] 7. Actualizar `local.properties` para soportar variable de entorno GEMINI_API_KEY
      Agregar: `GEMINI_API_KEY=your_api_key_here` (comentado con instrucción)
      Files: `local.properties` (y crear `local.defaults.properties` si no existe con valor vacío)
      Verify: Verificar que `local.properties` existe y contiene el placeholder

- [ ] 8. Actualizar `app/build.gradle.kts` para pasar GEMINI_API_KEY a BuildConfig
      En bloque `defaultConfig`: Agregar `buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY") as? String ?: ""}\"")` 
      Files: `app/build.gradle.kts`
      Verify: Compilar y verificar que BuildConfig.GEMINI_API_KEY está disponible

### 2.4 Integración en DI (Hilt)

- [ ] 9. Crear `AiModule.kt` en `core/di/` para provisionar GeminiImageAnalyzer como singleton
      Contenido: @Module, @InstallIn, @Provides @Singleton fun provideImageAnalyzer(): ImageAnalyzer = GeminiImageAnalyzer()
      Files: `app/src/main/java/com/pillsense/app/core/di/AiModule.kt`
      Verify: Compilar y verificar que no hay errores de Hilt

---

## FASE 3: GESTIÓN DE PERMISOS

**Decisión arquitectónica**: Crear utilidad centralizada para gestión de permisos en tiempo de ejecución. Soportará SCHEDULE_EXACT_ALARM, POST_NOTIFICATIONS, REQUEST_IGNORE_BATTERY_OPTIMIZATIONS. Usar ActivityResultLauncher pattern (moderno) en lugar de requestPermissions callback.

### 3.1 Crear estructura de permisos

- [ ] 10. Crear package `core/util/permissions/` y archivo `PermissionManager.kt`
       Contenido:
       - Enum con permisos: SCHEDULE_EXACT_ALARM, POST_NOTIFICATIONS, BATTERY_OPTIMIZATION
       - Interface `PermissionHandler` para solicitar permisos (recibe lista de permisos)
       - Clase `PermissionManager(context: Context)` con método `checkPermissions(permissions: List<Permission>): Map<Permission, Boolean>`
       - Método para solicitar permisos (prepara intents para settings si son permisos especiales)
       Files: `app/src/main/java/com/pillsense/app/core/util/permissions/PermissionManager.kt`
       Verify: Compilar y verificar sintaxis

### 3.2 Actualizar AndroidManifest.xml

- [ ] 11. Agregar permisos faltantes a AndroidManifest.xml
        Agregar: `<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />`
        Ya existen: CAMERA, POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM, RECEIVE_BOOT_COMPLETED, INTERNET, ACCESS_NETWORK_STATE
        Files: `app/src/main/AndroidManifest.xml`
        Verify: Verificar que todos los permisos están presentes en manifest

---

## FASE 4: LIMPIEZA DE DEPENDENCIAS

**Decisión**: ML Kit Barcode está en build.gradle.kts pero no se usa en el código (el proyecto usa Gemini ahora). Se elimina.

- [ ] 12. Remover ML Kit Barcode de `app/build.gradle.kts`
        Línea actual: `implementation(libs.mlkit.text.recognition)`
        Acción: Eliminar esta línea
        Files: `app/build.gradle.kts`
        Verify: Compilar con `.\gradlew.bat assembleDebug` (debe compilar sin la dependencia)

- [ ] 13. Remover ML Kit de `gradle/libs.versions.toml` si existe
        Buscar: `mlkit.*` o `text-recognition`
        Acción: Eliminar referencias
        Files: `gradle/libs.versions.toml`
        Verify: Grep para confirmar que no hay más referencias a mlkit: `Select-String -Path "gradle/libs.versions.toml" -Pattern "mlkit"`

---

## FASE 5: VERIFICACIÓN DE ESTRUCTURA FEATURE-BASED

**Verificación**: Confirmar que la estructura ya está correcta. No hay cambios requeridos, solo validación.

- [ ] 14. Validar que la estructura feature-based está correcta
        Verificar:
        - feature/medication/ui/scan/ScanScreen.kt ✓ (debe usar GeminiImageAnalyzer)
        - feature/medication/ui/confirm/ConfirmScreen.kt ✓
        - feature/medication/ui/list/ (estructura existe, archivos vacíos)
        - feature/intake/ui/TodayScreen.kt ✓
        - feature/auth/ui/LoginScreen.kt ✓
        - feature/adherence/ui/ (vacío, listo para TodayScreen si no está en intake)
        - feature/reminders/ (estructura con data/domain/ui/scheduler/receivers vacíos)
        Files: (verificación, no cambios)
        Verify: Listado de directorios confirma la estructura: `Get-ChildItem -Path "c:\5_semestre\trabajos-finales\Pillsense\app\src\main\java\com\pillsense\app\feature" -Recurse -Directory`

- [ ] 15. Limpiar carpetas vacías con solo .gitkeep cuando no sean necesarias
        Revisar:
        - feature/medication/ui/list/.gitkeep (puede eliminarse si no hay archivos)
        - feature/adherence/ui/.gitkeep, data/.gitkeep, domain/.gitkeep
        - feature/reminders/ui/.gitkeep, scheduler/.gitkeep, receivers/.gitkeep (mantener estructura, no eliminar)
        Acción: Eliminar .gitkeep solo de carpetas que NO tendrán contenido en el futuro (medication/list solo tiene .gitkeep)
        Files: (eliminar) `app/src/main/java/com/pillsense/app/feature/medication/ui/list/.gitkeep`
        Verify: Estructura sigue siendo válida, compilación OK

---

## FASE 6: COMPILACIÓN Y VERIFICACIÓN FINAL

- [ ] 16. Compilación final con verificación de errores
        Ejecutar: `.\gradlew.bat assembleDebug 2>&1 | Select-String "^e: "` 
        Verificar: 0 errores (solo warnings aceptables)
        Files: (verificación de build)
        Verify: Pegar últimas 15 líneas textuales del output

---

## Resumen de Cambios Estructurales

### Eliminaciones
- `app/src/main/java/com/pillsense/app/ui/theme/` (completa)
- `app/src/main/java/com/pillsense/app/ui/` (raíz)
- ML Kit Barcode de dependencias

### Creaciones
- `core/ai/` con GeminiImageAnalyzer y tipos relacionados
- `core/util/permissions/` con PermissionManager
- `core/di/AiModule.kt`

### Actualizaciones
- `app/build.gradle.kts` (agregar Gemini SDK, eliminar ML Kit, agregar BuildConfig)
- `gradle/libs.versions.toml` (agregar Gemini SDK)
- `AndroidManifest.xml` (agregar REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
- `local.properties` (agregar GEMINI_API_KEY)

### Mantiene sin cambios
- Estructura feature-based (ya correcta)
- Importes en screens (ya correctos)
- core/designsystem/ (canónica)
- Resto de arquitectura MVVM+DI+Room+Flow

---

## Notas Importantes

1. **Gemini API Key**: El equipo debe obtener una clave de Gemini AI y colocarla en `local.properties` para que funcione el análisis. Sin ella, el mock retornará datos ficticios.

2. **Permisos runtime**: La app necesita solicitar permisos en runtime para Android 12+. El PermissionManager proporciona utilidades, pero cada feature debe integrarla en su UI (ej: ScanScreen debe solicitar CAMERA).

3. **Sin cambios en textos ni estructura lógica**: La refactorización es estrictamente arquitectónica. Los screens, lógica de negocio y contenido permanecen iguales.

4. **Verificación post-refactor**: Después de cada fase, compilar con `.\gradlew.bat assembleDebug` para asegurar que no hay imports rotos ni problemas de estructura.

5. **Orden de ejecución**: Las fases deben ejecutarse en el orden listado (1→6) porque:
   - Fase 1 limpia duplicación antes de agregar nuevas cosas
   - Fase 2-3 agregan nuevos módulos (sin dependencias cruzadas)
   - Fase 4 limpia lo viejo
   - Fase 5-6 verifica

---

## Comandos de Referencia

```powershell
# Compilar y ver errores
.\gradlew.bat assembleDebug 2>&1 | Select-String "^e: "

# Compilación completa con salida
.\gradlew.bat assembleDebug

# Verificar estructura
Get-ChildItem -Path "c:\5_semestre\trabajos-finales\Pillsense\app\src\main\java\com\pillsense\app\core" -Recurse -Directory

# Buscar imports rotos (ejemplo)
Select-String -Path "**/*.kt" -Pattern "import.*ui\.theme"
```
