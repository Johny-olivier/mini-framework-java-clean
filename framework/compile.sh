#!/bin/bash
set -e

cd "$(dirname "$0")"

rm -rf build
mkdir -p build

find src -name "*.java" | sort > build/sources.txt

javac \
    -encoding UTF-8 \
    -parameters \
    -cp lib/servlet-api.jar \
    -d build \
    @build/sources.txt

rm -f build/sources.txt

jar -cf framework.jar -C build .

echo "framework.jar construit ($(find build -name '*.class' | wc -l) classes)."
