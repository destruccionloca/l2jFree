#!/bin/bash

##############################################
# Configure this, if you don't have 'svn' in the path!
PATH=${PATH}:/usr/bin

MAVEN_OPTS="-Xms64m -Xmx256m"

# Configure this, if you don't have 'mvn' in the path!
MAVEN="mvn"
##############################################

echo ""
cd ..
cd l2j-mmocore
$MAVEN clean:clean install -Dmaven.test.skip=true
cd ..
cd l2j-commons
$MAVEN clean:clean install -Dmaven.test.skip=true
cd ..
cd l2jfree-core
$MAVEN clean:clean install assembly:assembly -Dmaven.test.skip=true
cd ..
cd l2jfree-login
$MAVEN clean:clean assembly:assembly -Dmaven.test.skip=true
cd ..
cd l2jfree-datapack
$MAVEN clean:clean compile -Dmaven.test.skip=true
cd ..
cd tools
echo ""
echo "Sources compiled, and dependencies installed to the local repository."
