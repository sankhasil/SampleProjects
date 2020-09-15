#!/bin/sh
#while ! nc -zv 127.0.0.1 4488 ; do
#    echo "Waiting for upcoming Config Server"
#   sleep 2
#done
#fuser -k 4488/tcp

java -jar /opt/QANDA/qanda-REST-1.0-SNAPSHOT.jar
