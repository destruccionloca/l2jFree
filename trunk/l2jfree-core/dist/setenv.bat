REM Hibernate and associated jars

SET CLASSPATH=%CLASSPATH%;l2j-mmocore-1.3.0.jar

REM jython
SET CLASSPATH=%CLASSPATH%;jython-2.2.1.jar
SET CLASSPATH=%CLASSPATH%;jython-engine-1.0.1.jar

SET CLASSPATH=%CLASSPATH%;bsf-2.0.jar
SET CLASSPATH=%CLASSPATH%;bsh-2.0b5.jar
SET CLASSPATH=%CLASSPATH%;bsh-engine-1.0.1.jar
SET CLASSPATH=%CLASSPATH%;core-3.3.0.jar
SET CLASSPATH=%CLASSPATH%;java-engine-1.0.2.jar

SET CLASSPATH=%CLASSPATH%;commons-lang-2.1.jar

REM For connection pool
SET CLASSPATH=%CLASSPATH%;c3p0-0.9.1.2.jar

REM for logging usage
SET CLASSPATH=%CLASSPATH%;commons-logging-1.1.jar
REM enable if using log4j
REM SET CLASSPATH=%CLASSPATH%;log4j-1.2.14.jar

REM for common input output 
SET CLASSPATH=%CLASSPATH%;commons-io-1.2.jar

REM for performance usage
SET CLASSPATH=%CLASSPATH%;javolution-1.5.5.2.6.jar

REM main jar
SET CLASSPATH=%CLASSPATH%;l2j-commons-1.3.0.jar
SET CLASSPATH=%CLASSPATH%;l2jfree-core-1.3.0.jar

REM For SQL use
SET CLASSPATH=%CLASSPATH%;mysql-connector-java-5.1.5.jar

REM For IRC use
SET CLASSPATH=%CLASSPATH%;irclib-1.10.jar

REM for configuration
SET CLASSPATH=%CLASSPATH%;./config/
SET CLASSPATH=%CLASSPATH%;.