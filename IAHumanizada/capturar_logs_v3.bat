@echo off
setlocal enabledelayedexpansion

echo ========================================
echo CAPTURANDO LOGS DE IAHUMANIZADA v3
echo ========================================
echo.

REM Ruta completa a ADB (basado en instalación típica de Android Studio)
set ADB_PATH=C:\Users\%USERNAME%\AppData\Local\Android\Sdk\platform-tools\adb.exe

echo Verificando ADB...
if not exist "%ADB_PATH%" (
    echo ❌ ADB no encontrado en: %ADB_PATH%
    echo.
    echo SOLUCIONES:
    echo 1. Asegúrate de que Android Studio esté instalado
    echo 2. Ve a Tools → SDK Manager → SDK Tools
    echo 3. Instala "Android SDK Platform-Tools"
    echo 4. Reinicia Android Studio
    echo.
    pause
    exit /b 1
)

echo ✅ ADB encontrado: %ADB_PATH%
echo.

echo Verificando dispositivos conectados...
"%ADB_PATH%" devices
echo.

REM Contar dispositivos (excluyendo el header)
for /f %%i in ('"%ADB_PATH%" devices ^| findstr /r /c:"^[a-zA-Z0-9]" ^| find /c "device"') do set DEVICE_COUNT=%%i

if %DEVICE_COUNT% equ 0 (
    echo ❌ NO HAY DISPOSITIVOS CONECTADOS
    echo.
    echo INSTRUCCIONES PARA CONECTAR UN DISPOSITIVO:
    echo.
    echo OPCION A - EMULADOR:
    echo 1. Abre Android Studio
    echo 2. Tools → Device Manager
    echo 3. Create Device (elige un teléfono)
    echo 4. Launch (inicia el emulador)
    echo 5. Espera a que cargue completamente
    echo.
    echo OPCION B - DISPOSITIVO REAL:
    echo 1. Habilita "USB Debugging" en tu teléfono
    echo 2. Conecta por USB
    echo 3. Acepta el prompt de depuración USB
    echo.
    echo Luego ejecuta la app y envía un mensaje.
    echo.
    pause
    exit /b 1
)

echo ✅ %DEVICE_COUNT% dispositivo(s) conectado(s)
echo.

echo ========================================
echo CAPTURANDO LOGS...
echo ========================================
echo.

echo Ejecutando: adb logcat -v threadtime IAHumanizada *:S
echo (Presiona Ctrl+C para detener)
echo.

"%ADB_PATH%" logcat -v threadtime IAHumanizada *:S

echo.
echo ========================================
echo LOGS CAPTURADOS
echo ========================================
echo.
pause
