# SOLUCIÓN RÁPIDA - Modo Local

## El Problema
```
Tu mensaje → App intenta llamar API Gemini → Algo falla → Cae a fallback local
```

Ves al final de la respuesta: `"(Modo Local)"` ← Esto indica que falló.

## Lo Que Hice

Agregué **logging detallado** en dos archivos críticos para que veas exactamente DÓNDE falla:

### 1️⃣ **ApiService.kt** (Cliente HTTP)
Ahora muestra:
- ✅ Cuándo conecta exitosamente
- ✅ El código HTTP que recibe (200, 401, 429, etc)
- ✅ El error exacto del servidor
- ✅ Qué significa ese error

### 2️⃣ **ChatViewModel.kt** (Lógica de negocio)
Ahora muestra:
- ✅ Por qué falló (tipo específico de error)
- ✅ Qué fallback se usa
- ✅ Mensaje del error en lenguaje claro

## Cómo Verlos

### Opción 1: Android Studio (Recomendado)
```
1. Abre Android Studio
2. View → Tool Windows → Logcat
3. En el filtro: escribe "IAHumanizada"
4. Envía un mensaje en la app
5. Mira la salida en Logcat
```

### Opción 2: Línea de Comandos
```
Double-click: capturar_logs.bat
O en PowerShell:
  adb logcat | findstr "IAHumanizada"
```

## Qué Buscar

Si ves esto:
```
❌ Error API (401): ...
❌ Tipo de error: Autenticación fallida (API Key inválida?)
```

Significa: **Tu API Key está expirada o inválida**
Solución: Ve a https://aistudio.google.com/apikey, genera nueva, actualiza `local.properties`

---

Si ves esto:
```
❌ Error API (429): ...
❌ Tipo de error: Cuota excedida
```

Significa: **Excediste los límites gratis de Google**
Solución: Espera 24 horas o activa pagos en Google Cloud Console

---

Si ves esto:
```
❌ Error en ApiService: IOException - Unable to resolve host
```

Significa: **Sin internet**
Solución: Verifica conexión WiFi/datos

---

Si ves esto:
```
✅ Respuesta exitosa de Gemini API
```

Significa: **¡ESTÁ FUNCIONANDO!** (sin modo local)

## Los 3 Archivos Nuevos

| Archivo | Para Qué |
|---------|----------|
| `README_DEBUGGING.md` | 👈 LEEME PRIMERO (este archivo) |
| `DIAGNOSTICO_API.md` | Guía detallada de troubleshooting |
| `capturar_logs.bat` | Script para capturar logs fácil |

## Próximo Paso

1. **Recompila** la app (Build → Make Project)
2. **Ejecuta** en emulador/dispositivo
3. **Envía un mensaje**
4. **Lee los logs**
5. **Usa DIAGNOSTICO_API.md** para encontrar la solución

---

**¡Con estos logs, cualquier problema se resuelve en minutos!** 🎯

