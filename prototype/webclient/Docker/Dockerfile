FROM node:19-alpine3.16
RUN addgroup -S nonroot \
    && adduser -S nonroot -G nonroot
ENV NODE_ENV=production
RUN  npm install -global --ignore-scripts http-server
USER nonroot
RUN mkdir -p /home/nonroot/docker/artifact
COPY --chown=nonroot:nonroot dist /home/nonroot/docker/artifact/
COPY Docker/start.sh /home/nonroot/docker/artifact/
CMD [ "sh", "/home/nonroot/docker/artifact/start.sh" ]
