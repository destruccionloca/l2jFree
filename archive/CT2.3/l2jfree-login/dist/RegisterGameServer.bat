@echo off
SET OLDCLASSPATH=%CLASSPATH%
call setenv.bat

@java -Djava.util.logging.config.file=console.cfg com.l2jfree.gsregistering.GameServerRegister

SET CLASSPATH=%OLDCLASSPATH%
@pause