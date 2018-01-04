@ECHO OFF

rem -------------------------------------------------------------------------------------
rem Sharedocs configuration variables
rem
rem For more details, see: https://github.com/atware/sharedocs/blob/master/README.md
rem -------------------------------------------------------------------------------------

SET SITE_NAME=Sharedocs:local
SET SITE_TITLE=Sharedocs

SET DATABASE_HOST=********
SET DATABASE_DBNAME=********
SET DATABASE_USER=********
SET DATABASE_PASSWORD=********

SET GOOGLE_ANALYTICS_KEY=********

SET LOGIN_PROVIDOR=app
SET LOGIN_PERMITTED_EMAIL_DOMAINS=example.com

SET SKINNY_OAUTH2_CLIENT_ID_GOOGLE=*************************
SET SKINNY_OAUTH2_CLIENT_SECRET_GOOGLE=***********************

SET LDAP_TYPE=plain
SET LDAP_HOST=***
SET LDAP_PORT=389
SET LDAP_BASE_DN=***
SET LDAP_BIND_DN=***
SET LDAP_BIND_PASSWORD=***
SET LDAP_USER_NAME_ATTRIBUTE=uid
SET LDAP_MAIL_ADDRESS_ATTRIBUTE=mail
SET LDAP_KEY_STORE=

SET UPLOADS_DESTINATION=s3

SET LOCAL_UPLOAD_BASE_DIR=%TEMP%
SET LOCAL_UPLOAD_BASE_URL="/uploads"

SET AWS_ACCESS_KEY=********
SET AWS_SECRET_KEY=********
SET AWS_S3_BUCKET="xxxxxxxx"
SET AWS_S3_BASE_DIR="images/"
SET AWS_S3_BASE_URL=https://xxxxxxxx.s3.amazonaws.com/

SET EXTERNAL_INTEGRATION_SERVICE=logger

SET OGP_ALLOW_UA_PREFIXES="UA prefix to allow to access"

rem SET MARKDOWN_HELP_PAGE_ID=1
