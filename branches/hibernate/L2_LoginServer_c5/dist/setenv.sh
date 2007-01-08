# Hibernate and associated jars
CLASSPATH=${CLASSPATH}:antlr-2.7.6.jar
CLASSPATH=${CLASSPATH}:asm.jar
CLASSPATH=${CLASSPATH}:asm-attrs.jar
CLASSPATH=${CLASSPATH}:cglib-2.1.3.jar
CLASSPATH=${CLASSPATH}:hibernate-3.1rc2.jar
CLASSPATH=${CLASSPATH}:jta.jar

# for second level cache (hibernate)
CLASSPATH=${CLASSPATH}:ehcache-1.2.3.jar

# for bean use
CLASSPATH=${CLASSPATH}:commons-beanutils-1.7.0.jar

# For connection pool
CLASSPATH=${CLASSPATH}:c3p0-0.9.0.4.jar

# for logging usage
CLASSPATH=${CLASSPATH}:commons-logging-1.1.jar
CLASSPATH=${CLASSPATH}:log4j-1.2.14.jar

# for common input output 
CLASSPATH=${CLASSPATH}:commons-io-1.2.jar

# for dom 
CLASSPATH=${CLASSPATH}:dom4j-1.6.1.jar

# for performance usage
CLASSPATH=${CLASSPATH}:javolution.jar

# main jar
CLASSPATH=${CLASSPATH}:l2j-loginserver.jar

# spring 
CLASSPATH=${CLASSPATH}:spring-2.0-rc3.jar

# For SQL use
CLASSPATH=${CLASSPATH}:mysql-connector-java-3.1.10-bin.jar
CLASSPATH=${CLASSPATH}:sqljdbc.jar

# for configuration
CLASSPATH=${CLASSPATH}:./config/

export CLASSPATH