#!/bin/bash

# read config
. config.properties

./bins/linkern/linkern -R 1 -o ./bins/linkern/$1.tour ${tspdata}$1.tsp 
