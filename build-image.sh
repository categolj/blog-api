#!/bin/bash
set -e
VERSION=$(grep '<version>' pom.xml | head -n 2 | tail -n 1 | sed -e 's|<version>||g' -e 's|</version>||g' -e 's| ||g' |  tr -d '\t')
BOOT_VERSION=$(grep '<version>' pom.xml | head -n 1 | sed -e 's|<version>||g' -e 's|</version>||g' -e 's|<.*>||g' -e 's| ||g')
./mvnw clean package -Dspring-boot.version=${BOOT_VERSION}
pack build making/blog-api:${VERSION} \
  -p target/blog-api-*.jar \
  --publish \
  --builder making/java-cnb-builder \
  #--builder cloudfoundry/cnb:bionic \

