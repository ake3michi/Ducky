# ✅ CONFIGURACIÓN FINAL - OLLAMA CON MODELO SARA

## 📝 CAMBIOS REALIZADOS

### Código Actualizado
- ✅ **Modelo**: Cambiado a `"Sara"` (modificación de llama3.2:3b)
- ✅ **URL**: `http://100.127.147.114:3000/api/generate` (TailScale)
- ✅ **Configuración**: Optimizada para modelo personalizado

### Archivo Modificado
**`ApiService.kt`** - Línea 18
```kotlin
private val modelo: String = "Sara"
```

## 🎯 CONFIGURACIÓN DE OLLAMA

### 1. Instalar Ollama
```bash
# Descarga desde https://ollama.com
# Instala en tu PC
```

### 2. Ejecutar Ollama Server
```bash
# Ejecuta en puerto 3000 para acceso remoto
ollama serve --host 0.0.0.0 --port 3000
```

### 3. Instalar Modelo Sara
```bash
# Si es un modelo personalizado, cárgalo así:
ollama create Sara -f Modelfile

# O si está disponible remotamente:
ollama pull nombre-del-modelo-sara
```

### 4. Verificar Modelo
```bash
ollama list
# Deberías ver "Sara" en la lista
```

## 📊 ESPECIFICACIONES DEL MODELO

| Propiedad | Valor |
|-----------|-------|
| **Nombre** | Sara |
| **Base** | llama3.2:3b |
| **Tipo** | Modificación personalizada |
| **Tamaño** | ~3B parámetros |
| **Uso** | Conversacional personalizado |

## 🚀 CONFIGURACIÓN DE TAILSCALE

### IP Configurada
- **Dirección**: `100.127.147.114`
- **Puerto**: `3000`
- **Protocolo**: HTTP (permitido en Android)

### Requisitos
- ✅ TailScale instalado en PC
- ✅ TailScale instalado en dispositivo Android
- ✅ Ambos dispositivos en la misma red TailScale
- ✅ Puerto 3000 abierto en firewall

## ✨ CARACTERÍSTICAS

- ✅ **Modelo personalizado**: Sara (tu modificación de llama3.2:3b)
- ✅ **Acceso remoto**: Via TailScale VPN
- ✅ **100% Gratuito**: Sin costos de API
- ✅ **Privado**: Tus conversaciones quedan en tu PC
- ✅ **Offline**: Funciona sin internet (una vez cargado el modelo)

## 🔧 PRUEBA DE FUNCIONAMIENTO

### 1. Verificar Ollama
```bash
# En terminal de PC
curl http://localhost:3000/api/tags
# Deberías ver el modelo "Sara" listado
```

### 2. Probar desde Android
- Recompila la app
- Envía un mensaje
- Busca en logs:
  ```
  🔵 Iniciando llamada a API con modelo: Sara
  ✅ Respuesta generada recibida
  ```

## 📋 COMANDOS ÚTILES

```bash
# Ver modelos disponibles
ollama list

# Ejecutar modelo directamente
ollama run Sara

# Ver logs de Ollama
ollama logs

# Detener Ollama
ollama stop
```

## 🎯 RESULTADO ESPERADO

- ✅ **Conexión exitosa** a Ollama via TailScale
- ✅ **Modelo Sara** cargado y funcionando
- ✅ **Respuestas personalizadas** de tu IA modificada
- ✅ **Funcionamiento offline** después de la primera carga

## 🚨 SOLUCIÓN DE PROBLEMAS

### "Model not found"
```bash
# Asegúrate de que el modelo esté creado
ollama create Sara -f Modelfile
# O verifica el nombre exacto
ollama list
```

### "Connection refused"
```bash
# Verifica que Ollama esté corriendo
ollama serve --host 0.0.0.0 --port 3000
# Verifica IP de TailScale
tailscale ip
```

### "Network error"
- Verifica conectividad TailScale
- Verifica que el puerto 3000 esté abierto
- Prueba con `curl http://100.127.147.114:3000/api/tags`

---

**¡Configura Ollama con el modelo Sara y recompila la app!** 🚀
