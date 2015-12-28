#!/bin/bash

# read config
. config.properties

./bins/linkern/linkern -I 4 -R 1 -o ./bins/linkern/$1.tour ${tspdata}$1.tsp 
