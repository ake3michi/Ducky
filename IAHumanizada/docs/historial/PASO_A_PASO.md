# PASO A PASO - Solucionar "Modo Local"

## 🎯 Tu situación
Está viendo mensajes que terminan en `"(Modo Local)"` → Significa que la API falló y cayó al fallback.

## ✅ Lo que hice

Agregué **logging ultra detallado** para que veas exactamente dónde falla. Ahora los logs te dirán:
- ✅ Si la API Key es válida
- ✅ Si hay conexión a internet
- ✅ El código HTTP exacto que recibe
- ✅ Por qué falló específicamente

## 🚀 PASO 1: Recompila

```
En Android Studio:
1. Build → Clean Project
2. Build → Build Bundle(s) / APK(s)
   O simplemente Run el proyecto
```

## 🔍 PASO 2: Captura Logs

### Opción A (Recomendado - Android Studio)
```
1. Abre Android Studio
2. View → Tool Windows → Logcat
3. En el filtro (abajo a la izquierda): escribe "IAHumanizada"
4. Presiona Enter
```

### Opción B (Windows - Fácil)
```
1. En el folder del proyecto, dobla-clic en: capturar_logs.bat
2. Se abrirá una ventana de terminal
3. Te mostrará los logs en tiempo real
```

### Opción C (PowerShell - Avanzado)
```powershell
$env:ANDROID_SDK = "C:\Users\Sobet\AppData\Local\Android\Sdk"
& "$env:ANDROID_SDK\platform-tools\adb.exe" logcat | Select-String "IAHumanizada"
```

## 💬 PASO 3: Envía un Mensaje

1. Abre la app en el emulador/dispositivo
2. Escribe cualquier mensaje en el chat
3. Presiona enviar
4. **Mira los logs** - verás una cascada de logs azules/verdes/rojos

## 📋 PASO 4: Lee el Error

### Busca esta línea en los logs:
```
❌ Error API (XXX): ...
❌ Tipo de error: ...
```

### El número XXX te dice el problema:

| Código | Significado | Solución |
|--------|-------------|----------|
| `401` o `403` | API Key inválida | Genera nueva en https://aistudio.google.com/apikey |
| `429` | Cuota excedida | Espera 24 horas o habilita pagos |
| `400` | Filtro de seguridad | Revisa el prompt (PersonalityEngine.kt) |
| `500-599` | Servidor caído | Espera a que Google lo repare |
| `Connection refused` | Sin internet | Verifica WiFi/datos |

## 🔧 PASO 5: Soluciona

Según el código de error, ve a **DIAGNOSTICO_API.md** y sigue la solución específica.

---

## 📚 Documentos Útiles

| Nombre | Para Qué |
|--------|----------|
| **QUICKSTART_LOGS.md** | Explicación rápida |
| **README_DEBUGGING.md** | Resumen de cambios |
| **DIAGNOSTICO_API.md** | ← **LEEME DESPUÉS DE VER EL ERROR** |
| **CAMBIOS_REALIZADOS.md** | Qué cambié exactamente |

---

## ✅ Verificación Final

Después de recompilar, verás **UNA** de estas dos cosas:

### Escenario 1: API Funciona ✅
```
🔵 Iniciando llamada a API con modelo: gemini-1.5-flash
🔵 API Key presente: true
🔵 Conexión establecida, enviando payload...
🔵 API Response Code: 200
✅ Respuesta OK recibida, parseando JSON...
✅ Respuesta extraída exitosamente (250 caracteres)
✅ Respuesta exitosa de Gemini API
```
**Resultado**: Chat funciona normalmente SIN "(Modo Local)"

### Escenario 2: API Falla ❌
```
🔵 Iniciando llamada a API con modelo: gemini-1.5-flash
🔵 API Key presente: true
🔵 Conexión establecida, enviando payload...
🔵 API Response Code: 401
❌ Error API (401): Invalid API key
❌ Tipo de error: Autenticación fallida (API Key inválida?)
⚠️ Error de API (UnknownError), usando Fallback local
```
**Resultado**: Chat responde SÍ con "(Modo Local)" al final

Si ves Escenario 2, **copia los logs exactos** y consulta **DIAGNOSTICO_API.md** para la solución.

---

## 🎯 Resumen Rápido

```
1. Recompila     → Build la app
2. Captura logs  → Abre Logcat/capturar_logs.bat
3. Envía mensaje → En la app
4. Lee error     → Busca "❌ Error API"
5. Soluciona     → Consulta DIAGNOSTICO_API.md
```

---

## ⏱️ Tiempo Estimado
- **Recompilar**: 1-2 minutos
- **Ver logs**: 30 segundos
- **Encontrar problema**: 10 segundos
- **Solucionar**: 1-5 minutos (depende del error)

**Total: ~10 minutos máximo**

---

## 🆘 Si Aún Tiene Problemas

1. Verifica que Android SDK esté instalado correctamente
2. Verifica que el emulador/dispositivo está conectado (`adb devices`)
3. Verifica que la app está corriendo (deberías verla en el emulador)
4. Intenta limpiar proyecto: `Build → Clean Project` y recompila

---

**¡Ahora mismo sabrás exactamente qué está fallando!** 🎯

