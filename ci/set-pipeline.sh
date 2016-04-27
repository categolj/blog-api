#!/bin/sh
echo y | fly -t do sp -p blog-blog-api -c pipeline.yml -l ../../credentials.yml
