# 📚 ÍNDICE - Documentación de Debugging

## 🎯 ¿Por dónde empiezo?

### Si Acabas de Empezar
1. **👉 PASO_A_PASO.md** - Instrucciones paso a paso (5 minutos)
2. Lee los logs que aparecerán

### Si Ya Viste los Logs
1. **DIAGNOSTICO_API.md** - Encontrar la solución según el error

### Si Quieres Entender Todo
1. **README_DEBUGGING.md** - Resumen ejecutivo
2. **CAMBIOS_REALIZADOS.md** - Qué cambié exactamente

---

## 📁 Todos los Documentos

### 🟢 PARA EJECUTAR INMEDIATAMENTE

| Archivo | Descripción | Duración |
|---------|-------------|----------|
| **PASO_A_PASO.md** | Instrucciones paso a paso | ⏱️ 5 min |
| **capturar_logs.bat** | Script para ver logs (doble-clic) | ⏱️ Automático |

### 🟡 CUANDO VEAS UN ERROR

| Archivo | Descripción | Uso |
|---------|-------------|-----|
| **QUICKSTART_LOGS.md** | Explicación rápida del error | ⏱️ 2 min |
| **DIAGNOSTICO_API.md** | Guía de troubleshooting | ⏱️ 3-5 min |

### 🔵 PARA ENTENDER LOS CAMBIOS

| Archivo | Descripción | Detalles |
|---------|-------------|---------|
| **README_DEBUGGING.md** | Resumen completo | Cambios + cómo usar |
| **CAMBIOS_REALIZADOS.md** | Qué cambié específicamente | Código antes/después |

---

## 🚀 Flujo Recomendado

```
START
  ↓
1. Abre PASO_A_PASO.md (⏱️ 5 min)
  ↓
2. Recompila la app
  ↓
3. Ejecuta capturar_logs.bat
  ↓
4. Envía un mensaje
  ↓
5. Ves un error "❌ Error API"?
  ↓ SÍ
6. Copia el código de error (401, 429, etc)
  ↓
7. Abre DIAGNOSTICO_API.md
  ↓
8. Busca tu código de error en la tabla
  ↓
9. Sigue la solución
  ↓
FIN - ¡Problema resuelto!
```

---

## 🔍 Buscar por Error

### Error: "Autenticación fallida"
→ Ve a **DIAGNOSTICO_API.md** → Sección "API Key Inválida"

### Error: "Cuota excedida"
→ Ve a **DIAGNOSTICO_API.md** → Sección "Cuota"

### Error: "Sin conectividad"
→ Ve a **DIAGNOSTICO_API.md** → Sección "Conectividad"

### Error: "Bloqueado por seguridad"
→ Ve a **DIAGNOSTICO_API.md** → Sección "Filtros"

### No veo ningún error (todo OK)
→ ¡Tu app funciona! Consulta **README_DEBUGGING.md** para entender los cambios

---

## 📊 Resumen de Cambios de Código

### Archivos Modificados
1. **ApiService.kt** (139 líneas)
   - Logs detallados en líneas 28-127
   - Emojis para identificar fácilmente

2. **ChatViewModel.kt** (147 líneas)
   - Diferenciación de errores en líneas 87-103
   - Logs específicos por tipo de error

### Archivos Creados (Documentación)
1. **PASO_A_PASO.md** ← EMPIEZA AQUÍ
2. **DIAGNOSTICO_API.md**
3. **README_DEBUGGING.md**
4. **CAMBIOS_REALIZADOS.md**
5. **QUICKSTART_LOGS.md**
6. **capturar_logs.bat**

---

## 📚 Lectura Rápida

### 2 minutos
- QUICKSTART_LOGS.md

### 5 minutos
- PASO_A_PASO.md

### 10 minutos
- PASO_A_PASO.md + DIAGNOSTICO_API.md

### 15 minutos
- PASO_A_PASO.md + README_DEBUGGING.md + CAMBIOS_REALIZADOS.md

### Todo (comprensión total)
- Todos los documentos en orden

---

## 🎯 Quick Links por Problema

| Problema | Solución Rápida |
|----------|-----------------|
| Ver logs | Ejecuta `capturar_logs.bat` |
| API Key inválida (401) | Ver DIAGNOSTICO_API.md → API Key |
| Cuota excedida (429) | Ver DIAGNOSTICO_API.md → Cuota |
| Sin internet | Ver DIAGNOSTICO_API.md → Conectividad |
| Recompilación lenta | Normal, espera 1-2 min |
| No veo logs | Ver PASO_A_PASO.md → PASO 2 |
| ¿Qué cambió? | Ver CAMBIOS_REALIZADOS.md |

---

## 🔗 Recursos Externos

- [Google AI Studio - Generar API Key](https://aistudio.google.com/apikey)
- [Gemini API Documentación](https://ai.google.dev/docs)
- [Android LogCat Help](https://developer.android.com/studio/debug/logcat)

---

## ✅ Checklist Antes de Empezar

- [ ] Android SDK instalado
- [ ] Emulador o dispositivo conectado
- [ ] `adb devices` muestra tu dispositivo
- [ ] Kotlin SDK configurado
- [ ] Build Tools actualizados

---

## 📞 Resumen

**Problema**: App responde en modo local (sin API)
**Causa**: La API está fallando
**Solución**: Logs detallados te muestran exactamente qué falla
**Tiempo**: ~10 minutos desde aquí hasta resuelto

**Empieza aquí**: 👉 **PASO_A_PASO.md**

---

## 🎯 Objetivo Final

Después de seguir PASO_A_PASO.md, verás logs como este:
```
✅ Respuesta exitosa de Gemini API
```

En lugar de:
```
⚠️ Error de API, usando Fallback local
```

¡Eso significa que todo está funcionando! 🚀

---

**Última actualización**: 2026-04-20
**Versión**: 1.0
**Estado**: ✅ Listo para usar

