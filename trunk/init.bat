@echo off

REM #############################################
REM # Configure this, if you don't have 'mvn' as an environment variable!
set MAVEN=mvn
REM #############################################

echo.
call %MAVEN% clean:clean eclipse:clean eclipse:eclipse -DdownloadSources=true -DdownloadJavadocs=true dependency:sources install -Dmaven.test.skip=true
echo.
echo Done.
pause
