#/bin/sh
cp ../packaging/target/*.zip ./sensiNact-gateway-latest.zip
docker build . -t sensinact:latest