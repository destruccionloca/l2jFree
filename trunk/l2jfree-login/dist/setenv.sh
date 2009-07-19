# Hibernate and associated jars
CLASSPATH=${CLASSPATH}:antlr-2.7.6.jar
CLASSPATH=${CLASSPATH}:asm-1.5.3.jar
CLASSPATH=${CLASSPATH}:cglib-2.1_3.jar
CLASSPATH=${CLASSPATH}:hibernate-3.2.2.ga.jar
CLASSPATH=${CLASSPATH}:jta-1.0.1B.jar
CLASSPATH=${CLASSPATH}:commons-collections-2.1.1.jar

# for second level cache (hibernate)
CLASSPATH=${CLASSPATH}:ehcache-1.2.3.jar

CLASSPATH=${CLASSPATH}:l2j-mmocore-1.3.0.jar

# for bean use
CLASSPATH=${CLASSPATH}:commons-beanutils-1.7.0.jar

CLASSPATH=${CLASSPATH}:commons-lang-2.1.jar

# For connection pool
CLASSPATH=${CLASSPATH}:c3p0-0.9.1.2.jar

# for logging usage
CLASSPATH=${CLASSPATH}:commons-logging-1.1.jar

# for common input output 
CLASSPATH=${CLASSPATH}:commons-io-1.2.jar

# for dom 
CLASSPATH=${CLASSPATH}:dom4j-1.6.1.jar

# for performance usage
CLASSPATH=${CLASSPATH}:javolution-1.5.5.2.6.jar

# main jar
CLASSPATH=${CLASSPATH}:l2j-commons-1.3.0.jar
CLASSPATH=${CLASSPATH}:l2jfree-login-1.3.0.jar

# spring 
CLASSPATH=${CLASSPATH}:spring-2.0.2.jar

# For SQL use
CLASSPATH=${CLASSPATH}:mysql-connector-java-5.1.5.jar

# for configuration
CLASSPATH=${CLASSPATH}:./config/
CLASSPATH=${CLASSPATH}:.

export CLASSPATH