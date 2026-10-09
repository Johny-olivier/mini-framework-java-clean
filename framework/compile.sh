javac -cp lib/servlet-api.jar \
      -d build \
      src/framework/servlet/*.java

jar cf framework.jar -C build .