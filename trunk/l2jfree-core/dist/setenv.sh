VERSION=1.3.0

CLASSPATH=${CLASSPATH}:./lib/l2j-mmocore-${VERSION}.jar

#jython
CLASSPATH=${CLASSPATH}:./lib/jython-2.2.1.jar
CLASSPATH=${CLASSPATH}:./lib/jython-engine-1.0.1.jar

CLASSPATH=${CLASSPATH}:./lib/bsf-2.0.jar
CLASSPATH=${CLASSPATH}:./lib/bsh-2.0b5.jar
CLASSPATH=${CLASSPATH}:./lib/bsh-engine-1.0.1.jar

CLASSPATH=${CLASSPATH}:./lib/commons-lang-2.4.jar

# For connection pool
CLASSPATH=${CLASSPATH}:./lib/c3p0-0.9.1.2.jar

# for logging usage
CLASSPATH=${CLASSPATH}:./lib/commons-logging-1.1.1.jar

# for common input output 
CLASSPATH=${CLASSPATH}:./lib/commons-io-1.4.jar

# for performance usage
CLASSPATH=${CLASSPATH}:./lib/javolution-5.3.1.jar

# main jar
CLASSPATH=${CLASSPATH}:./lib/l2j-commons-${VERSION}.jar
CLASSPATH=${CLASSPATH}:l2jfree-core-${VERSION}.jar

CLASSPATH=${CLASSPATH}:./lib/core-3.3.0.jar
CLASSPATH=${CLASSPATH}:./lib/java-engine-1.0.2.jar

# For SQL use
CLASSPATH=${CLASSPATH}:./lib/mysql-connector-java-5.1.6.jar

# For irc use
CLASSPATH=${CLASSPATH}:./lib/irclib-1.10.jar

CLASSPATH=${CLASSPATH}:./lib/trove-2.1.1.jar

# for configuration
CLASSPATH=${CLASSPATH}:./config/
CLASSPATH=${CLASSPATH}:.

export CLASSPATH