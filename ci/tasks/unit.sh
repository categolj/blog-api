#!/bin/bash

cd config-server-repo
./mvnw spring-boot:run -Dmaven.repo.local=../m2/rootfs/opt/m2 -Drun.arguments="--server.port=8888" &

while true; do
    check=`curl localhost:8888`
    if [ "X$check" != "X" ]; then
        break
    fi
    echo "wait for config server boot"
    sleep 1
done
cd ..

cd repo
./mvnw test -Dmaven.repo.local=../m2/rootfs/opt/m2