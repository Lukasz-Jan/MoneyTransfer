#!/bin/bash

container_name=postgres-db

if docker inspect "$container_name" > /dev/null 2>&1; then
    echo "The container $container_name exists."
    # Check if the container is running
    if $(docker inspect -f '{{.State.Status}}' "$container_name" | grep -q "running"); then
        echo "The container $container_name is running."
    else
        echo "Starting $container_name to build integration tests."
        docker start postgres-db
    fi
else
    echo "The container $container_name does not exist."
    # Create and start the container if it does not exist
    echo "Starting $container_name to build integration tests."
    docker run --name postgres-db -e POSTGRES_PASSWORD=pwd -e POSTGRES_USER=myuser -e POSTGRES_DB=transfer_db -p 5432:5432 -d postgres:17-alpine
fi