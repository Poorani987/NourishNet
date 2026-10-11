@REM Maven wrapper runner for Windows
@echo off
setlocal
set "DIR=%~dp0"
if exist "%DIR%..\..\..\..\apache-maven-3.9.9\bin\mvn.cmd" (
    call "%DIR%..\..\..\..\apache-maven-3.9.9\bin\mvn.cmd" %*
    exit /b %ERRORLEVEL%
)
if exist "%USERPROFILE%\apache-maven-3.9.9\bin\mvn.cmd" (
    call "%USERPROFILE%\apache-maven-3.9.9\bin\mvn.cmd" %*
    exit /b %ERRORLEVEL%
)
call mvn %*
exit /b %ERRORLEVEL%
