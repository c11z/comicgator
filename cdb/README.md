# Comic Database (CDB)

... is the primary datastore for Comic Gator using PostgreSQL 9.5 and organized with [sqitch](http://sqitch.org/).

Sqitch is cross platform but I installed it on my OSX with.

`brew tap theory/sqitch && brew install sqitch_pg`

Using the excellent postgres docker image.

```
docker run \
--name cdb-dev \
-p 5432:5432 \ 
-e "POSTGRES_USER=mrcg" \ 
-e "POSTGRES_PASSWORD=mrcg" \
-e "POSTGRES_DB=cdb" \
-e "PGDATA=/var/lib/postgresql/data" \
postgres:9.5
```

For a local development environment.

I have included a sqitch.conf example file.

`mv sqitch.conf.example sqitch.conf`

I keep my configuration private on account of it contains my production target and access credentials.

`sqitch deploy`

to deploy the schema.
