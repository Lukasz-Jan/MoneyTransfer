#!/bin/sh

app_directory=`pwd`

cp ./src/main/resources/envProperties/dev/postgres/application.properties ./src/main/resources/


sh ./environment/dev/startMongoContainer.sh
mvn clean install
docker stop mongodb
docker rm mongodb


sh ./environment/dev/startMongoContainer.sh
sh ./environment/dev/startPostgresContainer.sh
mvn spring-boot:run


