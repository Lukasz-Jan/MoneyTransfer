#!/bin/sh

cd ./environment/dev

#curl -X POST localhost:8082/close
procNo=`netstat -nlp | grep :8082 | sed -rn "s/.* ([0-9]*)\/java/\1/p"`
kill -9 $procNo

docker stop mongodb
docker rm mongodb

docker stop postgres-db
docker rm postgres-db
