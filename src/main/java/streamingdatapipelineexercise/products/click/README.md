## Product Click 

Bash into kafka container

```bash
docker-compose exec kafka bash
```


Create topic in Kakfa

```bash
kafka-topics --create --topic product_click_avro --bootstrap-server localhost:9092
```

Find the schema Id

```bash
# run commands in container

# install jq
apt-get update ; apt-get install jq

SUBJECT=product_click_avro
VERSION=1
SCHEMA_ID=$(curl -s "http://host.docker.internal:8081/subjects/${SUBJECT}-value/versions/${VERSION}" | jq '.id')
echo $SCHEMA_ID
```

Publish messages

```bash
# change $SCHEMA_ID to schema id from previous command

kafka-avro-console-producer \
--broker-list kafka:9092 \
--topic click_avro \
--property schema.registry.url=http://host.docker.internal:8081 \
--property value.schema.id=$SCHEMA_ID

# with message like

{"itemId": "1000", "description": "移动硬盘8t X", "count": 1, "eventTime": 1623259315000}
```

