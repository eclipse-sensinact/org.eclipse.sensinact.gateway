#!/bin/bash
# Generate JSON matrix of non-aggregator modules for parallel testing

set -e

# Find all pom.xml files
pom_files=$(find . -name "pom.xml" -not -path "*/target/*" -not -path "*/.flattened-pom.xml")

modules=()

for pom in $pom_files; do
  # Check if this is an aggregator POM (has <packaging>pom</packaging> and <modules>)
  is_aggregator=$(xmllint --xpath "boolean(//*[local-name()='project']/*[local-name()='packaging']/text()='pom' and //*[local-name()='project']/*[local-name()='modules'])" "$pom" 2>/dev/null || echo "false")

  if [ "$is_aggregator" = "false" ]; then
    # This is a leaf module - extract the path relative to repo root
    module_dir=$(dirname "$pom")
    # Remove leading './'
    module_dir=${module_dir#./}

    # Only add if not the root pom
    if [ "$module_dir" != "." ]; then
      modules+=("$module_dir")
    fi
  fi
done

# Convert to JSON array
json_array=$(printf '%s\n' "${modules[@]}" | jq -R . | jq -s .)

# Output the matrix
echo "{\"module\": $json_array}"
