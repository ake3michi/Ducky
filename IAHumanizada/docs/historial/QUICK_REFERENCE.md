# 🎯 QUICK REFERENCE - Tarjeta de Referencia Rápida

## 🚨 PROBLEMA
```
Respuesta de IA termina con "(Modo Local)"
↓
Significa que la API falló y usó fallback local
```

## ✅ SOLUCIÓN EN 3 PASOS

### PASO 1: VER LOGS (30 segundos)
```
Opción A: Android Studio
  View → Tool Windows → Logcat
  Filtro: "IAHumanizada"

Opción B: Windows (Fácil)
  Doble-clic: capturar_logs.bat
```

### PASO 2: BUSCAR ERROR (10 segundos)
```
Busca esta línea en los logs:
  ❌ Error API (XXX): ...
  ❌ Tipo de error: ...

Toma nota del código (XXX) y el tipo de error
```

### PASO 3: SOLUCIONAR (1-5 minutos)
```
Código 401/403 → API Key inválida
  Solución: Genera nueva en https://aistudio.google.com/apikey

Código 429 → Cuota excedida
  Solución: Espera 24h o habilita pagos

Código 400 → Filtro de seguridad
  Solución: Revisa PersonalityEngine.kt

Connection refused → Sin internet
  Solución: Verifica WiFi/datos

No hay error (200) → ¡Funciona!
  Solución: No hay nada que hacer ✅
```

---

## 📊 CÓDIGOS HTTP COMUNES

| Código | Significado | Acción |
|--------|------------|--------|
| 200 | OK | ✅ Todo bien |
| 400 | Bad Request | ⚠️ Filtro seguridad |
| 401/403 | Unauthorized | 🔑 API Key inválida |
| 429 | Too Many | ⏳ Cuota excedida |
| 500+ | Server Error | 🔴 Google caído |

---

## 🔍 LOGS ESPERADOS (CORRECTO)

```
🔵 Iniciando llamada...
🔵 API Key presente: true
🔵 Conexión establecida...
🔵 API Response Code: 200
✅ Respuesta OK recibida...
✅ Respuesta exitosa de Gemini API
```

**Resultado**: Sin "(Modo Local)" ✅

---

## 🔍 LOGS ESPERADOS (INCORRECTO)

```
🔵 Iniciando llamada...
🔵 API Key presente: true
🔵 Conexión establecida...
🔵 API Response Code: 401
❌ Error API (401): Invalid API Key
❌ Tipo de error: Autenticación fallida (API Key inválida?)
⚠️ Error de API, usando Fallback local
```

**Resultado**: Con "(Modo Local)" ❌

---

## 📚 DOCUMENTOS

| Doc | Para Qué | Duración |
|-----|----------|----------|
| INDEX.md | Navegar | 2 min |
| PASO_A_PASO.md | Instrucciones | 5 min |
| DIAGNOSTICO_API.md | Soluciones | 3-5 min |
| Este archivo | Referencia rápida | 1 min |

---

## 🚀 CHECKLIST

- [ ] Recompilé la app
- [ ] Abierto LogCat (o capturar_logs.bat)
- [ ] Envié un mensaje
- [ ] Vi los logs
- [ ] Busqué "❌ Error API"
- [ ] Identifiqué el código de error
- [ ] Apliqué la solución
- [ ] Probé de nuevo
- [ ] ¡Funciona! ✅

---

## 💾 ARCHIVOS MODIFICADOS

```
app/src/main/java/.../
  ✏️ data/repository/ApiService.kt
  ✏️ ui/chat/ChatViewModel.kt
```

---

## 🎯 SI ALGO SIGUE FALLANDO

1. Ve a **DIAGNOSTICO_API.md** (guía completa)
2. Ve a **README_DEBUGGING.md** (resumen)
3. Ejecuta `adb logcat -c` para limpiar y reintentar
4. Verifica con `adb devices` que el dispositivo esté conectado

---

## ⏱️ TIMELINE

```
Recompilación: 1-2 min
Ver logs:      30 seg
ID error:      10 seg
Solucionar:    1-5 min
─────────────────────────
TOTAL:         ~10 min
```

---

## 📌 PIN ESTO 📌

**Cuando veas "(Modo Local)":**
1. `PASO_A_PASO.md` → 5 minutos
2. Busca "❌ Error API" en logs
3. Ve a `DIAGNOSTICO_API.md` con tu código de error
4. Soluciona → Hecho ✅

---

## 🔗 LINKS

- Generar API Key: https://aistudio.google.com/apikey
- Gemini Docs: https://ai.google.dev/docs
- LogCat Help: https://developer.android.com/studio/debug/logcat

---

**IMPRIME ESTO O GUARDA EN FAV** 📌

Es tu guía rápida cada vez que algo falle.

---

Versión: 1.0
Fecha: 2026-04-20
Estado: ✅ Listo

