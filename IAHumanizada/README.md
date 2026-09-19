# Ducky (antes IAHumanizada)

App de Android (Kotlin + Jetpack Compose) que simula una IA conversacional con
"almas" (personalidades) intercambiables, un detector de estado emocional del
usuario y un sistema de memoria que aprende con el tiempo.

## Arquitectura

- **`engine/PersonalityEngine`** — arma el prompt final combinando el alma
  activa, la afinación del usuario (sliders) y el estado detectado.
- **`engine/StateDetector`** — heurística basada en palabras clave, pausas y
  longitud del mensaje para inferir el estado emocional (cálido, técnico,
  juguetón, silencioso, reflexivo, neutro).
- **`engine/MemoryManager`** — memoria declarativa (Room) + memoria de
  aprendizaje que se "promueve" al Soul del alma tras 5 refuerzos.
- **`data/repository/AlmaRepository`** — carga las 6 almas desde
  `res/raw/alma_*.json`.
- **`data/repository/ApiService`** — llama a un servidor Ollama local (por
  defecto vía Tailscale). **Requiere que tú tengas Ollama corriendo** en tu
  propia red — ver `docs/historial/` para el historial de esa migración.

## Cómo correrlo

1. Levanta un servidor Ollama accesible desde el móvil (local o vía Tailscale).
2. Ajusta las IPs en `ApiService.kt` si no usas las que están hardcodeadas.
3. `./gradlew assembleDebug`

## Estado conocido / pendientes

- El sistema de "aprendizaje reforzado" (`MemoryManager.learn/reinforce`)
  existe pero todavía no está conectado a ningún flujo de la UI — solo la
  memoria declarativa (`store`) se alimenta automáticamente en cada mensaje.
- Las IPs de Ollama están hardcodeadas; sería mejor moverlas a
  `local.properties` como ya se hace con la API key de Gemini (legado, sin uso
  actual).

## Historial de cambios

Las notas de sesiones de desarrollo anteriores (migraciones de modelo, debug
de logs, etc.) están archivadas en `docs/historial/` para no ensuciar la raíz.
