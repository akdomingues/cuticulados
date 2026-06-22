@echo off
setlocal enabledelayedexpansion

rem Garante que estamos na pasta do projeto (onde esta o rodar.bat)
cd /d "%~dp0"

rem ---- Localiza um JDK ----
rem 1) Usa JAVA_HOME se ja estiver valido
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" goto :java_ok

rem 2) Procura nos JDKs do usuario (ex.: instalados pelo IntelliJ em %USERPROFILE%\.jdks)
for /d %%D in ("%USERPROFILE%\.jdks\*") do (
    if exist "%%D\bin\java.exe" set "JAVA_HOME=%%D"
)
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" goto :java_ok

rem 3) Fallback: JBR embarcado do IntelliJ
for /d %%D in ("%ProgramFiles%\JetBrains\IntelliJ IDEA*") do (
    if exist "%%D\jbr\bin\java.exe" set "JAVA_HOME=%%D\jbr"
)
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" goto :java_ok

echo [ERRO] Nao encontrei um JDK. Instale o Java 17 ou defina a variavel JAVA_HOME.
pause
exit /b 1

:java_ok
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo Usando JAVA_HOME=%JAVA_HOME%
java -version

rem ---- Roda a aplicacao via Maven wrapper ----
call "%~dp0mvnw.cmd" clean compile exec:java
set "EXITCODE=%ERRORLEVEL%"

if not "%EXITCODE%"=="0" (
    echo.
    echo [ERRO] A execucao falhou com codigo %EXITCODE%.
)
pause
exit /b %EXITCODE%
