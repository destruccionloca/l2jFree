REM SET VERSION=1.3.0

REM Hibernate and associated jars
REM SET CLASSPATH=%CLASSPATH%;./lib/antlr-2.7.6.jar
REM SET CLASSPATH=%CLASSPATH%;./lib/asm-1.5.3.jar
REM SET CLASSPATH=%CLASSPATH%;./lib/asm-attrs-1.5.3.jar
REM SET CLASSPATH=%CLASSPATH%;./lib/cglib-2.1_3.jar
REM SET CLASSPATH=%CLASSPATH%;./lib/hibernate-3.2.2.ga.jar
REM SET CLASSPATH=%CLASSPATH%;./lib/jta-1.0.1B.jar
REM SET CLASSPATH=%CLASSPATH%;./lib/commons-collections-2.1.1.jar

REM for second level cache (hibernate)
REM SET CLASSPATH=%CLASSPATH%;./lib/ehcache-1.2.3.jar

REM SET CLASSPATH=%CLASSPATH%;./lib/l2j-mmocore-%VERSION%.jar

REM SET CLASSPATH=%CLASSPATH%;./lib/commons-lang-2.4.jar

REM For connection pool
REM SET CLASSPATH=%CLASSPATH%;./lib/c3p0-0.9.1.2.jar

REM for logging usage
REM SET CLASSPATH=%CLASSPATH%;./lib/commons-logging-1.1.1.jar

REM for common input output 
REM SET CLASSPATH=%CLASSPATH%;./lib/commons-io-1.4.jar

REM for dom 
REM SET CLASSPATH=%CLASSPATH%;./lib/dom4j-1.6.1.jar

REM for performance usage
REM SET CLASSPATH=%CLASSPATH%;./lib/javolution-5.3.1.jar

REM main jar
REM SET CLASSPATH=%CLASSPATH%;./lib/l2j-commons-%VERSION%.jar
REM SET CLASSPATH=%CLASSPATH%;l2jfree-login-%VERSION%.jar
SET CLASSPATH=%CLASSPATH%;./lib/*

REM spring 
REM SET CLASSPATH=%CLASSPATH%;./lib/spring-2.0.2.jar
REM SET CLASSPATH=%CLASSPATH%;./lib/spring-mock-2.0.2.jar

REM For SQL use
REM SET CLASSPATH=%CLASSPATH%;./lib/mysql-connector-java-5.1.6.jar

REM for configuration
SET CLASSPATH=%CLASSPATH%;./config/*
SET CLASSPATH=%CLASSPATH%;./*