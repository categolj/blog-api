#!/bin/sh

cd repo
./mvnw deploy --settings ci/tasks/settings.xml -Dmaven.test.skip=true -Dmaven.repo.local=../m2/rootfs/opt/m2 -DperformRelease=true
