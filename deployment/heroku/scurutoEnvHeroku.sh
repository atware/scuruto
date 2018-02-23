#!/bin/bash

# You must set the environment variables to heroku,
# e.g.
# heroku config:set LOGIN_PERMITTED_EMAIL_DOMAINS=your-team.com
#

export SITE_NAME=Scuruto:HEROKU
export SITE_TITLE=Scuruto

export LOGIN_PROVIDOR=app

export UPLOAD_DESTINATION=local
export LOCAL_UPLOAD_BASE_DIR="/tmp"
export LOCAL_UPLOAD_BASE_URL="/upload/file"

export SKINNY_ENV=heroku