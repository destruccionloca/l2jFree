#!/bin/bash

##############################################
# Configure this, if you don't have 'mvn' as an environment variable!
MAVEN="mvn"
##############################################

echo ""
$MAVEN clean:clean eclipse:clean eclipse:eclipse -DdownloadSources=true -DdownloadJavadocs=true dependency:sources install -Dmaven.test.skip=true
echo ""
echo "Done."
