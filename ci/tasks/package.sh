#!/bin/sh

cd repo
./mvnw package -Dmaven.test.skip=true -Dmaven.repo.local=../m2/rootfs/opt/m2
cp target/*.jar ../output/app.jar