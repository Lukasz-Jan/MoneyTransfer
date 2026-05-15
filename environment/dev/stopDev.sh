#!/bin/bash


curl -X POST localhost:8082/close

docker stop mongodb
docker rm mongodb

docker stop postgres-db
docker rm postgres-db