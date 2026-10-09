#!/bin/bash

APP_NAME="teste-framework"
BUILD_DIR="build"
TOMCAT_WEBAPPS="/opt/tomcat/apache-tomcat-10.0.16/webapps"

# Nettoyage
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"

# Copie du contenu web
cp -r WEB-INF "$BUILD_DIR"

# Création du WAR
cd "$BUILD_DIR" || exit 1
jar -cf "$APP_NAME.war" *
cd ..

# Déploiement
cp -f "$BUILD_DIR/$APP_NAME.war" "$TOMCAT_WEBAPPS/"

echo "Déploiement terminé."