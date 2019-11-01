#!/bin/bash
set -e

./mvnw clean package -Drestdoc.scheme=https -Drestdoc.host=blog-api.ik.am -Drestdoc.port=443

#VERSION=$(grep '<version>' pom.xml | head -n 1 | tail -n 1 | sed -e 's|<version>||g' -e 's|</version>||g' -e 's| ||g' |  tr -d '\t')
VERSION=latest
pack build making/blog-api:${VERSION} \
  -p target/blog-api-*.jar \
  --publish \
  --builder making/java-cnb-builder \
  #--builder cloudfoundry/cnb:bionic \

