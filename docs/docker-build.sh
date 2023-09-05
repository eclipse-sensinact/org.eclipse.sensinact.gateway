#!/bin/bash
######################################################################
# Copyright (c) 2023 Contributors to the Eclipse Foundation.
#
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Kentyou - initial implementation
######################################################################

#
# Script to run docker to compile the documentation
#

# Compute build formats

if [[ -z "$@" ]]
then
    echo "Build HTML by default"
    formats="html"
else
    formats="$@"
fi

# Run the container and let it sleep
echo "Starting container..."
CID=$(docker run --rm -d -it -v "${PWD}:/docs" -w /docs --entrypoint sleep sphinxdoc/sphinx inf)

# Install requirements
echo "Install requirements..."
docker exec "$CID" apt update
docker exec "$CID" apt install -y git
docker exec "$CID" python -m pip install -r requirements.txt

# Clear output directory
echo "Clean up..."
docker exec "$CID" make clean

# Generate outputs
for format in ${formats[@]}
do
    echo "Generate $format..."
    docker exec "$CID" make "$format"
done

# Clean up
docker rm -f "$CID"
