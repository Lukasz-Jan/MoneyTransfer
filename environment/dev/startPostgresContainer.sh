#!/bin/bash

container_name=postgres-db
#docker start ${container_name}

docker run --name postgres-db -e POSTGRES_PASSWORD=pwd -e POSTGRES_USER=myuser -e POSTGRES_DB=transfer_db -p 5432:5432 -d postgres:17-alpine