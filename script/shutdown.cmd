@echo off

if not exist "%JAVA_HOME%\bin\jps.exe" (
    echo Please set the JAVA_HOME variable in your environment to the correct JDK directory.
    echo JDK8 or later is recommended!
    EXIT /B 1
)

setlocal

set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Stopping Thingshub...

set "THINGSHUB_RUNNING=false"
for /f "tokens=1" %%i in ('jps -m ^| find "nacos.nacos"') do (
    set "THINGSHUB_RUNNING=true"
    taskkill /F /PID %%i
)

if "%THINGSHUB_RUNNING%"=="true" (
    echo Thingshub stopped.
) else (
    echo Thingshub is not running.
)

echo Done!
