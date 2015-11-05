#!/bin/bash

DIR=/home/kennelcrash/MySpace/Studium/ETH/Semester1/AdvancedSystemsLab/AWS
scp -v -i $DIR/asl.pem -r ubuntu@$2:/home/ubuntu/$1 .
