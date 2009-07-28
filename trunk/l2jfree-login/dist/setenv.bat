SET VERSION=1.3.0

REM Hibernate and associated jars
SET CLASSPATH=%CLASSPATH%;./lib/antlr-2.7.6.jar
SET CLASSPATH=%CLASSPATH%;./lib/asm-1.5.3.jar
SET CLASSPATH=%CLASSPATH%;./lib/asm-attrs-1.5.3.jar
SET CLASSPATH=%CLASSPATH%;./lib/cglib-2.1_3.jar
SET CLASSPATH=%CLASSPATH%;./lib/hibernate-3.2.2.ga.jar
SET CLASSPATH=%CLASSPATH%;./lib/jta-1.0.1B.jar
SET CLASSPATH=%CLASSPATH%;./lib/commons-collections-2.1.1.jar

REM for second level cache (hibernate)
SET CLASSPATH=%CLASSPATH%;./lib/ehcache-1.2.3.jar

SET CLASSPATH=%CLASSPATH%;./lib/l2j-mmocore-%VERSION%.jar

SET CLASSPATH=%CLASSPATH%;./lib/commons-lang-2.4.jar

REM For connection pool
SET CLASSPATH=%CLASSPATH%;./lib/c3p0-0.9.1.2.jar

REM for logging usage
SET CLASSPATH=%CLASSPATH%;./lib/commons-logging-1.1.1.jar

REM for common input output 
SET CLASSPATH=%CLASSPATH%;./lib/commons-io-1.4.jar

REM for dom 
SET CLASSPATH=%CLASSPATH%;./lib/dom4j-1.6.1.jar

REM for performance usage
SET CLASSPATH=%CLASSPATH%;./lib/javolution-5.3.1.jar

REM main jar
SET CLASSPATH=%CLASSPATH%;./lib/l2j-commons-%VERSION%.jar
SET CLASSPATH=%CLASSPATH%;l2jfree-login-%VERSION%.jar

REM spring 
SET CLASSPATH=%CLASSPATH%;./lib/spring-2.0.2.jar
SET CLASSPATH=%CLASSPATH%;./lib/spring-mock-2.0.2.jar

REM For SQL use
SET CLASSPATH=%CLASSPATH%;./lib/mysql-connector-java-5.1.6.jar

REM for configuration
SET CLASSPATH=%CLASSPATH%;./config/
SET CLASSPATH=%CLASSPATH%;.