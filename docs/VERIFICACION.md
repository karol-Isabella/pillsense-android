# Verificación de la entrega

Fecha: 6 de octubre de 2026.

- Gradle 8.14.4, JDK 21; objetivo JVM 17 y Android SDK 36.
- `:app:assembleDebug`: compilación satisfactoria.
- `:app:testDebugUnitTest`: 31 pruebas, 0 fallos y 0 errores.
- `:app:lintDebug`: 0 errores; 197 advertencias (principalmente recursos no usados y recomendaciones sobre versiones).
- Sin importaciones entre features distintos.
- `git diff --check`: sin problemas de espacios.

Cobertura de las pruebas: registro y acceso, normalización del correo, contraseñas incorrectas, cuentas duplicadas, salts distintos, cierre/persistencia de sesión, límites de horarios, conservación de minutos, medianoche, duración, ausencia de tomas retroactivas, denominador de adherencia, lectura conservadora de campos de texto y consejos locales.

El reconocimiento óptico real de imágenes, el cifrado nativo SQLCipher/Keystore, la cámara, el ciclo de permisos, las notificaciones en reposo y tras reinicio y la comparación visual en Android no se ejecutaron: `adb devices` no encontró un dispositivo ni emulador disponible. Las pruebas del parser usan texto; no sustituyen una prueba del modelo OCR en un teléfono.

La app no requiere Gemini ni ninguna clave externa. El cambio de proveedor del documento original a OCR y recomendaciones locales sigue la instrucción final del usuario.

APK de entrega: `../PillSense.apk` desde la carpeta del proyecto. Guía y prueba manual: `README.md`.
