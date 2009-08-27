#!/bin/bash

##############################################
MAVEN_OPTS="-Xms64m -Xmx256m"

# Configure this, if you don't have 'mvn' as an environment variable!
MAVEN="mvn"

# Toggle comments, if you will need sources and docs
PROJECT_INIT_FLAG=""
# PROJECT_INIT_FLAG="-DdownloadSources=true -DdownloadJavadocs=true"
##############################################

echo ""
cd ..
$MAVEN clean:clean eclipse:clean
echo "Environment cleaned."
cd tools
./compile.sh
cd ..
$MAVEN eclipse:eclipse $PROJECT_INIT_FLAG
cd tools
echo ""
echo "Environment initialized."
