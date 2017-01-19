FROM postgres:9.5

RUN apt-get update
RUN apt-get install -y task-japanese
RUN echo "ja_JP.UTF-8 UTF-8" >> /etc/locale.gen
RUN locale-gen
RUN update-locale LANG=ja_JP.UTF-8
RUN localedef -i ja_JP  -c -f UTF-8 -A /usr/share/locale/locale.alias ja_JP.UTF-8

ENV LANG ja_JP.UTF-8

# exec DDL & DML
ADD src/main/resources/db/migration/*.sql /docker-entrypoint-initdb.d/

EXPOSE 5432
CMD ["postgres"]
