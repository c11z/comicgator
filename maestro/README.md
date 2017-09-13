# Maestro

... is the API for Comic Gator using the Scala Play Framework.

For a development environment, use docker to build

```
docker build \
-t comicgator/maestro:dev \
-f Dockerfile.dev .
```

Then you can run it independently.

```
docker run \
-it \
-e "APPLICATION_SECRET=8Vz>klF16_mNUwYN1Mr@e^=vL37Ydj/kRT^808y18TCNm5DoY<wdz_hb9C[SKV<B"
-e "COMIC_GATOR_HOSTNAME=http://localhost:9000"
-e "DATABASE_URL=jdbc:postgresql://0.0.0.0:5432/cdb"
-e "DATABASE_USER=mrcg"
-e "DATABASE_PASSWORD=mrcg"
--name maestro-dev \
-v $PWD:/home/c11z/maestro comicgator/maestro:dev
```

or run with docker-compose.

`docker-compose up`