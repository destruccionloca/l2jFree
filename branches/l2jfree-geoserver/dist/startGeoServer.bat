@echo off
title GeoServer Console
:start
echo Starting l2jfree geoserver
echo.

SET OLDCLASSPATH=%CLASSPATH%
call setenv.bat

java -Dfile.encoding=UTF-8 -Xmx1024m com.l2jfree.geoserver.GeoServer

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