@echo off

REM #############################################
set MAVEN_OPTS=-Xms64m -Xmx256m

REM # Configure this, if you don't have 'mvn' as an environment variable!
set MAVEN=mvn

REM # Toggle comments, if you will need sources and docs
set PROJECT_INIT_FLAG=
REM # set PROJECT_INIT_FLAG="-DdownloadSources=true -DdownloadJavadocs=true"
REM #############################################

echo.
cd ..
call %MAVEN% clean:clean eclipse:clean
echo Environment cleaned.
cd tools
call compile.bat
cd ..
call %MAVEN% eclipse:m2eclipse %PROJECT_INIT_FLAG%
cd tools
echo.
echo Environment initialized.
pause
