#!/bin/bash

#kapp deploy -a blog-api -c --wait -f <(kbld -f k8s/blog-api.yml)
kbld -f k8s/blog-api.yml | kubectl apply -f -
