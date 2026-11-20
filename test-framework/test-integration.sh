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

contient_formulaire() {
    local url="$1"
    local attendu="$2"
    shift 2
    local corps
    corps="$(curl -s "$@" "$url")"
    [[ "$corps" == *"$attendu"* ]] && echo 1 || echo 0
}

contient_cookie() {
    local url="$1"
    local attendu="$2"
    local cookies="$3"
    local corps
    corps="$(curl -s -c "$cookies" -b "$cookies" "$url")"
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
    echo "== Binding objet =="
    verifier "GET /employe affiche le formulaire" "$(contient "$URL/employe" "Nouvel employé")"
    verifier "POST /employe remplit le nom" \
        "$(contient_formulaire "$URL/employe" "Johny" -X POST \
            --data-urlencode "nom=Johny" \
            --data-urlencode "poste=Developpeur" \
            --data-urlencode "salaire=1234.5" \
            --data-urlencode "actif=true" \
            --data-urlencode "adresse.rue=Lalana" \
            --data-urlencode "adresse.ville=Antananarivo")"
    verifier "POST /employe remplit le poste" \
        "$(contient_formulaire "$URL/employe" "Developpeur" -X POST \
            --data-urlencode "nom=Johny" --data-urlencode "poste=Developpeur")"
    verifier "POST /employe remplit le salaire" \
        "$(contient_formulaire "$URL/employe" "1234.5" -X POST \
            --data-urlencode "nom=Johny" --data-urlencode "salaire=1234.5")"
    verifier "POST /employe remplit le booléen" \
        "$(contient_formulaire "$URL/employe" ">oui<" -X POST \
            --data-urlencode "nom=Johny" --data-urlencode "actif=true")"
    verifier "POST /employe remplit l'objet imbriqué" \
        "$(contient_formulaire "$URL/employe" "Antananarivo" -X POST \
            --data-urlencode "nom=Johny" \
            --data-urlencode "adresse.rue=Lalana" \
            --data-urlencode "adresse.ville=Antananarivo")"
    verifier "POST /api/employe renvoie l'objet en JSON" \
        "$(contient_formulaire "$URL/api/employe" "\"nom\":\"Johny\"" -X POST \
            --data-urlencode "nom=Johny" \
            --data-urlencode "poste=Developpeur" \
            --data-urlencode "salaire=1234.5" \
            --data-urlencode "actif=true" \
            --data-urlencode "adresse.rue=Lalana" \
            --data-urlencode "adresse.ville=Antananarivo")"
    verifier "POST /api/employe sérialise l'objet imbriqué" \
        "$(contient_formulaire "$URL/api/employe" "\"rue\":\"Lalana\"" -X POST \
            --data-urlencode "nom=Johny" \
            --data-urlencode "adresse.rue=Lalana" \
            --data-urlencode "adresse.ville=Antananarivo")"
    verifier "POST /api/employe refuse un salaire invalide" \
        "$([ "$(code_http -X POST --data-urlencode "salaire=abc" "$URL/api/employe")" = "400" ] && echo 1 || echo 0)"

    echo
    echo "== Spring (sprint 5bis) =="
    verifier "GET /spring?nom=Johny appelle le service Spring" \
        "$(contient "$URL/spring?nom=Johny" "Bonjour Johny")"
    verifier "GET /spring sans nom utilise la valeur par défaut" \
        "$(contient "$URL/spring" "Bonjour inconnu")"
    verifier "GET /spring/contexte reçoit le WebApplicationContext" \
        "$(contient "$URL/spring/contexte" "Contexte Spring :")"
    verifier "le listener publie le contexte Spring" \
        "$(condition grep -rq 'Contexte Spring publié' "$BASE/logs")"

    echo
    echo "== Upload (sprint 8) =="
    FICHIER_TEST="$TRAVAIL/fichier-test.txt"
    printf 'contenu de test sprint 8\n' > "$FICHIER_TEST"
    TAILLE_FICHIER="$(wc -c < "$FICHIER_TEST" | tr -d ' ')"

    verifier "GET /upload affiche le formulaire" "$(contient "$URL/upload" "Envoyer un fichier")"
    verifier "POST /upload reçoit le fichier et les champs texte" \
        "$(contient_formulaire "$URL/upload" "fichier-test.txt" -F "nom=Johny" \
            -F "poste=Developpeur" -F "fichier=@$FICHIER_TEST;type=text/plain")"
    verifier "POST /upload expose le type de contenu" \
        "$(contient_formulaire "$URL/upload" "text/plain" -F "nom=Johny" \
            -F "fichier=@$FICHIER_TEST;type=text/plain")"
    verifier "POST /upload expose la taille" \
        "$(contient_formulaire "$URL/upload" "$TAILLE_FICHIER" -F "nom=Johny" \
            -F "fichier=@$FICHIER_TEST;type=text/plain")"
    verifier "POST /api/upload renvoie les métadonnées JSON" \
        "$(contient_formulaire "$URL/api/upload" "\"nom\":\"fichier-test.txt\"" -F "nom=Johny" \
            -F "fichier=@$FICHIER_TEST;type=text/plain")"
    verifier "POST /api/upload accepte l'absence de fichier" \
        "$(contient_formulaire "$URL/api/upload" "\"fichier\":null" -F "nom=Johny")"
    verifier "POST /upload sans fichier reste valide" \
        "$(contient_formulaire "$URL/upload" "aucun fichier" -F "nom=Johny")"

    DOSSIER_UPLOAD="$CIBLE_WEBAPPS/$CONTEXTE/uploads"
    AVANT="$(find "$DOSSIER_UPLOAD" -type f 2>/dev/null | wc -l | tr -d ' ')"
    contient_formulaire "$URL/upload" "fichier-test" -F "nom=Johny" \
        -F "fichier=@$FICHIER_TEST;type=text/plain" >/dev/null
    APRES="$(find "$DOSSIER_UPLOAD" -type f 2>/dev/null | wc -l | tr -d ' ')"
    verifier "un nouvel envoi crée un fichier supplémentaire sans écrasement" \
        "$([ "$APRES" -eq $((AVANT + 1)) ] && echo 1 || echo 0)"

    SAUVEGARDE="$(find "$DOSSIER_UPLOAD" -type f 2>/dev/null | sort | sed -n '1p')"
    verifier "le contenu enregistré est identique à l'original" \
        "$(condition cmp -s "$FICHIER_TEST" "$SAUVEGARDE")"

    echo
    echo "== Session (sprint 9) =="
    COOKIES="$TRAVAIL/session-cookies.txt"
    rm -f "$COOKIES"
    verifier "GET /compteur incrémente la session" \
        "$(contient_cookie "$URL/compteur" "Compteur : 1" "$COOKIES")"
    verifier "GET /compteur avec le même cookie donne 2" \
        "$(contient_cookie "$URL/compteur" "Compteur : 2" "$COOKIES")"
    verifier "GET /api/session renvoie les attributs en JSON" \
        "$(contient_cookie "$URL/api/session" "\"compteur\":2" "$COOKIES")"
    verifier "GET /reset vide la session" \
        "$(contient_cookie "$URL/reset" "OK" "$COOKIES")"
    verifier "GET /compteur repart à 1 après reset" \
        "$(contient_cookie "$URL/compteur" "Compteur : 1" "$COOKIES")"

    echo
    echo "== Authentification (sprint 10) =="
    COOKIES_AUTH="$TRAVAIL/auth-cookies.txt"
    rm -f "$COOKIES_AUTH"
    verifier "route publique accessible" \
        "$([ "$(code_http "$URL/public")" = "200" ] && echo 1 || echo 0)"
    verifier "route privée sans session renvoie 401" \
        "$([ "$(code_http "$URL/prive")" = "401" ] && echo 1 || echo 0)"
    verifier "route privée API sans session renvoie 401" \
        "$([ "$(code_http "$URL/api/prive")" = "401" ] && echo 1 || echo 0)"
    verifier "route privée API sans session renvoie du JSON" \
        "$(condition bash -c "curl -s '$URL/api/prive' | grep -q 'Authentification requise'")"

    curl -s -c "$COOKIES_AUTH" -b "$COOKIES_AUTH" "$URL/auth/login?role=user" >/dev/null
    verifier "route privée avec session renvoie 200" \
        "$([ "$(code_http -b "$COOKIES_AUTH" "$URL/prive")" = "200" ] && echo 1 || echo 0)"
    verifier "route privée avec session contient le contenu" \
        "$(contient_cookie "$URL/prive" "Page privée" "$COOKIES_AUTH")"

    if [ "$(code_http -b "$COOKIES_AUTH" "$URL/admin")" = "403" ]; then
        verifier "route admin avec rôle user renvoie 403" "1"
    else
        verifier "route admin avec rôle user renvoie 403" "0"
    fi

    rm -f "$COOKIES_AUTH"
    curl -s -c "$COOKIES_AUTH" -b "$COOKIES_AUTH" "$URL/auth/login?role=admin" >/dev/null
    verifier "route admin avec rôle admin renvoie 200" \
        "$([ "$(code_http -b "$COOKIES_AUTH" "$URL/admin")" = "200" ] && echo 1 || echo 0)"
    verifier "route admin avec rôle admin contient le contenu" \
        "$(contient_cookie "$URL/admin" "Page admin" "$COOKIES_AUTH")"

    echo
    echo "== Rôles (sprint 10bis) =="
    rm -f "$COOKIES_AUTH"
    curl -s -c "$COOKIES_AUTH" -b "$COOKIES_AUTH" "$URL/auth/login?role=user" >/dev/null
    if [ "$(code_http -b "$COOKIES_AUTH" "$URL/rh")" = "403" ]; then
        verifier "route RH avec rôle user renvoie 403" "1"
    else
        verifier "route RH avec rôle user renvoie 403" "0"
    fi

    rm -f "$COOKIES_AUTH"
    curl -s -c "$COOKIES_AUTH" -b "$COOKIES_AUTH" "$URL/auth/login?role=RH" >/dev/null
    verifier "route RH avec rôle RH renvoie 200" \
        "$([ "$(code_http -b "$COOKIES_AUTH" "$URL/rh")" = "200" ] && echo 1 || echo 0)"
    verifier "route RH avec rôle RH contient le contenu" \
        "$(contient_cookie "$URL/rh" "Espace RH" "$COOKIES_AUTH")"

    rm -f "$COOKIES_AUTH"
    curl -s -c "$COOKIES_AUTH" -b "$COOKIES_AUTH" "$URL/auth/login?role=admin" >/dev/null
    verifier "route RH avec rôle admin renvoie 200" \
        "$([ "$(code_http -b "$COOKIES_AUTH" "$URL/rh")" = "200" ] && echo 1 || echo 0)"

    echo
    echo "== Singleton (sprint 11) =="
    verifier "première requête singleton renvoie 1" \
        "$(contient "$URL/singleton" "Singleton : 1")"
    verifier "deuxième requête singleton renvoie 2 (même instance)" \
        "$(contient "$URL/singleton" "Singleton : 2")"

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
