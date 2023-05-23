#!/bin/sh
# get some vars from env and write to json
RUNTIME_CONF="{
  \"INFO_CHECK_URI\": \"$INFO_CHECK_URI\",
  \"INFO_BASE_URI\": \"$INFO_BASE_URI\"
}"
echo $RUNTIME_CONF > /home/nonroot/docker/artifact/config/config.json
# start my app
http-server /home/nonroot/docker/artifact/
