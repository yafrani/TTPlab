#!/bin/bash

# read config
. config.properties

./bins/linkern/linkern -I 0 -R $2 -o ./bins/linkern/$1.tour ${tspdata}$1.tsp 
