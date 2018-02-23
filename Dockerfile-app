FROM ubuntu:latest

RUN apt-get update
RUN apt-get install -y scala
RUN apt-get install -y curl
RUN curl -sL https://deb.nodesource.com/setup_6.x | bash - && apt-get install -y nodejs
RUN apt-get install -y  build-essential
ADD . /opt/scuruto/

RUN touch /opt/scuruto/scurutoEnv

RUN cd /opt/scuruto && npm install

RUN mkdir -p /var/scuruto/upload
VOLUME /var/scuruto/upload

EXPOSE 8080
CMD ["/opt/scuruto/scuruto", "run"]
