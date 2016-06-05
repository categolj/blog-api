#!/bin/sh
echo y | fly -t azr sp -p blog-blog-api -c pipeline.yml -l ../../credentials.yml
