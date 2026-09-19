@echo off
echo ========================================
echo CAPTURANDO LOGS DE IAHUMANIZADA - MODELO SARA
echo ========================================
echo.

echo Verificando dispositivos conectados...
adb devices
echo.

echo Si no ves dispositivos arriba, necesitas:
echo 1. Abrir Android Studio
echo 2. Tools → Device Manager
echo 3. Crear o iniciar un emulador
echo 4. Ejecutar la app
echo 5. Enviar un mensaje
echo 6. Volver a ejecutar este script
echo.

echo Esperando 3 segundos...
timeout /t 3 /nobreak > nul

echo ========================================
echo LOGS DE IAHUMANIZADA (ultimos 50)
echo ========================================

adb logcat -v threadtime -d | findstr "IAHumanizada" | tail -50

echo.
echo ========================================
echo FIN DE LOS LOGS
echo ========================================
echo.
echo Si no ves logs arriba, significa que:
echo - No hay dispositivo/emulador conectado
echo - La app no esta ejecutandose
echo - No has enviado ningun mensaje
echo.
echo Solucion: Ejecuta la app, envia un mensaje, luego vuelve a ejecutar este script.
echo.
echo NOTA: Ahora usa modelo personalizado 'Sara' via TailScale (100.127.147.114:3000)
echo.
pause
