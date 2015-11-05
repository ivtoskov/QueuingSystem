#!/bin/bash

rm -rf db/
ant run-benchmark -Dport=5432 -Dhost=$1
