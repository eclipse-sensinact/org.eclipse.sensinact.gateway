FROM ubuntu:latest
ENV DEBIAN_FRONTEND noninteractive
RUN apt-get update
RUN apt-get install -y apt-utils curl iptables telnet bsdtar sqlite3 openjdk-8-jre
WORKDIR /opt
RUN mkdir sensiNact
ADD ./sensiNact-gateway-latest.zip /opt/sensiNact
WORKDIR /opt/sensiNact
RUN bsdtar -xf ./sensiNact-gateway-latest.zip -s'|[^/]*/||'
RUN cp -vf load/rest/*.jar bundle/
RUN chmod +x sensinact
EXPOSE 8080
ENTRYPOINT ["/opt/sensiNact/sensinact"]