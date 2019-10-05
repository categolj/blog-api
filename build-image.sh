#!/bin/bash
set -e

./mvnw clean package -Drestdoc.scheme=https -Drestdoc.host=blog-api.ik.am -Drestdoc.port=443

#VERSION=$(grep '<version>' pom.xml | head -n 1 | tail -n 1 | sed -e 's|<version>||g' -e 's|</version>||g' -e 's| ||g' |  tr -d '\t')
VERSION=latest
pack build making/blog-api:${VERSION} \
  -p target/blog-api-*.jar \
  --publish \
  --no-pull \
  --builder cloudfoundry/cnb:bionic \
  --buildpack org.cloudfoundry.openjdk,org.cloudfoundry.jvmapplication,org.cloudfoundry.springboot,org.cloudfoundry.distzip
  #--builder asia.gcr.io/fe-tmaki/cf-build-service-dev-219913-build-service-builders-p-builder-08a87070945c7177ae30c35d16d0f4ed \
  #--buildpack org.cloudfoundry.archiveexpanding,io.pivotal.openjdk,org.cloudfoundry.jvmapplication,org.cloudfoundry.springboot,org.cloudfoundry.distzip

