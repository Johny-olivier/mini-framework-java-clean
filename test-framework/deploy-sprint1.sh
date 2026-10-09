#!/bin/bash

APP_NAME="teste-framework"
BUILD_DIR="build"
SRC_DIR="src"
WEBINF_DIR="WEB-INF"

TOMCAT_WEBAPPS="/opt/tomcat/apache-tomcat-10.0.16/webapps"

FRAMEWORK_JAR="$WEBINF_DIR/lib/framework.jar"
SERVLET_API="/opt/tomcat/apache-tomcat-10.0.16/lib/servlet-api.jar"

echo "Nettoyage..."
rm -rf "$BUILD_DIR"

echo "Création structure WAR..."
mkdir -p "$BUILD_DIR/WEB-INF/classes"

echo "Copie WEB-INF..."
cp -r "$WEBINF_DIR"/* "$BUILD_DIR/WEB-INF/"

echo "Compilation des classes du projet de test..."

find "$SRC_DIR" -name "*.java" > sources.txt

if [ -s sources.txt ]; then
    javac \
        -cp "$FRAMEWORK_JAR:$SERVLET_API" \
        -d "$BUILD_DIR/WEB-INF/classes" \
        @sources.txt
fi

rm -f sources.txt

echo "Création WAR..."

cd "$BUILD_DIR" || exit 1

jar -cf "$APP_NAME.war" *

cd ..

echo "Déploiement..."

cp -f "$BUILD_DIR/$APP_NAME.war" "$TOMCAT_WEBAPPS/"

echo "Terminé."