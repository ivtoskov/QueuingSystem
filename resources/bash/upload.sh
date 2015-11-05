#!/bin/bash

DIR=/home/kennelcrash/MySpace/Studium/ETH/Semester1/AdvancedSystemsLab/AWS
for var in $(cat $2);
do
  scp -v -i $DIR/asl.pem $1 ubuntu@$var:/home/ubuntu
done
