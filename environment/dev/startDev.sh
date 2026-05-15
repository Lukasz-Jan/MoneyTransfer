#!/bin/bash

docker run --name postgres-db -e POSTGRES_PASSWORD=pwd -e POSTGRES_USER=myuser -e POSTGRES_DB=transfer_db -p 5432:5432 -d postgres

docker run --name mongodb -p 27017:27017 -d mongodb/mongodb-community-server:latest

cd ../..
main_directory=`pwd`
cd src/main/resources/envProperties/dev/postgres/
cp application.properties ./../../..


cd ${main_directory}

mvn spring-boot:run