@echo off

REM #############################################
set MAVEN_OPTS=-Xms64m -Xmx256m

REM # Configure this, if you don't have 'mvn' as an environment variable!
set MAVEN=mvn
REM #############################################

echo.
cd ..
cd l2j-mmocore
call %MAVEN% clean:clean install -Dmaven.test.skip=true
cd ..
cd l2j-commons
call %MAVEN% clean:clean install -Dmaven.test.skip=true
cd ..
cd l2jfree-core
call %MAVEN% clean:clean install assembly:assembly -Dmaven.test.skip=true
cd ..
cd l2jfree-login
call %MAVEN% clean:clean assembly:assembly -Dmaven.test.skip=true
cd ..
cd l2jfree-datapack
call %MAVEN% clean:clean compile -Dmaven.test.skip=true
cd ..
cd tools
echo.
echo Sources compiled, and dependencies installed to the local repository.
pause
