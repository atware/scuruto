#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" = "false" ] && [ "$TRAVIS_BRANCH" = "master" ]; then
  docker login -u="$DOCKER_USERNAME" -p="$DOCKER_PASSWORD" &&
    ./sharedocs package:docker &&
    ./sharedocs publish:docker
fi
