find src -name "*.java" > sources.txt

javac \
    -cp lib/servlet-api.jar \
    -d build \
    @sources.txt

rm sources.txt

cd build && jar -cf ../framework.jar framework/ && cd ..