@echo off

REM #############################################
REM # Configure this, if you don't have 'svn' in the path!
SET PATH=%PATH%;C:\Program Files\Subversion\bin

set MAVEN_OPTS=-Xms64m -Xmx256m

REM # Configure this, if you don't have 'mvn' in the path!
set MAVEN=mvn
REM #############################################

echo.
cd ..
cd l2j-mmocore
call %MAVEN% clean:clean deploy -Dmaven.test.skip=true
cd ..
cd l2j-commons
call %MAVEN% clean:clean deploy -Dmaven.test.skip=true
cd ..
cd tools
echo.
echo Snapshots deployed.
pause
