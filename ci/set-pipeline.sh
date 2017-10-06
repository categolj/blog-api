#!/bin/sh
fly -t home sp -p blog-blog-api \
    -c `dirname $0`/pipeline.yml \
    -l `dirname $0`/../../credentials.yml