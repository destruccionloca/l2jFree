@echo off

REM #############################################
REM # Configure this, if you don't have 'mvn' as an environment variable!
set MAVEN=mvn
REM #############################################

echo.
cd ..
call %MAVEN% clean:clean eclipse:clean eclipse:eclipse -DdownloadSources=true -DdownloadJavadocs=true dependency:sources
echo.
echo Done.
pause
