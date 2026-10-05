**1. Aplicación Móvil: PillSense**

**Concepto**

PillSense es una aplicación móvil orientada a mejorar la adherencia a los tratamientos médicos. Permite registrar medicamentos mediante el escaneo de recetas o cajas utilizando inteligencia artificial para extraer la información automáticamente. Posteriormente programa recordatorios, registra las tomas realizadas y ofrece recomendaciones personalizadas basadas en los hábitos del usuario.

Plataforma: Android

Tecnologías sugeridas: Kotlin, Jetpack Compose, Room, ML Kit OCR, Gemini API.

**Requerimientos Funcionales**

- RF-001: El sistema debe permitir al usuario registrarse e iniciar sesión mediante correo electrónico y contraseña.

- RF-002: El sistema debe permitir capturar una fotografía de una receta médica o de la caja del medicamento.

- RF-003: La inteligencia artificial debe extraer automáticamente el nombre del medicamento, la dosis y la frecuencia mediante OCR.

- RF-004: El usuario debe poder editar y confirmar la información antes de guardarla.

- RF-005: El sistema debe programar recordatorios según los horarios establecidos.

- RF-006: La notificación debe permitir marcar la dosis como Tomada, Pospuesta u Omitida.

- RF-007: El sistema debe registrar el historial de todas las tomas realizadas.

- RF-008: La inteligencia artificial debe calcular semanalmente el porcentaje de adherencia al tratamiento.

- RF-009: La inteligencia artificial debe generar recomendaciones cuando detecte olvidos frecuentes.

- RF-010: El usuario debe poder gestionar contactos de emergencia.

**Requerimientos No Funcionales**

- RNF-001: Las alarmas deben funcionar sin conexión a internet.

- RNF-002: Los recordatorios deben ejecutarse aunque la aplicación esté cerrada.

- RNF-003: Los datos del usuario deben almacenarse localmente de forma segura.

- RNF-004: Los botones principales deben cumplir criterios mínimos de accesibilidad táctil (48 × 48 píxeles).

- RNF-005: La interfaz debe responder en menos de un segundo durante la navegación normal.

