# Heroku Deployment

## Instant Run

[![Heroku Deploy](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

----

## Setup on your own

### Setup infrastructure

You can easily setting up infrastructure on Heroku platform with Terraform using `heroku.tf`.

```shell
$ cd /path/to/scuruto/deployment/heroku
$ vi heroku.tf
$ terraform plan
$ terraform apply
```

### Install heroku-buildpack-scuruto

You can easily deploy Scuruto app to Heroku platform with it.
[atware/heroku\-buildpack\-scuruto](https://github.com/atware/heroku-buildpack-scuruto)

```shell
$ heroku buildpacks:set https://github.com/atware/heroku-buildpack-scuruto
```

Next,
git push to heroku, then run build Scuruto application.

### Set the environment variables

You must set the environment variables to heroku,

```shell
$ heroku config:set LOGIN_PERMITTED_EMAIL_DOMAINS=your-team.com

```

### Setup database tables

After first push to heroku, you must execute skinny's db:migrate to create Scuruto's tables.

```shell
$ heroku run deployment/heroku/skinny db:migrate heroku
```

## Trouble Shooting

### Push failed

```
Compiled slug size: 346.7M is too large (max is 300M)
```

If you met an push error, try app's repo compaction as below.

```shell
$ heroku plugins:install heroku-repo
$ heroku repo:gc -a appname
$ heroku repo:purge_cache -a appname
```

[heroku/heroku\-repo: Plugin for heroku CLI that can manipulate the repo](https://github.com/heroku/heroku-repo)

### Timezone

```shell
$ heroku config:add TZ=Asia/Tokyo
Setting TZ and restarting ⬢ scuruto-test... done, v20
TZ: Asia/Tokyo
```
