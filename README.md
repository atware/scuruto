# Scuruto [![Build Status](https://travis-ci.org/atware/scuruto.svg?branch=master)](https://travis-ci.org/atware/scuruto)

An internal knowledge sharing app.

[![Heroku Deploy](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

or

[<img src="https://www.docker.com/sites/default/files/legal/small_h-dark.png" height="40" />](https://www.docker.com/)

1. Install [<img src="https://www.docker.com/sites/default/files/legal/small_h-dark.png" height="40" />](https://www.docker.com/)
1. Run ```docker-compose up```
1. access [http://localhost:8080/](http://localhost:8080/)

## System Requirements
- Scala 2.11 or 2.12
- PostgreSQL 9.5.x or 9.6.x

## Setup for Development Environment

### Setup PostgreSQL

Setup PostgreSQL server on your machine and create database.

    $ createuser -P scuruto
    $ createdb -E UTF8 -T template0 --lc-collate=ja_JP.UTF-8 --lc-ctype=ja_JP.UTF-8 scuruto

### Prepare Tables

#### mac/*nix

    ./scuruto db:migrate

#### Windows

    ./scuruto.bat db:migrate

### Set Environment Variables to scurutoEnv script

#### mac/*nix

    cp scurutoEnv-template scurutoEnv

#### Windows

    cp scurutoEnv-template.bat scurutoEnv.bat

##### Environment Variables

| ENV name (*required) | Description | Example |
|:----|:----|:----|
| SITE_NAME | site name for page header | "Scuruto:CompanyName" |
| SITE_TITLE | site title for `title` tag | "Scuruto" |
| DATABASE_HOST * | datebase host | "localhost:5432" |
| DATABASE_DBNAME * | database name | "scuruto" |
| DATABASE_USER * | database user | "user" |
| DATABASE_PASSWORD * | database password | "password" |
| GOOGLE_ANALYTICS_KEY | Google analytics key | "abcdefg" |
| LOGIN_PROVIDOR | login providor<br />`app` - Login with email/password<br />`google` - Login with Google+ account<br />`ldap` - Login with LDAP or ActiveDirectory<br />*default is `app`* | "google" |
| LOGIN_PERMITTED_EMAIL_DOMAINS | email domains to allow to login(comma-separeted)<br />**allow to login any email address if empty** | "yourcompany.co.jp" |
| SKINNY_OAUTH2_CLIENT_ID_GOOGLE | (if LOGIN_PROVIDOR==google)<br />Google OAuth2 API Key | "abcdefgabcdefg" |
| SKINNY_OAUTH2_CLIENT_SECRET_GOOGLE | (if LOGIN_PROVIDOR==google)<br />Google OAuth2 Secret Key | "abcdefgabcdefgabcdefg" |
| LDAP_TYPE | (if LOGIN_PROVIDOR==ldap)<br /> `plain` - use LDAP <br/> `ssl` - use LDAPS <br/> `tls` use STARTTLS  | "plain" |
| LDAP_HOST | (if LOGIN_PROVIDOR==ldap)<br />LDAP host name | "xxx.xx.xxx.xxx" |
| LDAP_PORT | (if LOGIN_PROVIDOR==ldap)<br />LDAP port | 389 |
| LDAP_BASE_DN | (if LOGIN_PROVIDOR==ldap)<br />Top level DN of your LDAP directory tree (used for user search)| "dc=example,dc=com" |
| LDAP_BIND_DN | (if LOGIN_PROVIDOR==ldap)<br />Username that has read access to the LDAP | "uid=user,cn=user,dc=example,dc=com" or "user@examle.com"|
| LDAP_BIND_PASSWORD | (if LOGIN_PROVIDOR==ldap)<br />Password for Bind DN account | "password" |
| LDAP_USER_NAME_ATTRIBUTE | (if LOGIN_PROVIDOR==ldap)<br />User name attribute of LDAP. This is used as username in this application. | "uid" or "sAMAccountName"|
| LDAP_MAIL_ADDRESS_ATTRIBUTE | (if LOGIN_PROVIDOR==ldap)<br />Mail address attribute of LDAP. This is used as email in this application. | "mail" or "userPrincipalName" |
| LDAP_KEY_STORE | (if LOGIN_PROVIDOR==ldap)<br /> Path to the Java keystore. if empty, use JVM default. In generally, this needs when TLS uses.| "/var/xx/cacerts" |
| UPLOAD_DESTINATION | image file upload destination<br />`local` - upload to local disk<br />`s3` - upload to Amazon S3<br />*default is `local`* | "s3" |
| LOCAL_UPLOAD_BASE_DIR | (if UPLOAD_DESTINATION==local)<br />base directory to upload image file | "/tmp" |
| LOCAL_UPLOAD_BASE_URL | (if UPLOAD_DESTINATION==local)<br />base url to access uploaded image file | "/static/uploads" |
| AWS_ACCESS_KEY | (if UPLOAD_DESTINATION==s3)<br />AWS access key | "abcdefg" |
| AWS_SECRET_KEY | (if UPLOAD_DESTINATION==s3)<br />AWS secret key | "abcdefgabcdefg" |
| AWS_S3_BUCKET | (if UPLOAD_DESTINATION==s3)<br />S3 bucket name | "scuruto-xx" |
| AWS_S3_BASE_DIR | (if UPLOAD_DESTINATION==s3)<br />base pash to upload image file | "images/" |
| AWS_S3_BASE_URL | (if UPLOAD_DESTINATION==s3)<br />base url to access uploaded image file | "https://xxxxxxxx.s3.amazonaws.com/" |

### Run Application

#### mac/*nix

    ./scuruto run

#### Windows

    ./scuruto.bat run


## Additional setup for Development Environment

Setup npm command on your machine and install npm packages.

    cd /path/to/scuruto/dir
    npm install

scuruto uses webpack. You must execute the command when static resources are changed.

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

### Customize ScurutoEnv on docker environment
If you want customize ScurutoEnv, you can customize environment valiables in docker-compose.yml .

#### Example
* customize docker-compose.yml
```
app:
  ...
  environment:
    LOGIN_PERMITTED_EMAIL_DOMAINS: "yourcompany.co.jp"
```

## License

MIT License
