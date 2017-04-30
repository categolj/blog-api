#!/bin/sh
echo y | fly -t home sp -p blog-blog-api -c pipeline.yml -l ../../credentials.yml
