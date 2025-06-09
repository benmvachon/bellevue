# fly/mysql/Dockerfile
FROM mysql:8

ENV MYSQL_ROOT_PASSWORD=root
ENV MYSQL_DATABASE=bellevue_dev
ENV TZ=America/New_York

# Copy your SQL files into the standard init path
COPY ./sql /docker-entrypoint-initdb.d/
