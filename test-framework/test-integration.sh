#!/bin/bash
# Tests d'intégration : déploie l'application dans un Tomcat isolé et vérifie
# les routes réelles avec curl. Aucune modification du Tomcat du système.
set -euo pipefail

cd "$(dirname "$0")"

CATALINA_HOME="${CATALINA_HOME:-/opt/tomcat/apache-tomcat-10.0.16}"
PORT="${PORT:-18080}"
PORT_ARRET=$((PORT + 5))
PORT_AJP=$((PORT + 9))
CONTEXTE="${CONTEXTE:-test-framework}"
TRAVAIL="${TRAVAIL:-/tmp/opencode/framework-test}"
BASE="$TRAVAIL/tomcat"
CIBLE_WEBAPPS="$BASE/webapps"
URL="http://localhost:$PORT/$CONTEXTE"

verifications=0
echecs=0

verifier() {
    local description="$1"
    local condition="$2"
    verifications=$((verifications + 1))
    if [ "$condition" = "1" ]; then
        echo "  OK    $description"
    else
        echo "  ECHEC $description"
        echecs=$((echecs + 1))
    fi
}

condition() {
    if "$@" >/dev/null 2>&1; then echo 1; else echo 0; fi
}

code_http() {
    curl -s -o /dev/null -w '%{http_code}' "$@"
}

contient() {
    local url="$1"
    local attendu="$2"
    local methode="${3:-GET}"
    local corps
    corps="$(curl -s -X "$methode" "$url")"
    [[ "$corps" == *"$attendu"* ]] && echo 1 || echo 0
}

arreter_tomcat() {
    if [ -f "$BASE/logs/catalina.out" ]; then
        CATALINA_HOME="$CATALINA_HOME" CATALINA_BASE="$BASE" \
            "$CATALINA_HOME/bin/catalina.sh" stop >/dev/null 2>&1 || true
    fi
}

trap arreter_tomcat EXIT

./build.sh

rm -rf "$TRAVAIL"
mkdir -p "$BASE/logs" "$BASE/temp" "$BASE/webapps" "$BASE/work"

cp -r "$CATALINA_HOME/conf" "$BASE/conf"
sed -i \
    -e "s/port=\"8080\"/port=\"$PORT\"/g" \
    -e "s/port=\"8081\"/port=\"$PORT\"/g" \
    -e "s/port=\"8005\"/port=\"$PORT_ARRET\"/g" \
    -e "s/port=\"8009\"/port=\"$PORT_AJP\"/g" \
    "$BASE/conf/server.xml"

cp -r build "$CIBLE_WEBAPPS/$CONTEXTE"

CATALINA_HOME="$CATALINA_HOME" CATALINA_BASE="$BASE" \
    "$CATALINA_HOME/bin/catalina.sh" start >/dev/null 2>&1

pret=0
for _ in $(seq 1 40); do
    if [ "$(code_http "$URL/users")" = "200" ]; then
        pret=1
        break
    fi
    sleep 1
done

verifier "Tomcat répond sur $URL" "$pret"

if [ "$pret" = "1" ]; then
    echo
    echo "== Routes de base =="
    verifier "GET / renvoie 200" "$([ "$(code_http "$URL/")" = "200" ] && echo 1 || echo 0)"
    verifier "GET / contient Accueil" "$(contient "$URL/" "Accueil")"
    verifier "GET /users renvoie 200" "$([ "$(code_http "$URL/users")" = "200" ] && echo 1 || echo 0)"
    verifier "GET /about contient Page about" "$(contient "$URL/about" "Page about")"
    verifier "GET /login contient Page login" "$(contient "$URL/login" "Page login")"
    verifier "POST /login contient Connexion ok" \
        "$(contient "$URL/login" "Connexion ok" POST)"

    echo
    echo "== Vue JSP =="
    verifier "GET /bienvenue renvoie 200" "$([ "$(code_http "$URL/bienvenue")" = "200" ] && echo 1 || echo 0)"
    verifier "GET /bienvenue contient Bienvenue Rakoto" "$(contient "$URL/bienvenue" "Bienvenue Rakoto")"

    echo
    echo "== JSON et erreurs =="
    verifier "GET /api/users renvoie du JSON" \
        "$(condition bash -c "curl -s -D - -o /dev/null '$URL/api/users' | grep -qi '^Content-Type: application/json'")"
    verifier "GET /api/users contient Rakoto" "$(contient "$URL/api/users" "Rakoto")"
    verifier "GET /inconnu renvoie 404" "$([ "$(code_http "$URL/inconnu")" = "404" ] && echo 1 || echo 0)"
    verifier "404 liste les contrôleurs" "$(contient "$URL/inconnu" "Controllers détectés")"
    verifier "POST /users renvoie 405" "$([ "$(code_http -X POST "$URL/users")" = "405" ] && echo 1 || echo 0)"
    verifier "405 expose l'en-tête Allow" \
        "$(condition bash -c "curl -s -D - -o /dev/null -X POST '$URL/users' | grep -qi '^Allow: GET'")"
    verifier "GET /boom renvoie 500" "$([ "$(code_http "$URL/boom")" = "500" ] && echo 1 || echo 0)"

    echo
    echo "== Binding simple =="
    verifier "GET /cherche lit q et page" \
        "$(contient "$URL/cherche?q=livre&page=2" "Recherche livre page 2")"
    verifier "GET /cherche refuse une page non numérique" \
        "$([ "$(code_http "$URL/cherche?q=livre&page=abc")" = "400" ] && echo 1 || echo 0)"

    echo
    echo "== Démarrage =="
    verifier "le listener a initialisé les routes" \
        "$(condition grep -rq 'route(s) initialisés au démarrage' "$BASE/logs")"
else
    tail -40 "$BASE/logs/catalina.out" 2>/dev/null || true
fi

echo
echo "$verifications vérification(s), $echecs échec(s)."

if [ "$echecs" -ne 0 ]; then
    exit 1
fi
