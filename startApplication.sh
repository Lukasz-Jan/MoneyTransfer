#!/bin/sh

cp ./src/main/resources/envProperties/prod/postgres/application.properties ./src/main/resources/

sh ./environment/dev/startMongoContainer.sh

mvn clean install
#mvn clean install -Dmaven.test.skip

docker stop mongodb
docker rm mongodb

docker build -t "transfer_c1" .

cd ./environment/compose
sh compose_start.sh
cd ../..


