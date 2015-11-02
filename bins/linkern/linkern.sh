#!/bin/bash

#echo $1
#echo $2
./bins/linkern/linkern -t $2 -R $3 -o ./bins/linkern/$1.tour ./bins/linkern/../../TTP1_data/$1-ttp/$1.tsp 
