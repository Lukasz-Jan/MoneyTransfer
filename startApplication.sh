#!/bin/sh

pomDirectory=`pwd`

cd src/main/resources/envProperties/prod/postgres/
cp application.properties ./../../..

cd ${pomDirectory}

mvn clean install -Dmaven.test.skip

docker build -t "transfer_c1" .

cd ./environment/compose
  sh nodes_start.sh

cd ../..