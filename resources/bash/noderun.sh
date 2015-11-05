#!/bin/bash

rm -rf general/
ant run-benchmark -Dport=5006 -Dhost=$1
