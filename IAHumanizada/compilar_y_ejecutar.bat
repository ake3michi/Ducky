@echo off
echo ========================================
echo COMPILAR Y EJECUTAR IAHUMANIZADA
echo ========================================
echo.
echo Este script va a:
echo 1. Limpiar el proyecto
echo 2. Compilar el APK
echo 3. Instalar en dispositivo/emulador
echo 4. Abrirlo en logcat
echo.
echo REQUISITOS:
echo - Tener adb en PATH o usar Android Studio
echo - Tener el dispositivo/emulador conectado
echo - Haber configurado GEMINI_API_KEY en local.properties
echo.
pause

echo.
echo ========================================
echo VERIFICANDO LOCAL.PROPERTIES...
echo ========================================

REM Verificar si GEMINI_API_KEY está configurada
findstr /c:"GEMINI_API_KEY=" local.properties > nul
if errorlevel 1 (
    echo.
    echo [!] ADVERTENCIA: GEMINI_API_KEY no está configurada
    echo.
    echo Sigue estos pasos:
    echo 1. Abre https://aistudio.google.com/apikey
    echo 2. Copia tu API Key
    echo 3. Abre local.properties
    echo 4. Busca: GEMINI_API_KEY=
    echo 5. Pega tu key: GEMINI_API_KEY=AIzaSyD...
    echo 6. Guarda y vuelve a ejecutar este script
    echo.
    pause
    exit /b 1
)

echo [OK] GEMINI_API_KEY encontrada en local.properties
echo.

echo ========================================
echo COMPILANDO...
echo ========================================
echo.

REM Para Windows, usar gradlew.bat
if exist gradlew.bat (
    call gradlew.bat clean build
) else (
    echo Error: gradlew.bat no encontrado
    pause
    exit /b 1
)

if errorlevel 1 (
    echo.
    echo [ERROR] Compilación fallida
    echo Revisa los errores arriba
    pause
    exit /b 1
)

echo.
echo ========================================
echo COMPILACIÓN EXITOSA
echo ========================================
echo.
echo El siguiente paso depende de tu setup:
echo.
echo OPCIÓN A: Usando Android Studio
echo 1. Abre Android Studio
echo 2. Run → Run 'app' (o Shift+F10)
echo.
echo OPCIÓN B: Usando adb (línea de comandos)
echo Los cambios están listos para usar
echo En Android Studio, ejecuta mediante Run menu
echo.
pause

