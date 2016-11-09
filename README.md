# Sharedocs

An internal knowledge sharing app.

## Setup for Development Environment

### Setup PostgreSQL

Setup PostgreSQL server on your machine and create database.

    $ createuser -P sharedocs
    $ createdb -E UTF8 -T template0 --lc-collate=ja_JP.UTF-8 --lc-ctype=ja_JP.UTF-8 sharedocs

### Set Environment Variables to sharedocsEnv script

#### mac/*nix

    cp sharedocsEnv-template sharedocsEnv

#### Windows

    cp sharedocsEnv-template.bat sharedocsEnv.bat

### Prepare Tables

#### mac/*nix

    ./sharedocs db:migrate

#### Windows

    ./sharedocs.bat db:migrate

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

TODO