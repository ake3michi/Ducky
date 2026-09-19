@echo off
echo ========================================
echo CAPTURANDO LOGS DE IAHUMANIZADA v4
echo ========================================
echo.

REM Ruta completa a ADB
set ADB_PATH=C:\Users\%USERNAME%\AppData\Local\Android\Sdk\platform-tools\adb.exe

echo Verificando ADB...
if not exist "%ADB_PATH%" (
    echo ❌ ADB no encontrado en: %ADB_PATH%
    echo.
    echo SOLUCION: Instala Android SDK Platform-Tools
    pause
    exit /b 1
)

echo ✅ ADB encontrado
echo.

echo Verificando dispositivos...
"%ADB_PATH%" devices
echo.

REM Verificar si hay dispositivos
"%ADB_PATH%" devices | findstr "device" >nul
if %errorlevel% neq 0 (
    echo ❌ No hay dispositivos conectados
    echo.
    echo SOLUCION:
    echo 1. Abre Android Studio
    echo 2. Tools → Device Manager
    echo 3. Inicia un emulador
    echo 4. Ejecuta la app
    echo 5. Envía un mensaje
    echo 6. Vuelve a ejecutar este script
    pause
    exit /b 1
)

echo ✅ Dispositivo conectado
echo.

echo ========================================
echo CAPTURANDO LOGS EN TIEMPO REAL...
echo ========================================
echo.
echo Presiona Ctrl+C para detener y ver los logs
echo.

"%ADB_PATH%" logcat -v threadtime IAHumanizada *:S

echo.
echo ========================================
echo LOGS CAPTURADOS
echo ========================================
echo.
pause
