#!/bin/bash

##############################################
# Configure this, if you don't have 'mvn' as an environment variable!
MAVEN="mvn"
##############################################

echo ""
cd ..
$MAVEN clean:clean eclipse:clean eclipse:eclipse -DdownloadSources=true -DdownloadJavadocs=true dependency:sources
echo ""
echo "Done."
