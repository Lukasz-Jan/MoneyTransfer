#!/bin/sh

mvn clean install

cd environment/
  sh transferImageBuild.sh
cd compose/
  sh nodes_start.sh

cd ../..