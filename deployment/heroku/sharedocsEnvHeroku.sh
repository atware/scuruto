#!/bin/bash

# You must set the environment variables to heroku,
# e.g.
# heroku config:set AWS_ACCESS_KEY=********
# heroku config:set AWS_SECRET_KEY=********
# heroku config:set AWS_S3_BUCKET=********
# heroku config:set AWS_S3_BASE_DIR=********/
# heroku config:set AWS_S3_BASE_URL=https://********.s3.amazonaws.com/
# heroku config:set LOGIN_PERMITTED_EMAIL_DOMAINS=********
#

export SITE_NAME=Sharedocs:HEROKU
export SITE_TITLE=Sharedocs

export LOGIN_PROVIDOR=app

export UPLOAD_DESTINATION=s3

export SKINNY_ENV=heroku