#!/bin/bash

JAR="microservice/boot/service.jar"

if [ ! -f "$JAR" ]; then
    echo "Reconstruction du JAR..."
    cat microservice/boot/service_* > "$JAR"
fi

echo "Lancement du service..."
java -jar "$JAR"
