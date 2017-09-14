FROM alpine

RUN apk add --no-cache --virtual build-deps g++ make perl-dev tzdata && \
	cp /usr/share/zoneinfo/UTC /etc/localtime && \
	echo UTC > /etc/timezone && \
	apk add --no-cache perl && \
	cpan App::Sqitch && \
	apk del build-deps

VOLUME /src
WORKDIR /src
ENTRYPOINT ["sqitch"]
