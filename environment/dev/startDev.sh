#!/bin/bash


current_directory=`pwd`

sh startMongo.sh

cd ../..
app_directory=`pwd`

#-----postgres env--------------------------------------
cd src/main/resources/envProperties/dev/postgres/
cp application.properties ./../../..
cd ${app_directory}

cd src/test/resources/envProperties/dev/postgres/
cp application.properties ./../../..

cd ${current_directory}
sh startPostgres.sh
#-----postgres env--------------------------------------

#-----H2 env--------------------------------------------
#cd src/main/resources/envProperties/dev/H2/
#cp application.properties ./../../..
#cd ${app_directory}
#cd src/test/resources/envProperties/dev/H2/
#cp application.properties ./../../..
#-----H2 env--------------------------------------------



cd ${app_directory}

mvn clean install
mvn spring-boot:run