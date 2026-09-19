# Changelog

Resumen de la sesión de auditoría y reparación de IAHumanizada. Antes de esto
el proyecto tenía piezas completas (memoria, aprendizaje, personalidades) que
existían en el código pero nunca se conectaban entre sí.

## Memoria y aprendizaje

- **Memoria conectada de punta a punta.** `MemoryManager.store()` nunca se
  llamaba: ahora cada intercambio se guarda vía
  `PersonalityEngine.registrarIntercambio()`.
- **Aprendizaje automático conectado.** `StateDetector` detecta el estado
  emocional, y ahora ese patrón se refuerza vía `MemoryManager.learn()` y se
  promueve al Soul del alma tras 5 repeticiones (antes nunca se llamaba).
- **Feedback explícito (👍/👎).** Cada respuesta de la IA en el chat tiene
  botones de feedback que refuerzan (`reinforce()`) o debilitan
  (`debilitar()`, nuevo) el mismo patrón que el aprendizaje automático usa.
- **Filtro por alma en `getTopReinforced`.** Antes los aprendizajes de una
  alma se colaban en el contexto de otra.
- **Olvido selectivo conectado.** `olvidarDebiles()` existía pero nadie la
  llamaba; ahora corre una vez por sesión desde `ChatViewModel`.
- **`StateDetector` reescrito:** normaliza tildes (antes "reflexión" nunca
  coincidía con su versión sin tilde en el código) y usa límites de palabra
  reales en vez de `substring` (antes "api" disparaba dentro de "capital").

## Almas (personalidades)

- **Migradas de JSON a Room.** Las 6 almas originales se siembran una vez
  desde `res/raw/*.json` y de ahí en adelante viven en la base de datos.
- **Selector de almas en el Altar** (antes no existía ninguna forma de
  cambiar de alma desde la app).
- **Editor simple** (nombre, descripción, vocabulario) + duplicar/crear
  almas personalizadas + eliminar (solo las personalizadas; las 6
  originales no se pueden borrar).

## Robustez y errores

- **Mensajes de error con sentido.** Antes `ApiError` no tenía texto propio
  y el usuario veía literalmente "Error: null".
- **Reintentar tras fallo.** Si el envío falla, aparece un botón
  "↻ Reintentar" que reenvía el mismo mensaje sin reescribirlo.
- **Conexiones HTTP cerradas siempre** (`connection.disconnect()` en
  `finally`); antes se quedaban abiertas indefinidamente.
- **Un fallo al guardar memoria ya no descarta una respuesta que sí llegó
  bien** (se separó la actualización de UI del guardado en memoria).
- **`fallbackToDestructiveMigration`** en `AppDatabase`, para que cambios
  futuros de esquema no crasheen la app al abrir.

## Arquitectura y ciclo de vida

- **`ChatViewModel` vía `ViewModelProvider`** (`ChatViewModelFactory`, nuevo)
  en vez de instanciarse a mano en `onCreate` — sobrevive a rotaciones de
  pantalla sin perder el chat.
- **`pantallaActual` con `rememberSaveable`** por el mismo motivo.
- **Lectura de las almas movida a `Dispatchers.IO`** (antes se hacía en el
  hilo que llamaba, normalmente el principal).

## Configuración y limpieza

- **IPs de Ollama configurables** vía `local.properties`
  (`OLLAMA_URL_PRIMARY`/`OLLAMA_URL_FALLBACK`), no más hardcodeadas.
- **Referencia muerta a Gemini eliminada** de `MainActivity`.
- **`network_security_config.xml` limpiado** (tenía una regla sobre
  `api.anthropic.com`, resto de otra plantilla, que no aplicaba a este
  proyecto).
- **34 archivos `.md` sueltos** (13 vacíos) consolidados en
  `docs/historial/`; `README.md` nuevo y real en la raíz.

## Calidad

- **Tests unitarios** para `StateDetector` (puro) y `MemoryManager`
  (con Robolectric + un `FakeMemoryDao` en memoria).
- **CI en GitHub Actions** (`.github/workflows/android-ci.yml`): compila,
  corre lint y los tests unitarios en cada push/PR a `main`.

## Nota honesta

Este trabajo se hizo sin acceso a un SDK de Android real, así que nada de
esto se compiló de verdad — se revisó línea por línea contra las firmas
reales del código existente. Antes de dar por bueno cualquier cambio, corre
`./gradlew assembleDebug` en Android Studio.
