@echo off
SET OLDCLASSPATH=%CLASSPATH%
call setenv.bat

@java -Djava.util.logging.config.file=console.cfg net.sf.l2j.gsregistering.GameServerRegister

SET CLASSPATH=%OLDCLASSPATH%
@pause