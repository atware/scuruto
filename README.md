<img src="https://github.com/atware/sharedocs/blob/master/src/main/webapp/assets/img/logowtnd.png?raw=true" alt="Sharedocs" height="100">

<img src="https://github.com/atware/sharedocs/raw/master/misc/responsive.png?raw=true" alt="Responsive">

# Sharedocs [![Build Status](https://travis-ci.org/atware/sharedocs.svg?branch=master)](https://travis-ci.org/atware/sharedocs)

An internal knowledge sharing app.

:cloud: [Sharedocs Demo Site](https://sharedocs-demo.herokuapp.com/)

- :bust_in_silhouette: `demo1@example.com` / `demo1`
- :bust_in_silhouette: `demo2@example.com` / `demo2`

or

[![Heroku Deploy](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

## System Requirements
- Scala 2.11 or 2.12
- PostgreSQL 9.5.x or 9.6.x

## Setup for Development Environment

### Setup PostgreSQL

Setup PostgreSQL server on your machine and create database.

    $ createuser -P sharedocs
    $ createdb -E UTF8 -T template0 --lc-collate=ja_JP.UTF-8 --lc-ctype=ja_JP.UTF-8 sharedocs

### Prepare Tables

#### mac/*nix

    ./sharedocs db:migrate

#### Windows

    ./sharedocs.bat db:migrate

### Set Environment Variables to sharedocsEnv script

#### mac/*nix

    cp sharedocsEnv-template sharedocsEnv

#### Windows

    cp sharedocsEnv-template.bat sharedocsEnv.bat

##### Environment Variables

| ENV name (*required) | Description | Example |
|:----|:----|:----|:----|
| SITE_NAME | site name for page header | "Sharedocs:CompanyName" |
| SITE_TITLE | site title for `title` tag | "Sharedocs" |
| DATABASE_HOST * | datebase host | "localhost:5432" |
| DATABASE_DBNAME * | database name | "sharedocs" |
| DATABASE_USER * | database user | "user" |
| DATABASE_PASSWORD * | database password | "password" |
| GOOGLE_ANALYTICS_KEY | Google analytics key | "abcdefg" |
| LOGIN_PROVIDOR | login providor<br />`app` - Login with email/password<br />`google` - Login with Google+ account<br />*default is `app`* | "google" |
| LOGIN_PERMITTED_EMAIL_DOMAINS | email domains to allow to login(comma-separeted)<br />**allow to login any email address if empty** | "yourcompany.co.jp" |
| SKINNY_OAUTH2_CLIENT_ID_GOOGLE | (if LOGIN_PROVIDOR==google)<br />Google OAuth2 API Key | "abcdefgabcdefg" |
| SKINNY_OAUTH2_CLIENT_SECRET_GOOGLE | (if LOGIN_PROVIDOR==google)<br />Google OAuth2 Secret Key | "abcdefgabcdefgabcdefg" |
| UPLOAD_DESTINATION | image file upload destination<br />`local` - upload to local disk<br />`s3` - upload to Amazon S3<br />*default is `local`* | "s3" |
| LOCAL_UPLOAD_BASE_DIR | (if UPLOAD_DESTINATION==local)<br />base directory to upload image file | "/tmp" |
| LOCAL_UPLOAD_BASE_URL | (if UPLOAD_DESTINATION==local)<br />base url to access uploaded image file | "/static/uploads" |
| AWS_ACCESS_KEY | (if UPLOAD_DESTINATION==s3)<br />AWS access key | "abcdefg" |
| AWS_SECRET_KEY | (if UPLOAD_DESTINATION==s3)<br />AWS secret key | "abcdefgabcdefg" |
| AWS_S3_BUCKET | (if UPLOAD_DESTINATION==s3)<br />S3 bucket name | "sharedocs-xx" |
| AWS_S3_BASE_DIR | (if UPLOAD_DESTINATION==s3)<br />base pash to upload image file | "images/" |
| AWS_S3_BASE_URL | (if UPLOAD_DESTINATION==s3)<br />base url to access uploaded image file | "https://xxxxxxxx.s3.amazonaws.com/" |

### Run Application

#### mac/*nix

    ./sharedocs run

#### Windows

    ./sharedocs.bat run


## Additional setup for Development Environment

Setup npm command on your machine and install npm packages.

    cd /path/to/sharedocs/dir
    npm install

sharedocs uses webpack. You must execute the command when static resources are changed.

    npm run webpack

Alternatively, you can use watch mode as follows:

    npm run webpackw

After that, webpack recompiles static resources automatically after you change them.


## External service settings

### Google APIs
You must setup as below if you want to login with Google+ account.

https://console.developers.google.com/apis/credentials/ (Google Developers Console - API Manager - Credentials)

[Create credentials] - [OAuth client ID] - [Web application]

`Authorized redirect URIs` : `http://localhost:8080/session/callback`

### Amazon S3
You must setup as below if you want to upload files to Amazon S3.

https://console.aws.amazon.com/s3/

[Create Bucket] and then setting up bucket policy and CORS configuration.

#### Bucket Policy

Bucket Policy example:

[Bucket]-[Permissions]-[Edit bucket policy]

```
{
	"Version": "2012-10-17",
	"Id": "Policy*************",
	"Statement": [
		{
			"Sid": "Stmt*************",
			"Effect": "Allow",
			"Principal": "*",
			"Action": "s3:GetObject",
			"Resource": "arn:aws:s3:::[bucket name]/*"
		},
		{
			"Sid": "Stmt*************",
			"Effect": "Allow",
			"Principal": {
				"AWS": "arn:aws:iam::[user id]:user/[user-name]"
			},
			"Action": "s3:PutObject",
			"Resource": "arn:aws:s3:::[bucket name]/*"
		}
	]
}
```

#### CORS Configuration

CORS Configuration example:

[Bucket]-[Permissions]-[Edit CORS Configuration]

```
<?xml version="1.0" encoding="UTF-8"?>
<CORSConfiguration xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
    <CORSRule>
        <AllowedOrigin>*</AllowedOrigin>
        <AllowedMethod>GET</AllowedMethod>
        <AllowedMethod>POST</AllowedMethod>
    </CORSRule>
</CORSConfiguration>
```


## Lisence

MIT License
