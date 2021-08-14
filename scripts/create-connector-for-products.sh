#!/usr/bin/env bash

set -e

# https://docs.confluent.io/kafka-connect-spooldir/current/connectors/csv_source_connector.html

KEY_SCHEMA='{
      "type": "STRUCT",
      "name": "itemKey",
      "fieldSchemas": {
        "itemId": {
          "type": "STRING"
        }
      }
    }
'

KEY_SCHEMA_ESCAPED=$(echo "$KEY_SCHEMA" | tr -d '[:space:]' | sed -e 's/"/\\"/g')

VALUE_SCHEMA='{
      "type": "STRUCT",
      "name": "product",
      "fieldSchemas": {
        "itemId": {
          "type": "STRING"
        },
        "description": {
          "type": "STRING"
        },
        "count": {
          "type": "BIGINT"
        }
      }
    }
'

VALUE_SCHEMA_ESCAPED=$(echo "$VALUE_SCHEMA" | tr -d '[:space:]' | sed -e 's/"/\\"/g')

PAYLOAD="
{
    \"name\": \"product-csv-source-connector\",
    \"config\": {
        \"connector.class\": \"com.github.jcustenborder.kafka.connect.spooldir.SpoolDirCsvSourceConnector\",
        \"tasks.max\": \"1\",
        \"topic\": \"product_click\",
        \"input.path\": \"/data/raw/products/\",
        \"input.file.pattern\": \".*csv\",
        \"error.path\": \"/data/error/products/\",
        \"finished.path\": \"/data/finished/products\",
        \"csv.first.row.as.header\": true,
        \"key.schema\": \"${KEY_SCHEMA_ESCAPED}\",
        \"value.schema\": \"${VALUE_SCHEMA_ESCAPED}\"
    }
}
"

mkdir -p ./data/kafka-connect/raw/products
mkdir -p ./data/kafka-connect/error/products
mkdir -p ./data/kafka-connect/finished/products

echo "${PAYLOAD}" | jq

curl -s http://localhost:8083/connectors -H 'Content-Type: application/json' -H 'Accept: application/json' \
  --data "${PAYLOAD}" | jq


curl -s http://localhost:8083/connectors | jq
