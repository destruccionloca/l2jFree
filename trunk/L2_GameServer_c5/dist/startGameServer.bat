@echo off
:start
echo Starting L2J Game Server.
echo.


SET OLDCLASSPATH=%CLASSPATH%
call setenv.bat

java -Xmx1024m net.sf.l2j.gameserver.GameServer

SET CLASSPATH=%OLDCLASSPATH%

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Admin Restart ...
echo.
goto start
:error
echo.
echo Server terminated abnormaly
echo.
:end
echo.
echo server terminated
echo.
pause
