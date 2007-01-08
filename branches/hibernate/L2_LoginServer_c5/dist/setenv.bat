REM Hibernate and associated jars
SET CLASSPATH=%CLASSPATH%;antlr-2.7.6.jar
SET CLASSPATH=%CLASSPATH%;asm.jar
SET CLASSPATH=%CLASSPATH%;asm-attrs.jar
SET CLASSPATH=%CLASSPATH%;cglib-2.1.3.jar
SET CLASSPATH=%CLASSPATH%;hibernate-3.1rc2.jar
SET CLASSPATH=%CLASSPATH%;jta.jar
SET CLASSPATH=%CLASSPATH%;commons-collections-3.2.jar


REM for second level cache (hibernate)
SET CLASSPATH=%CLASSPATH%;ehcache-1.2.3.jar

REM for bean use
SET CLASSPATH=%CLASSPATH%;commons-beanutils-1.7.0.jar

REM For connection pool
SET CLASSPATH=%CLASSPATH%;c3p0-0.9.0.4.jar

REM for logging usage
SET CLASSPATH=%CLASSPATH%;commons-logging-1.1.jar
SET CLASSPATH=%CLASSPATH%;log4j-1.2.14.jar

REM for common input output 
SET CLASSPATH=%CLASSPATH%;commons-io-1.2.jar

REM for dom 
SET CLASSPATH=%CLASSPATH%;dom4j-1.6.1.jar

REM for performance usage
SET CLASSPATH=%CLASSPATH%;javolution.jar

REM main jar
SET CLASSPATH=%CLASSPATH%;l2j-loginserver.jar

REM spring 
SET CLASSPATH=%CLASSPATH%;spring-2.0-rc3.jar

REM For SQL use
SET CLASSPATH=%CLASSPATH%;mysql-connector-java-3.1.10-bin.jar
SET CLASSPATH=%CLASSPATH%;sqljdbc.jar

REM for configuration
SET CLASSPATH=%CLASSPATH%;./config/