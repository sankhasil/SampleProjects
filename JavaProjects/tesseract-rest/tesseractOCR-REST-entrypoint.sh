#!/bin/sh
#while ! nc -zv 127.0.0.1 4488 ; do
#    echo "Waiting for upcoming Config Server"
#   sleep 2
#done
#fuser -k 4488/tcp

# goss serve
goss -g /opt/tesseractOCR/goss.yaml s -l :10000 &
java -jar /opt/tesseractOCR/tesseractOCR-REST-1.0-SNAPSHOT.jar
