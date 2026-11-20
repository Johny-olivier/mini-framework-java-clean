#!/bin/bash
set -e

cd "$(dirname "$0")"

./build.sh

CATALINA_HOME="${CATALINA_HOME:-/opt/tomcat/apache-tomcat-10.0.16}"
DESTINATION="${TOMCAT_WEBAPPS:-$CATALINA_HOME/webapps}"
CONTEXTE="${CONTEXTE:-test-framework}"

rm -rf "$DESTINATION/$CONTEXTE"
cp -r build "$DESTINATION/$CONTEXTE"

echo "Application déployée dans $DESTINATION/$CONTEXTE."
