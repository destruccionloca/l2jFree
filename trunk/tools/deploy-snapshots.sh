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
$MAVEN clean:clean deploy -Dmaven.test.skip=true
cd ..
cd l2j-commons
$MAVEN clean:clean deploy -Dmaven.test.skip=true
cd ..
cd tools
echo ""
echo "Snapshots deployed."
