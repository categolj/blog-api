#!/bin/sh

cd repo
./mvnw package -Dmaven.test.skip=true -Dmaven.repo.local=../m2/rootfs/opt/m2
./mvnw help:evaluate -Dexpression=project.artifactId
artifactId=`./mvnw help:evaluate -Dexpression=project.artifactId | egrep -v '(^\[INFO])'`
cp target/${artifactId}.jar ../output/app.jar