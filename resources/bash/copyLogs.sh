#!/bin/bash

DIR=/home/kennelcrash/MySpace/Studium/ETH/Semester1/AdvancedSystemsLab/AWS

mv /home/kennelcrash/PycharmProjects/untitled1/ASL/data/* /home/kennelcrash/MySpace/Studium/ETH/Semester1/AdvancedSystemsLab/Logs

for var in $(cat clients.txt);
do
  scp -v -i $DIR/asl.pem -r ubuntu@$var:/home/ubuntu/QueuingSystem/general/* /home/kennelcrash/PycharmProjects/untitled1/ASL/data/
done
