#!/bin/sh


cd environment/
  sh transferImageBuild.sh
cd compose/
  sh nodes_start.sh

cd ../..