#!/bin/bash
set -e

cd "$(dirname "$0")"

VERSION="${SPRING_VERSION:-6.1.6}"
DESTINATION="WEB-INF/lib"
DEPOT_LOCAL="${M2_REPO:-$HOME/.m2/repository}/org/springframework"
BASE_URL="https://repo1.maven.org/maven2/org/springframework"

mkdir -p "$DESTINATION"

for artefact in spring-core spring-beans spring-context spring-expression spring-jcl spring-web spring-aop; do
    cible="$DESTINATION/$artefact-$VERSION.jar"
    if [ -f "$cible" ]; then
        continue
    fi

    source="$DEPOT_LOCAL/$artefact/$VERSION/$artefact-$VERSION.jar"
    if [ -f "$source" ]; then
        cp "$source" "$cible"
        echo "Spring : $artefact-$VERSION.jar copié depuis le dépôt Maven local."
    else
        echo "Spring : téléchargement de $artefact-$VERSION.jar..."
        curl -fsSL -o "$cible" "$BASE_URL/$artefact/$VERSION/$artefact-$VERSION.jar" || {
            rm -f "$cible"
            echo "Échec du téléchargement de $artefact-$VERSION.jar." >&2
            exit 1
        }
    fi
done
