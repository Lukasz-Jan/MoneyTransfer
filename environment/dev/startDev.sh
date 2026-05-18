#!/bin/bash

#docker run --name postgres-db -e POSTGRES_PASSWORD=pwd -e POSTGRES_USER=myuser -e POSTGRES_DB=transfer_db -p 5432:5432 -d postgres:17-alpine



sh startMongo.sh

cd ../..
pom_directory=`pwd`

#cd src/main/resources/envProperties/dev/postgres/
#cp application.properties ./../../..

cd src/main/resources/envProperties/dev/H2/
cp application.properties ./../../..



cd ${pom_directory}

mvn clean install
mvn spring-boot:run