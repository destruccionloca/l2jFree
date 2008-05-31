@echo off
SET OLDCLASSPATH=%CLASSPATH%
call setenv.bat

@java -Djava.util.logging.config.file=console.cfg net.sf.l2j.accountmanager.AccountManager

SET CLASSPATH=%OLDCLASSPATH%
@pause
