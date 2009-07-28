SET VERSION=1.3.0

SET CLASSPATH=%CLASSPATH%;./lib/l2j-mmocore-%VERSION%.jar

REM jython
SET CLASSPATH=%CLASSPATH%;./lib/jython-2.2.1.jar
SET CLASSPATH=%CLASSPATH%;./lib/jython-engine-1.0.1.jar

SET CLASSPATH=%CLASSPATH%;./lib/bsf-2.0.jar
SET CLASSPATH=%CLASSPATH%;./lib/bsh-2.0b5.jar
SET CLASSPATH=%CLASSPATH%;./lib/bsh-engine-1.0.1.jar
SET CLASSPATH=%CLASSPATH%;./lib/core-3.3.0.jar
SET CLASSPATH=%CLASSPATH%;./lib/java-engine-1.0.2.jar

SET CLASSPATH=%CLASSPATH%;./lib/commons-lang-2.4.jar

REM For connection pool
SET CLASSPATH=%CLASSPATH%;./lib/c3p0-0.9.1.2.jar

REM for logging usage
SET CLASSPATH=%CLASSPATH%;./lib/commons-logging-1.1.1.jar

REM for common input output 
SET CLASSPATH=%CLASSPATH%;./lib/commons-io-1.4.jar

REM for performance usage
SET CLASSPATH=%CLASSPATH%;./lib/javolution-5.3.1.jar

REM main jar
SET CLASSPATH=%CLASSPATH%;./lib/l2j-commons-%VERSION%.jar
SET CLASSPATH=%CLASSPATH%;l2jfree-core-%VERSION%.jar

REM For SQL use
SET CLASSPATH=%CLASSPATH%;./lib/mysql-connector-java-5.1.6.jar

REM For IRC use
SET CLASSPATH=%CLASSPATH%;./lib/irclib-1.10.jar

SET CLASSPATH=%CLASSPATH%;./lib/trove-2.1.1.jar

REM for configuration
SET CLASSPATH=%CLASSPATH%;./config/
SET CLASSPATH=%CLASSPATH%;.