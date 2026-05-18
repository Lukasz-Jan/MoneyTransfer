#!/bin/bash


main_directory=`pwd`
cd ../..
cd src/main/resources/envProperties/prod/postgres/
cp application.properties ./../../..

cd ${main_directory}

docker compose -f docker-compose.yml up --remove-orphans -d