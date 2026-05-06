#!/bin/bash

container_name="mongodb"

if docker inspect "$container_name" > /dev/null 2>&1; then
    echo "The container $container_name exists."
    # Check if the container is running
    if $(docker inspect -f '{{.State.Status}}' "$container_name" | grep -q "running"); then
        echo "The container $container_name is running."
    else
        echo "Starting mongodb to build integration tests."
        docker start mongodb
    fi
else
    echo "The container $container_name does not exist."
    # Create and start the container if it does not exist
    echo "Starting mongodb to build integration tests."
    docker run --name mongodb -p 27017:27017 -d mongodb/mongodb-community-server:latest
fi

cd ../
mvn clean install
echo "Stopping mongodb after build."
docker stop mongodb
docker rm mongodb


docker build -t "transfer_c1" .
cd ./environment
