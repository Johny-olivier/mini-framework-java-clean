#!/bin/bash
set -e

cd "$(dirname "$0")"

./compile.sh

rm -rf build-test
mkdir -p build-test
find tests -name "*.java" | sort > build-test/sources.txt

javac \
    -encoding UTF-8 \
    -parameters \
    -cp "build:lib/servlet-api.jar" \
    -d build-test \
    @build-test/sources.txt

rm -f build-test/sources.txt

java -cp "build-test:build:lib/servlet-api.jar" framework.tests.LanceurTests
