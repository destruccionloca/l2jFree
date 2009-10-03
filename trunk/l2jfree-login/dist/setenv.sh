# VERSION=1.3.0

# Hibernate and associated jars
# CLASSPATH=${CLASSPATH}:./lib/antlr-2.7.6.jar
# CLASSPATH=${CLASSPATH}:./lib/asm-1.5.3.jar
# CLASSPATH=${CLASSPATH}:./lib/asm-attrs-1.5.3.jar
# CLASSPATH=${CLASSPATH}:./lib/cglib-2.1_3.jar
# CLASSPATH=${CLASSPATH}:./lib/hibernate-3.2.2.ga.jar
# CLASSPATH=${CLASSPATH}:./lib/jta-1.0.1B.jar
# CLASSPATH=${CLASSPATH}:./lib/commons-collections-2.1.1.jar

# for second level cache (hibernate)
# CLASSPATH=${CLASSPATH}:./lib/ehcache-1.2.3.jar

# CLASSPATH=${CLASSPATH}:./lib/l2j-mmocore-${VERSION}.jar

# CLASSPATH=${CLASSPATH}:./lib/commons-lang-2.4.jar

# For connection pool
# CLASSPATH=${CLASSPATH}:./lib/c3p0-0.9.1.2.jar

# for logging usage
# CLASSPATH=${CLASSPATH}:./lib/commons-logging-1.1.1.jar

# for common input output 
# CLASSPATH=${CLASSPATH}:./lib/commons-io-1.4.jar

# for dom 
# CLASSPATH=${CLASSPATH}:./lib/dom4j-1.6.1.jar

# for performance usage
# CLASSPATH=${CLASSPATH}:./lib/javolution-5.3.1.jar

# main jar
# CLASSPATH=${CLASSPATH}:./lib/l2j-commons-${VERSION}.jar
# CLASSPATH=${CLASSPATH}:l2jfree-login-${VERSION}.jar
CLASSPATH=${CLASSPATH}:./lib/*

# spring 
# CLASSPATH=${CLASSPATH}:./lib/spring-2.0.2.jar
# CLASSPATH=${CLASSPATH}:./lib/spring-mock-2.0.2.jar

# For SQL use
# CLASSPATH=${CLASSPATH}:./lib/mysql-connector-java-5.1.6.jar

# for configuration
CLASSPATH=${CLASSPATH}:./config/*
CLASSPATH=${CLASSPATH}:./*

export CLASSPATH