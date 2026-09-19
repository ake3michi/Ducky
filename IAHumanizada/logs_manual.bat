@echo off
echo ========================================
echo LOGS DE IAHUMANIZADA - MÉTODO FÁCIL
echo ========================================
echo.

echo PASO 1: Abre Android Studio
echo PASO 2: Ejecuta la app en emulador/dispositivo
echo PASO 3: Ve a View → Tool Windows → Logcat
echo PASO 4: En el filtro escribe: IAHumanizada
echo PASO 5: Envía un mensaje en la app
echo PASO 6: Copia los logs que aparecen
echo.

echo ========================================
echo LOGS ESPERADOS (copia y pega aquí)
echo ========================================
echo.
echo 🔵 Iniciando llamada a API con modelo: gemini-1.5-flash
echo 🔵 API Key presente: true
echo 🔵 Conexión establecida, enviando payload...
echo 🔵 API Response Code: 200
echo ✅ Respuesta OK recibida, parseando JSON...
echo ✅ Respuesta extraída exitosamente (XXX caracteres)
echo ✅ Respuesta exitosa de Gemini API
echo.

echo ========================================
echo SI SALE LOCAL (ERROR)
echo ========================================
echo.
echo ❌ Error API (401): Invalid API Key
echo ❌ Tipo de error: Autenticación fallida (API Key inválida?)
echo ⚠️ Error de API (UnknownError), usando Fallback local
echo.

echo INSTRUCCIONES:
echo 1. Copia los logs de Android Studio
echo 2. Pégalos en una respuesta
echo 3. Te diré exactamente qué hacer
echo.

pause
