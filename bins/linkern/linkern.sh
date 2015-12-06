#!/bin/bash

# read config
. config.properties

./bins/linkern/linkern -t $2 -R $3 -o ./bins/linkern/$1.tour ${tspdata}$1.tsp 
