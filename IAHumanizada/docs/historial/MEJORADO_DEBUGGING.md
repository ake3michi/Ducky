# Mejoras de Debugging - API Response en Modo Local

## Cambios Realizados

### 1. **Mejorado ApiService.kt** ✅
Agregué logging detallado en cada etapa de la llamada a la API:

- **Inicio**: Muestra modelo y confirmación de API Key
- **Conexión**: Log cuando se establece conexión  
- **Respuesta exitosa**: Log cuando se recibe 200 OK y se parsea JSON
- **Errores**: Logs específicos para cada tipo de error (401, 403, 429, 400, 500, etc.)

**Emojis en logs para identificar fácilmente**:
- 🔵 = Pasos normales (azul = información)
- ✅ = Éxito (verde)
- ⚠️ = Advertencia (amarillo)
- ❌ = Error (rojo)

### 2. **Mejorado ChatViewModel.kt** ✅
Diferenciación de tipos de errores de API:

```kotlin
when (e) {
    is ApiError.SafetyFiltered -> Log: "Bloqueado por filtros"
    is ApiError.QuotaExceeded -> Log: "Cuota de API excedida"
    is ApiError.NetworkError -> Log: "Error de conectividad"
    is ApiError.UnknownError -> Log: "Error [code]: [mensaje]"
}
```

### 3. **Creado DIAGNOSTICO_API.md** ✅
Guía de troubleshooting que incluye:
- Las 4 causas más comunes por qué falla
- Cómo leer LogCat
- Secuencia de logs esperada vs real
- Links a recursos

---

## Cómo Usar Estos Logs

### Paso 1: Ejecuta la App
```bash
# En Android Studio, abre Logcat o ejecuta:
adb logcat | grep "IAHumanizada"
```

### Paso 2: Envía un Mensaje
Escribe cualquier cosa en el chat para disparar el flujo.

### Paso 3: Busca en los Logs

**Si ves "Modo Local" al final** = Fallback activado ❌

**Busca la línea con error**:
```
❌ Error API (XXX): ...
```

El código de error te dirá:
- `401/403` → API Key inválida
- `429` → Cuota excedida
- `400` → Filtro de seguridad o payload inválido
- `500/502/503` → Servidor de Google caído

### Paso 4: Solucionar

Ver `DIAGNOSTICO_API.md` para la solución específica.

---

## Archivos Modificados

1. **ApiService.kt** (133 líneas)
   - Línea 28-43: Logs de inicio y conexión
   - Línea 81-109: Logs de respuesta y errores detallados
   - Línea 127: Log con stacktrace de errores

2. **ChatViewModel.kt** (137 líneas)
   - Línea 87-103: Manejo diferenciado de tipos de ApiError
   - Logs específicos para cada tipo de fallo

3. **DIAGNOSTICO_API.md** (Nuevo)
   - Guía completa de troubleshooting
   - Causas probables ordenadas por probabilidad
   - Cómo leer LogCat

---

## Próximo Paso

Ejecuta la app, envía un mensaje, y **copia la salida de LogCat** cuando veas "(Modo Local)". Eso nos dirá exactamente dónde está el problema.

Los logs ahora son muchísimo más informativos que antes. 🎯

