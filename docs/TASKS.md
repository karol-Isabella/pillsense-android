# PillSense - Tareas Completadas

## Fase 1: Autenticación y Validación ✓

- Autenticación segura (email/password, hashing PBKDF2)
- Validación detallada de inputs
- Lockout de 30 segundos tras 5 intentos fallidos
- 52+ unit tests pasando

**Última actualización**: `6ee62e0`

## Fase 2: Internacionalización (i18n) ✓

- Strings en español (es) e inglés (en) en res/values/
- Selector de idioma en Perfil con live switch
- DateLocaleHelper para formato de fechas por idioma
- TimeFormatHelper para formato 12h (a. m./p. m. en español, AM/PM en inglés)

**Última actualización**: `2d9b4dd` (restore ai label from cache)

## Fase 3: Gestión de Medicamentos ✓

- Agregar, editar, eliminar medicamentos
- Formulario de confirmación con placeholders claros
- Selector de forma (pastilla/cápsula, jarabe, gotas, otro)
- Unidades por toma según forma
- Stock con notificación (≤5 unidades, una sola vez por caja)
- OCR con ML Kit (foto no sale del dispositivo)
- Validación de datos con TimeFormatHelper

**Última actualización**: `39a5782` (medication form placeholders and low stock alert)

## Fase 4: Recordatorios y Tomas ✓

- Programación de recordatorios con AlarmManager y WorkManager
- Notificaciones exactas (si hay permiso)
- Registro de ingesta: tomada, pospuesta (15 min), omitida
- Historial completo con filtros
- Stock se decrementa al marcar como tomada

**Estado**: Implementado en ReminderEngine, funcionando sin conexión

## Fase 5: Adherencia e IA ✓

- Cálculo de adherencia semanal (%)
- Análisis con IA (Gemini): tres niveles (welcome, inicial, completo)
- Consentimiento e interruptor en Perfil
- Caché de respuestas IA (1 hora)
- Validación de respuestas Gemini (no permite mención de fármacos, dosis)
- Respaldo local ("Reglas locales") si Gemini no responde o rechaza

**Última actualización**: `e1afe2d` (read sms toggle from same storage)

## Fase 6: Contactos de Emergencia ✓

- Agregar/eliminar contactos con validación de teléfono
- Máximo 2 contactos
- SMS automático a los 30 minutos si no se confirma toma
- Interruptor con diálogo de consentimiento
- Lista de contactos visible siempre

**Última actualización**: `8bda39d` (sms toggle handler and always show contacts)

## Base de Datos ✓

- Room Database (v4) con SQLCipher
- Entidades: Medication, Intake, EmergencyContact, AiCache
- Migraciones: v1→v2→v3→v4
- DAOs con queries optimizadas

**Última actualización**: `39a5782` (migrations 1-4 registradas)

## Tests ✓

- Unit tests: 117+ pasando
- TimeFormatHelperTest: 8 tests (12h format español/inglés)
- Validación de formatos de entrada
- Sin fallos

## Compilación ✓

```bash
./gradlew assembleDebug  # SUCCESS in ~10-30s
./gradlew testDebugUnitTest  # 117+ tests passing
```

## Pendiente / No Implementado

- [ ] Integración con servidor en la nube para SMS (actualmente usa SmsManager del dispositivo)
- [ ] Push notifications desde servidor (actualmente solo local WorkManager)
- [ ] Sincronización en la nube (datos siempre locales cifrados)
- [ ] Deep links y share
- [ ] Tests de integración (actualmente solo unit tests)
- [ ] Documentación API Swagger

## Notas

- Código en inglés, strings en español/inglés, comentarios en español
- Stack: Kotlin, Compose, Room, Hilt, Coroutines, ML Kit OCR, Gemini IA
- Arquitectura: MVVM + Clean (core/feature/navigation)
- Persistencia: SQLCipher + EncryptedSharedPreferences
- Permisos: POST_NOTIFICATIONS, SEND_SMS, SCHEDULE_EXACT_ALARM, CAMERA, READ_EXTERNAL_STORAGE
