@echo off
:start
echo Starting L2J Game Server.
echo.
java -Xmx1024m -cp ./config/;commons-logging-1.1.jar;ibmaio.jar;log4j-1.2.14.jar;bsf.jar;bsh-2.0.jar;javolution.jar;c3p0-0.9.0.4.jar;mysql-connector-java-3.1.10-bin.jar;l2j-gameserver.jar;jython.jar net.sf.l2j.gameserver.GameServer
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
