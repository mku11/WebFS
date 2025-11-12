@ECHO ON
set WEBFS_VERSION=1.0.0

set CURRDIR=%CD%
set JAVA_WS=java-ws
set WS_APP_PROPERTIES=.\config\application.properties
set WS_SCRIPT_SH=.\scripts\start-webfs-service.sh
set WS_SCRIPT_BAT=.\scripts\start-webfs-service.bat
set WS_WAR=.\build\libs\webfs-service-%WEBFS_VERSION%.war
set WS_WAR_NAME=webfs-service.war

set CONFIG_DIR=config
set OUTPUT_ROOT=..\..\output
set WS_OUTPUT_DIR=%OUTPUT_ROOT%\%JAVA_WS%
set WS_PACKAGE_NAME=%JAVA_WS%.%WEBFS_VERSION%
set WS_PACKAGE_NAME_ZIP=%JAVA_WS%.%WEBFS_VERSION%.zip

powershell mkdir -ErrorAction SilentlyContinue %WS_OUTPUT_DIR%

:: Web Service
rd /s /q %WS_OUTPUT_DIR%\%WS_PACKAGE_NAME%
powershell mkdir -ErrorAction SilentlyContinue %WS_OUTPUT_DIR%\%WS_PACKAGE_NAME%
powershell mkdir -ErrorAction SilentlyContinue %WS_OUTPUT_DIR%\%WS_PACKAGE_NAME%\%CONFIG_DIR%
copy %WS_SCRIPT_BAT% %WS_OUTPUT_DIR%\%WS_PACKAGE_NAME%
copy %WS_SCRIPT_SH% %WS_OUTPUT_DIR%\%WS_PACKAGE_NAME%
copy %WS_WAR% %WS_OUTPUT_DIR%\%WS_PACKAGE_NAME%\%WS_WAR_NAME%
copy %WS_APP_PROPERTIES% %WS_OUTPUT_DIR%\%WS_PACKAGE_NAME%\%CONFIG_DIR%
copy README.txt %WS_OUTPUT_DIR%\%WS_PACKAGE_NAME%\README.txt
cd %WS_OUTPUT_DIR%\%WS_PACKAGE_NAME%
powershell -command Compress-Archive -Force -DestinationPath ..\%WS_PACKAGE_NAME_ZIP% -Path *
cd %CURRDIR%