#!/bin/bash
set -e

cd "$(dirname "$0")"

RACINE="$(cd .. && pwd)"
CATALINA_HOME="${CATALINA_HOME:-/opt/tomcat/apache-tomcat-10.0.16}"

./telecharger-spring.sh

"$RACINE/framework/compile.sh"

BUILD="build"
rm -rf "$BUILD"
mkdir -p "$BUILD/WEB-INF/classes" "$BUILD/WEB-INF/lib"

# web.xml et ressources du répertoire WEB-INF
cp -r WEB-INF/. "$BUILD/WEB-INF/"

# Le framework est toujours reconstruit : on ne réutilise pas un jar périmé.
cp "$RACINE/framework/framework.jar" "$BUILD/WEB-INF/lib/framework.jar"

# Les vues sont à la racine de l'application (view-prefix=/views/).
cp -r views "$BUILD/views"

find src -name "*.java" | sort > sources.txt

javac \
    -encoding UTF-8 \
    -parameters \
    -cp "$BUILD/WEB-INF/lib/*:$CATALINA_HOME/lib/servlet-api.jar" \
    -d "$BUILD/WEB-INF/classes" \
    @sources.txt

rm -f sources.txt

echo "Application de test construite dans $BUILD/."
