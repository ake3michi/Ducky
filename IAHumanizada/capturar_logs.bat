@echo off
REM Script para capturar LogCat de IAHumanizada
REM Uso: capturar_logs.bat

echo ========================================
echo LogCat Capture para IAHumanizada
echo ========================================
echo.

REM Buscar Android SDK
if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
    set ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
) else (
    echo ERROR: adb no encontrado en ruta estándar
    echo Asegúrate que Android SDK está instalado
    pause
    exit /b 1
)

echo Dispositivos conectados:
%ADB% devices

echo.
echo Limpiando logs anteriores...
%ADB% logcat -c

echo.
echo ========================================
echo Esperando mensajes (Ctrl+C para detener)
echo ========================================
echo.
echo INSTRUCCIONES:
echo 1. En la app, envía un mensaje
echo 2. Mira los logs abajo
echo 3. Presiona Ctrl+C cuando termines
echo.

%ADB% logcat -v threadtime | findstr "IAHumanizada"

pause

